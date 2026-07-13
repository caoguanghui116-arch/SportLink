package com.mashang.socialservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.socialservice.domain.entity.Comment;
import com.mashang.common.common.R;
import com.mashang.socialservice.domain.query.create.CommentQuery;
import com.mashang.socialservice.domain.vo.CommentVo;
import com.mashang.socialservice.feign.UserServiceFeign;
import com.mashang.socialservice.mapper.CommentMapper;
import com.mashang.socialservice.mapper.PostMapper;
import com.mashang.socialservice.mapping.CommentMapping;
import com.mashang.socialservice.service.ICommentService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.stream.Collectors;

/**
 * 评论互动服务实现类
 * <p>
 * 核心职责：处理评论的添加、删除、列表查询、树形结构构建、点赞等完整业务逻辑。
 * 设计思路：
 * <ul>
 *   <li><b>多层级评论</b>：通过 parentId 字段实现评论的多层级嵌套回复，
 *       一级评论的 parentId 为 null，子回复的 parentId 指向其父评论</li>
 *   <li><b>树形结构构建（递归）</b>：查询时先将所有评论平铺查出，再通过递归算法
 *       将其组织为树形结构（一级评论 -> 子回复 -> 孙回复...），交给前端直接渲染</li>
 *   <li><b>评论数联动</b>：添加/删除评论时同步更新所属动态的 commentCount 字段</li>
 *   <li><b>软删除策略</b>：删除评论不物理删除记录，仅将 delFlag 标记为 2L</li>
 *   <li><b>权限校验</b>：删除时校验用户是否为评论发布者，防止越权删除</li>
 *   <li><b>点赞去重（Redis Set）</b>：与动态点赞机制一致，使用 Redis Set 记录点赞用户</li>
 *   <li><b>缓存失效策略</b>：评论变更后同时清除评论列表缓存和动态详情缓存，
 *       保证用户看到的评论区数据是最新的</li>
 *   <li><b>Feign 调用降级</b>：获取用户名时如果 user-service 不可用，返回降级默认值</li>
 * </ul>
 *
 * @author SportLink Team
 */
@Service
public class CommentServiceImpl extends ServiceImpl<CommentMapper, Comment> implements ICommentService {

    /**
     * Redis Key 前缀：评论点赞用户集合
     * 完整 Key 格式：social:like:comment:{commentId}
     * 数据类型：Set（集合），存储点赞用户的 userId 字符串集合
     */
    private static final String LIKE_KEY_PREFIX = "social:like:comment:";

    /**
     * Redis Key 前缀：动态评论列表缓存
     * 完整 Key 格式：social:comment:post:{postId}
     * 数据类型：String（序列化后的 List<CommentVo> 树形结构）
     * 过期时间：5 分钟
     */
    private static final String COMMENT_POST_PREFIX = "social:comment:post:";

    /** 评论数据访问层，负责自定义 SQL 查询（按动态ID查询评论列表） */
    @Autowired
    private CommentMapper commentMapper;

    /**
     * 动态数据访问层
     * 用于在评论添加/删除时更新动态的评论计数（commentCount），
     * 保持数据一致性
     */
    @Autowired
    private PostMapper postMapper;

    /** Redis 操作模板，用于 <b>评论列表缓存</b> 和 <b>点赞 Set</b> 操作 */
    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 用户服务 Feign 远程调用客户端
     * 用于获取评论发布者的昵称
     */
    @Autowired
    private UserServiceFeign userServiceFeign;

