package com.mashang.socialservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.socialservice.domain.entity.Post;
import com.mashang.common.common.R;
import com.mashang.socialservice.domain.query.create.PostQuery;
import com.mashang.socialservice.domain.vo.CommentVo;
import com.mashang.socialservice.domain.vo.PostVo;
import com.mashang.socialservice.feign.UserServiceFeign;
import com.mashang.socialservice.mapper.PostMapper;
import com.mashang.socialservice.mapping.PostMapping;
import com.mashang.socialservice.service.ICommentService;
import com.mashang.socialservice.service.IPostService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * 社区动态服务实现类
 * <p>
 * 核心职责：处理动态（帖子）的发布、删除、列表查询、详情查询、点赞和取消点赞等完整业务逻辑。
 * 设计思路：
 * <ul>
 *   <li><b>软删除策略</b>：删除动态时不物理删除数据库记录，仅将 delFlag 标记为 2L（已删除），
 *       保留数据用于后续审计和数据分析</li>
 *   <li><b>权限校验</b>：删除时校验 userId 是否匹配动态的发布者，防止越权删除他人动态</li>
 *   <li><b>点赞去重（Redis Set）</b>：使用 Redis Set 存储每条动态的点赞用户ID集合，
 *       通过 SISMEMBER 命令判断是否已点赞，避免重复点赞；
 *       点赞/取消操作直接 ADD/REM 操作 Set，时间复杂度 O(1)</li>
 *   <li><b>多级缓存策略</b>：动态列表缓存（按运动会）和动态详情缓存（按动态ID）分别管理，
 *       写入/删除时精确删除相关缓存，避免缓存污染</li>
 *   <li><b>Cache Aside 模式</b>：查询时先查缓存，未命中查数据库并回写；写操作后删除缓存</li>
 *   <li><b>跨服务调用（Feign）</b>：通过 Feign 调用 user-service 获取用户昵称，
 *       调用失败时使用降级值（"用户"+userId），保证核心功能不受用户服务故障影响</li>
 * </ul>
 *
 * @author SportLink Team
 */
@Service
public class PostServiceImpl extends ServiceImpl<PostMapper, Post> implements IPostService {

    /**
     * Redis Key 前缀：动态点赞用户集合
     * 完整 Key 格式：social:like:post:{postId}
     * 数据类型：Set（集合），存储点赞用户的 userId 字符串集合
     * 用途：快速判断用户是否已点赞（SISMEMBER）以及记录/移除点赞用户
     */
    private static final String LIKE_KEY_PREFIX = "social:like:post:";

    /**
     * Redis Key 前缀：运动会动态列表缓存
     * 完整 Key 格式：social:post:meeting:{meetingId}
     * 数据类型：String（序列化后的 List<PostVo>）
     * 过期时间：5 分钟
     */
    private static final String POST_MEETING_PREFIX = "social:post:meeting:";

    /**
     * Redis Key 前缀：动态详情缓存
     * 完整 Key 格式：social:post:detail:{postId}
     * 数据类型：String（序列化后的 PostVo，含评论列表）
     * 过期时间：5 分钟
     */
    private static final String POST_DETAIL_PREFIX = "social:post:detail:";

    /** 动态数据访问层，负责自定义 SQL 查询（按运动会查询、按ID查详情、更新计数等） */
    @Autowired
    private PostMapper postMapper;

    /** 评论服务，用于拼接动态详情中的评论树形结构 */
    @Autowired
    private ICommentService commentService;

    /** Redis 操作模板，用于 <b>Cache Aside 模式</b> 的缓存读写和 <b>点赞 Set</b> 操作 */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户服务 Feign 远程调用客户端
     * 用于获取用户昵称（username），实现跨服务数据聚合
     */
    @Autowired
    private UserServiceFeign userServiceFeign;