    /**
     * 添加评论
     * <p>
     * 业务流程：
     * <ol>
     *   <li>通过 MapStruct 将请求参数转换为 Comment 实体</li>
     *   <li>设置 userId（评论者）、初始化 likeCount=0、delFlag=0L</li>
     *   <li>通过 parentId 区分一级评论（null）和子回复（非 null）</li>
     *   <li>插入数据库，MyBatis-Plus 自动回填主键 commentId</li>
     *   <li><b>更新动态评论数</b>：调用 PostMapper.updateCommentCount 将所属动态的
     *       commentCount +1</li>
     *   <li>删除评论列表缓存和动态详情缓存（因为评论数变了）</li>
     *   <li>构建 CommentVo 返回给前端，初始化空的 replies 列表</li>
     * </ol>
     * <p>
     * 使用场景：用户对动态发表评论，或回复他人的评论。
     *
     * @param commentQuery 评论创建请求体，包含 postId（目标动态ID）、
     *                     parentId（父评论ID，可为null）、content（评论内容）
     * @param userId 评论发布者用户ID
     * @return 创建成功的评论详情 VO
     */
    @Override
    @Transactional
    public CommentVo add(CommentQuery commentQuery, Long userId) {
        // 将请求参数转换为数据库实体
        Comment comment = CommentMapping.INSTANCE.toEntity(commentQuery);
        comment.setUserId(userId);
        comment.setLikeCount(0L);   // 新评论点赞数为 0
        comment.setDelFlag(0L);     // 删除标记：0=未删除

        commentMapper.insert(comment);
        // MyBatis-Plus 自动回填 commentId

        // 更新动态的评论数：+1
        postMapper.updateCommentCount(commentQuery.getPostId(), 1);

        // 删除相关缓存：评论列表缓存 + 动态详情缓存
        // 因为评论数据变了，缓存中的评论列表和动态详情都需要刷新
        redisTemplate.delete(COMMENT_POST_PREFIX + commentQuery.getPostId());
        redisTemplate.delete("social:post:detail:" + commentQuery.getPostId());

        // 构建返回的 VO 对象
        CommentVo vo = new CommentVo();
        vo.setCommentId(comment.getCommentId());
        vo.setPostId(comment.getPostId());
        vo.setUserId(comment.getUserId());
        vo.setParentId(comment.getParentId());
        vo.setContent(comment.getContent());
        vo.setLikeCount(0L);
        vo.setCreateTime(comment.getCreateTime());
        vo.setUsername(getUsername(userId));  // 通过 Feign 获取评论者昵称
        vo.setReplies(new ArrayList<>());     // 新评论无子回复

        return vo;
    }

    /**
     * 删除评论（软删除）
     * <p>
     * 核心业务规则：
     * <ol>
     *   <li><b>存在性校验</b>：先查询评论是否存在，不存在则抛异常</li>
     *   <li><b>权限校验</b>：只有评论发布者本人才能删除，
     *       userId 不匹配则抛出"无权删除他人评论"异常</li>
     *   <li><b>软删除</b>：将 delFlag 从 0L（正常）改为 2L（已删除）</li>
     *   <li><b>更新动态评论数</b>：调用 PostMapper.updateCommentCount 将所属动态的
     *       commentCount -1</li>
     *   <li><b>缓存清理</b>：删除评论列表缓存和动态详情缓存</li>
     * </ol>
     * <p>
     * 使用场景：用户想要删除自己之前发布的某条评论。
     *
     * @param commentId 评论ID，唯一标识一条评论
     * @param userId 当前操作用户ID，用于权限校验
     * @return 受影响行数，>0 表示删除成功
     * @throws RuntimeException 评论不存在时抛出
     * @throws RuntimeException 无权删除他人评论时抛出
     */
    @Override
    public int delete(Long commentId, Long userId) {
        Comment comment = commentMapper.selectById(commentId);
        if (comment == null) {
            throw new RuntimeException("评论不存在");
        }
        // 权限校验：判断当前用户是否为评论发布者
        if (!comment.getUserId().equals(userId)) {
            throw new RuntimeException("无权删除他人评论");
        }

        // 软删除：将 delFlag 标记为 2L（已删除）
        comment.setDelFlag(2L);
        int result = commentMapper.updateById(comment);

        if (result > 0) {
            // 更新动态的评论数：-1
            postMapper.updateCommentCount(comment.getPostId(), -1);
            // 清除评论列表缓存和动态详情缓存
            redisTemplate.delete(COMMENT_POST_PREFIX + comment.getPostId());
            redisTemplate.delete("social:post:detail:" + comment.getPostId());
        }

        return result;
    }

    /**
     * 按动态ID查询评论列表（树形结构，带 Redis 缓存）
     * <p>
     * 采用 <b>Cache Aside 模式</b>，核心流程：
     * <ol>
     *   <li>先查缓存：Key = social:comment:post:{postId}</li>
     *   <li>缓存未命中则查数据库，获取该动态下的所有评论（平铺列表，含 parentId）</li>
     *   <li>如果查询结果为空，返回空列表（不写缓存，避免缓存穿透）</li>
     *   <li>批量填充所有评论的用户名（通过 Feign 调用）</li>
     *   <li><b>递归构建树形结构</b>：调用 buildCommentTree 方法</li>
     *   <li>将树形结果写入 Redis，5 分钟过期</li>
     * </ol>
     * <p>
     * 使用场景：前端渲染动态详情页的评论区，展示嵌套的评论列表。
     *
     * @param postId 动态ID，唯一标识一条动态
     * @return 评论树形结构 VO 列表，空列表表示该动态暂无评论
     */
    @Override
    public List<CommentVo> listByPostId(Long postId) {
        String key = COMMENT_POST_PREFIX + postId;
        // Cache Aside 模式：第一步，先查缓存
        Object cached = redisTemplate.opsForValue().get(key);
        if (cached != null) {
            return (List<CommentVo>) cached;
        }
        // Cache Aside 模式：第二步，缓存未命中，查询数据库
        List<CommentVo> allComments = commentMapper.selectByPostId(postId);
        if (allComments == null || allComments.isEmpty()) {
            return new ArrayList<>();  // 空结果不写缓存，避免缓存穿透
        }

        // 批量填充用户名：通过 Feign 调用 user-service 获取每个评论者的昵称
        allComments.forEach(vo -> vo.setUsername(getUsername(vo.getUserId())));

        // 递归构建评论树形结构：将平铺列表转换为层级嵌套结构
        List<CommentVo> tree = buildCommentTree(allComments);
        // Cache Aside 模式：第三步，将树形结构写入缓存
        redisTemplate.opsForValue().set(key, tree, 5, TimeUnit.MINUTES);
        return tree;
    }

    /**
     * 点赞评论
     * <p>
     * <b>Redis Set 去重策略</b>（与动态点赞逻辑一致）：
     * <ol>
     *   <li>使用 SISMEMBER 检查用户是否已点赞该评论</li>
     *   <li>已点赞则抛出异常"已经点赞过了"</li>
     *   <li>未点赞则 SADD 加入 Set，然后更新数据库 likeCount +1</li>
     * </ol>
     * <p>
     * 与动态点赞的区别：评论点赞直接操作 Comment 实体更新 likeCount，
     * 而非通过 Mapper 的自定义 SQL 方法。两种方式均可，只是实现风格不同。
     *
     * @param commentId 评论ID
     * @param userId 点赞用户ID
     * @return true 表示点赞成功
     * @throws RuntimeException 已经点赞过时抛出
     */
    @Override
    public boolean like(Long commentId, Long userId) {
        String key = LIKE_KEY_PREFIX + commentId;
        // Redis SISMEMBER：O(1) 判断用户是否已在点赞集合中
        Boolean isMember = redisTemplate.opsForSet().isMember(key, userId.toString());
        if (Boolean.TRUE.equals(isMember)) {
            throw new RuntimeException("已经点赞过了");
        }

        // Redis SADD：将用户ID加入点赞集合
        redisTemplate.opsForSet().add(key, userId.toString());

        // 更新评论点赞数：先查询实体，修改 likeCount 后再更新
        Comment comment = commentMapper.selectById(commentId);
        if (comment != null) {
            comment.setLikeCount(comment.getLikeCount() + 1);
            commentMapper.updateById(comment);
        }

        return true;
    }