    /**
     * 发布动态
     * <p>
     * 业务流程：
     * <ol>
     *   <li>通过 MapStruct 将请求参数转换为 Post 实体</li>
     *   <li>设置 userId（发布者）、初始化 likeCount=0、commentCount=0</li>
     *   <li>设置 status=0L（正常）和 delFlag=0L（未删除）</li>
     *   <li>插入数据库，MyBatis-Plus 自动回填主键 postId</li>
     *   <li>删除该运动会的动态列表缓存，下次查询时重新加载</li>
     *   <li>构建返回的 PostVo，通过 Feign 获取发布者用户名</li>
     * </ol>
     * <p>
     * 使用场景：用户在社区中分享比赛精彩瞬间或发表感言。
     *
     * @param postQuery 动态发布请求体，包含 meetingId、content、imageUrl
     * @param userId 发布者用户ID
     * @return 发布成功的动态详情 VO，包含自动生成的 postId 和 createTime
     */
    @Override
    @Transactional
    public PostVo publish(PostQuery postQuery, Long userId) {
        // 将请求参数转换为数据库实体
        Post post = PostMapping.INSTANCE.toEntity(postQuery);
        post.setUserId(userId);
        post.setLikeCount(0L);       // 新发布的动态点赞数为 0
        post.setCommentCount(0L);    // 新发布的动态评论数为 0
        post.setStatus(0L);          // 状态：0=正常（待审核通过后可能变更为其他状态）
        post.setDelFlag(0L);         // 删除标记：0=未删除，2=已删除

        postMapper.insert(post);
        // MyBatis-Plus 自动将数据库生成的主键回填到 post.postId 字段

        // Cache Aside 模式：发布新动态后删除该运动会的动态列表缓存
        // 下次查询时从数据库重新加载，保证缓存中能包含新发布的动态
        redisTemplate.delete(POST_MEETING_PREFIX + postQuery.getMeetingId());

        // 构建返回给前端的 VO 对象
        PostVo vo = new PostVo();
        vo.setPostId(post.getPostId());
        vo.setUserId(post.getUserId());
        vo.setMeetingId(post.getMeetingId());
        vo.setContent(post.getContent());
        vo.setImageUrl(post.getImageUrl());
        vo.setLikeCount(0L);
        vo.setCommentCount(0L);
        vo.setCreateTime(post.getCreateTime());
        vo.setUsername(getUsername(userId));  // 通过 Feign 获取用户名

        return vo;
    }

    /**
     * 删除动态（软删除）
     * <p>
     * 核心业务规则：
     * <ol>
     *   <li><b>存在性校验</b>：先查询动态是否存在，不存在则抛异常</li>
     *   <li><b>权限校验</b>：只有动态发布者本人才能删除，
     *       userId 不匹配则抛出"无权删除他人动态"异常</li>
     *   <li><b>软删除</b>：将 delFlag 从 0L（正常）改为 2L（已删除），
     *       数据库记录保留，查询时通过 delFlag=0 过滤掉已删除数据</li>
     *   <li><b>缓存清理</b>：删除成功后清除该运动会的列表缓存和该动态的详情缓存</li>
     * </ol>
     * <p>
     * 使用场景：用户想要删除自己发布的某条动态。
     *
     * @param postId 动态ID，唯一标识一条动态
     * @param userId 当前操作用户ID，用于权限校验
     * @return 受影响行数，>0 表示删除成功
     * @throws RuntimeException 动态不存在时抛出
     * @throws RuntimeException 无权删除他人动态时抛出
     */
    @Override
    public int delete(Long postId, Long userId) {
        Post post = postMapper.selectById(postId);
        if (post == null) {
            throw new RuntimeException("动态不存在");
        }
        // 权限校验：判断当前用户是否为动态发布者
        if (!post.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除他人动态");
        }

        // 软删除：将 delFlag 标记为 2L（已删除）
        post.setDelFlag(2L);
        int result = postMapper.updateById(post);

        // Cache Aside 模式：删除成功后清除相关缓存
        if (result > 0) {
            redisTemplate.delete(POST_MEETING_PREFIX + post.getMeetingId());  // 清除列表缓存
            redisTemplate.delete(POST_DETAIL_PREFIX + postId);               // 清除详情缓存
        }
        return result;
    }