    /**
     * 构建评论树形结构（递归算法）
     * <p>
     * <b>算法说明</b>：
     * <ol>
     *   <li>从平铺的评论列表中筛选出所有 <b>一级评论</b>（parentId == null），
     *       这些是树的根节点</li>
     *   <li>对每个一级评论，调用递归方法 getChildReplies 查找其所有子孙回复</li>
     *   <li>将子回复列表设置到一级评论的 replies 字段中</li>
     * </ol>
     * <p>
     * <b>时间复杂度</b>：O(n*m)，n 为一级评论数，m 为总评论数。
     * 对于评论区场景（通常单条动态评论数在几十到几百条），性能完全可以接受。
     * 如果评论量极大（万级以上），可考虑使用 Map 分组优化为 O(n) 算法。
     * <p>
     * <b>示例结构</b>：
     * <pre>
     * 一级评论A
     *   +-- 回复A1
     *   +-- 回复A2
     *        +-- 回复A2-1
     * 一级评论B
     *   +-- 回复B1
     * </pre>
     *
     * @param allComments 该动态下的所有评论（平铺列表，每条含 parentId 字段）
     * @return 树形结构的评论列表，顶层为一级评论，子回复嵌套在 replies 中
     */
    private List<CommentVo> buildCommentTree(List<CommentVo> allComments) {
        // 第一步：找出所有根评论（parentId == null，即一级评论）
        List<CommentVo> roots = allComments.stream()
                .filter(c -> c.getParentId() == null)
                .collect(Collectors.toList());

        // 第二步：为每个根评论递归挂载子回复
        for (CommentVo root : roots) {
            root.setReplies(getChildReplies(root.getCommentId(), allComments));
        }

        return roots;
    }

    /**
     * 递归获取子回复列表
     * <p>
     * <b>递归逻辑</b>：
     * <ol>
     *   <li>从 allComments 中筛选出 parentId 等于当前评论ID的所有子评论</li>
     *   <li>对每个子评论，再次递归调用自身查找其子回复（孙评论）</li>
     *   <li>递归终止条件：某条评论在 allComments 中没有以它为 parentId 的子评论</li>
     * </ol>
     * <p>
     * <b>算法特点</b>：支持无限层级嵌套（实际受数据库查询结果数量和栈深度限制）。
     * 每次递归都需要遍历整个 allComments 列表，因此时间复杂度为 O(n*m)。
     *
     * @param parentId 父评论ID，当前要查找其子回复
     * @param allComments 该动态下的所有评论列表
     * @return 指定父评论的所有直接/间接子回复树
     */
    private List<CommentVo> getChildReplies(Long parentId, List<CommentVo> allComments) {
        // 筛选出 parentId 等于当前评论ID的子评论
        List<CommentVo> children = allComments.stream()
                .filter(c -> parentId.equals(c.getParentId()))
                .collect(Collectors.toList());

        // 对每个子评论，递归查找其子回复
        for (CommentVo child : children) {
            child.setReplies(getChildReplies(child.getCommentId(), allComments));
        }

        return children;
    }

    /**
     * 通过 Feign 远程调用获取用户昵称
     * <p>
     * <b>Feign 调用降级处理</b>：
     * <ol>
     *   <li>通过 Feign 向 user-service 发起远程调用获取用户信息</li>
     *   <li>调用成功且数据完整则返回用户名</li>
     *   <li>调用失败（超时、网络异常、服务不可用）则 catch 异常</li>
     *   <li>返回 <b>降级默认值</b>："用户"+userId，保证评论区不因用户服务故障而崩溃</li>
     * </ol>
     * <p>
     * 设计考量：评论是社交服务的核心功能，不应因获取不到用户名就阻止评论展示。
     * 降级后的"用户123"格式虽然不够个性化，但能保证评论功能正常运行。
     *
     * @param userId 用户ID
     * @return 用户昵称，获取失败则返回 "用户"+userId
     */
    private String getUsername(Long userId) {
        try {
            // 通过 Feign 调用 user-service 获取用户信息
            R<Map<String, Object>> result = userServiceFeign.getUserInfo(userId);
            if (result != null && result.getData() != null && result.getData().get("username") != null) {
                return result.getData().get("username").toString();
            }
        } catch (Exception e) {
            // Feign 调用失败，静默降级，不中断业务流程
        }
        // 降级处理：返回默认用户名格式
        return "用户" + userId;
    }

}