    /**
     * 按运动会ID查询动态列表（带 Redis 缓存）
     * <p>
     * 采用 <b>Cache Aside 模式</b>：
     * <ol>
     *   <li>先查缓存：Key = social:post:meeting:{meetingId}</li>
     *   <li>缓存未命中则查数据库，按创建时间倒序排列</li>
     *   <li>为每条动态填充发布者用户名（通过 Feign 调用 user-service）</li>
     *   <li>将结果写入 Redis 缓存，设置 5 分钟过期</li>
     * </ol>
     * <p>
     * 使用场景：进入运动会的社区首页，展示所有动态信息流。
     *
     * @param meetingId 运动会ID，唯一标识一届运动会
     * @return 该运动会下所有动态的 VO 列表，按时间倒序，含用户名
     */
    @Override
    public List<PostVo> listByMeetingId(Long meetingId) {
        String key = POST_MEETING_PREFIX + meetingId;
        // Cache Aside 模式：第一步，先查缓存
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return (List<PostVo>) cached;
        }
        // Cache Aside 模式：第二步，缓存未命中，查询数据库
        List<PostVo> list = postMapper.selectByMeetingId(meetingId);
        if (list != null) {
            // 批量填充用户名（每条动态都需要显示发布者昵称）
            list.forEach(vo -> vo.setUsername(getUsername(vo.getUserId())));
            // Cache Aside 模式：第三步，写入缓存，5分钟过期（社区数据更新较频繁）
            redisTemplate.opsForValue().set(key, list, 5, TimeUnit.MINUTES);
        }
        return list;
    }

    /**
     * 查询动态详情（带 Redis 缓存，含评论树形结构）
     * <p>
     * 采用 <b>Cache Aside 模式</b>，与列表查询独立缓存：
     * <ol>
     *   <li>先查详情缓存：Key = social:post:detail:{postId}</li>
     *   <li>缓存未命中则查数据库获取动态基本信息</li>
     *   <li>填充发布者用户名（Feign 调用）</li>
     *   <li>加载评论树形结构（调用 CommentService.listByPostId）</li>
     *   <li>将完整详情（含评论树）写入缓存，5分钟过期</li>
     * </ol>
     * <p>
     * 注意：评论数据的加载涉及递归构建树形结构，有一定计算开销，
     * 因此单独设置详情缓存以提升频繁查看的体验。
     * <p>
     * 使用场景：用户点击动态进入详情页，查看完整内容和所有评论。
     *
     * @param postId 动态ID，唯一标识一条动态
     * @return 动态详情 VO，包含动态信息、用户名和评论树形结构
     */
    @Override
    public PostVo detail(Long postId) {
        String key = POST_DETAIL_PREFIX + postId;
        // Cache Aside 模式：第一步，先查缓存
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return (PostVo) cached;
        }
        // Cache Aside 模式：第二步，缓存未命中，查询数据库
        PostVo vo = postMapper.selectDetailById(postId);
        if (vo != null) {
            vo.setUsername(getUsername(vo.getUserId()));
            // 加载评论树形结构：调用评论服务获取该动态下的所有评论（已组织为树形）
            List<CommentVo> comments = commentService.listByPostId(postId);
            vo.setComments(comments);
            // Cache Aside 模式：第三步，写入缓存，5分钟过期
            redisTemplate.opsForValue().set(key, vo, 5, TimeUnit.MINUTES);
        }
        return vo;
    }

    /**
     * 点赞动态
     * <p>
     * <b>Redis Set 去重策略</b>：
     * <ol>
     *   <li>使用 SISMEMBER 命令检查用户 userId 是否已存在于该动态的点赞 Set 中</li>
     *   <li>如果已存在（已点赞），直接抛出异常提示"已经点赞过了"，防止重复点赞</li>
     *   <li>如果不存在，执行 SADD 命令将 userId 加入 Set</li>
     *   <li>通过 Mapper 更新数据库中该动态的 likeCount +1</li>
     * </ol>
     * <p>
     * 使用 Redis Set 的优势：
     * <ul>
     *   <li>去重判断 O(1) 时间复杂度</li>
     *   <li>天然支持集合操作，无需额外的去重表</li>
     *   <li>Set 中的成员即点赞用户列表，方便后续扩展"谁点赞了"功能</li>
     * </ul>
     * <p>
     * 使用场景：用户点击动态的点赞按钮。
     *
     * @param postId 动态ID
     * @param userId 点赞用户ID
     * @return true 表示点赞成功
     * @throws RuntimeException 已经点赞过时抛出
     */
    @Override
    public boolean like(Long postId, Long userId) {
        String key = LIKE_KEY_PREFIX + postId;
        // Redis SISMEMBER：O(1) 判断用户是否已在点赞集合中
        Boolean isMember = redisTemplate.opsForSet().isMember(key, userId.toString());
        if (Boolean.TRUE.equals(isMember)) {
            // 已点赞，拒绝重复操作
            throw new RuntimeException("已经点赞过了");
        }

        // Redis SADD：将用户ID加入点赞集合
        redisTemplate.opsForSet().add(key, userId.toString());
        // 更新数据库中的点赞计数（+1）
        postMapper.updateLikeCount(postId, 1);
        return true;
    }

    /**
     * 取消点赞
     * <p>
     * <b>Redis Set 移除策略</b>：
     * <ol>
     *   <li>使用 SREM 命令从该动态的点赞 Set 中移除用户 userId</li>
     *   <li>如果移除成功（返回 >0，说明用户之前确实点过赞），
     *       则更新数据库 likeCount -1 并返回 true</li>
     *   <li>如果移除失败（返回 0，说明用户之前未点赞），直接返回 false</li>
     * </ol>
     * <p>
     * 使用场景：用户取消已点的赞。
     *
     * @param postId 动态ID
     * @param userId 取消点赞的用户ID
     * @return true 表示取消成功，false 表示之前未点赞无需取消
     */
    @Override
    public boolean unlike(Long postId, Long userId) {
        String key = LIKE_KEY_PREFIX + postId;
        // Redis SREM：从集合中移除用户ID，返回被移除的数量
        Long removed = redisTemplate.opsForSet().remove(key, userId.toString());
        if (removed != null && removed > 0) {
            // 移除成功 => 用户之前点过赞，更新数据库计数 -1
            postMapper.updateLikeCount(postId, -1);
            return true;
        }
        return false;  // 移除数量为 0 => 用户之前未点赞
    }

    /**
     * 通过 Feign 远程调用获取用户昵称
     * <p>
     * <b>Feign 调用降级处理</b>：
     * <ol>
     *   <li>通过 Feign 客户端向 user-service 发起 HTTP 请求获取用户信息</li>
     *   <li>调用成功且数据完整，则返回用户的 username（昵称）</li>
     *   <li>如果调用失败（网络超时、服务不可用等），catch 异常不做任何处理，
     *       返回降级默认值</li>
     *   <li>如果返回数据中 username 为空，同样返回降级默认值</li>
     *   <li><b>降级默认值</b>："用户"+userId，保证前端至少能展示一个可识别标识</li>
     * </ol>
     * <p>
     * 设计考量：用户服务不可用时不应影响社交服务的核心功能（浏览动态、发布内容），
     * 用户名降级为"用户123"这种格式虽不友好但比直接报错更优。
     *
     * @param userId 用户ID
     * @return 用户昵称，如果获取失败则返回 "用户"+userId 格式的默认值
     */
    private String getUsername(Long userId) {
        try {
            // 通过 Feign 调用 user-service 的 getUserInfo 接口
            R<Map<String, Object>> result = userServiceFeign.getUserInfo(userId);
            if (result != null && result.getData() != null && result.getData().get("username") != null) {
                return result.getData().get("username").toString();
            }
        } catch (Exception e) {
            // Feign 调用失败（网络异常、超时、服务不可用等），静默降级
            // 不抛出异常，保证社交服务核心流程不受影响
        }
        // 降级处理：返回默认用户名格式
        return "用户" + userId;
    }

}
