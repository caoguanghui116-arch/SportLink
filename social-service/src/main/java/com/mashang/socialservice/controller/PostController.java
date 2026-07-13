package com.mashang.socialservice.controller;

import com.mashang.common.common.R;
import com.mashang.socialservice.domain.query.create.PostQuery;
import com.mashang.socialservice.domain.vo.PostVo;
import com.mashang.socialservice.service.IPostService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 社区动态控制器
 * <p>
 * 核心职责：提供社区动态（帖子）的发布、删除、查询、点赞/取消点赞等 RESTful API 接口。
 * 设计思路：
 * <ul>
 *   <li><b>用户身份识别</b>：从 HTTP 请求头中获取 userId，网关层已完成认证并注入请求头</li>
 *   <li><b>权限控制</b>：删除操作校验当前用户是否为动态发布者，防止越权删除</li>
 *   <li><b>点赞机制</b>：使用 Redis Set 数据结构记录点赞用户，支持高效的去重判断和点赞/取消操作</li>
 *   <li><b>动态详情</b>：包含动态内容及其下的评论树形结构，一次请求获取完整信息</li>
 * </ul>
 *
 * @author SportLink Team
 */
@Api(tags = "社区动态")
@RestController
@RequestMapping("/social/post")
public class PostController {

    /** 动态服务接口，负责动态的发布、删除、查询、点赞/取消点赞等业务逻辑 */
    @Autowired
    private IPostService postService;

    /**
     * HTTP 请求对象
     * 用于从请求头中获取 userId（由上游网关在认证后注入），实现用户身份识别
     */
    @Autowired
    private HttpServletRequest request;

    /**
     * 发布动态
     * <p>
     * 用户在社区中发布一条新的动态（帖子），内容可以包含文字和图片链接。
     * 发布成功后返回完整的动态信息 VO 对象，包含自动生成的动态ID和创建时间。
     * 使用场景：用户在运动会社区中分享比赛精彩瞬间、加油助威等内容。
     *
     * @param postQuery 动态发布请求体，包含 meetingId（运动会ID）、content（内容）、imageUrl（图片链接）
     * @return 统一响应对象 R，data 字段为发布成功的动态详情 VO
     */
    @ApiOperation("发布动态")
    @PostMapping
    public R<PostVo> publish(@RequestBody @Validated PostQuery postQuery) {
        Long userId = getUserId();
        PostVo vo = postService.publish(postQuery, userId);
        return R.ok(vo);
    }

    /**
     * 删除动态
     * <p>
     * 软删除一条动态，仅在数据库中将 delFlag 标记为删除状态，不物理删除记录。
     * 权限校验：只有动态的发布者本人才有权限删除，管理员功能可后续扩展。
     * 使用场景：用户想要删除自己之前发布的某条动态。
     *
     * @param postId 动态ID，唯一标识一条动态
     * @return 统一响应对象 R，result>0 表示删除成功
     */
    @ApiOperation("删除动态")
    @DeleteMapping("/{postId}")
    @ApiImplicitParam(name = "postId", value = "动态ID", required = true)
    public R<Void> delete(@PathVariable Long postId) {
        Long userId = getUserId();
        return R.toResult(postService.delete(postId, userId));
    }

    /**
     * 查询运动会动态列表（按时间倒序）
     * <p>
     * 按运动会 ID 查询该运动会下所有用户发布的动态，按创建时间倒序排列（最新先展示）。
     * 数据优先从 Redis 缓存读取，支持 5 分钟的缓存有效期。
     * 使用场景：进入运动会社区首页，展示所有参赛者发布的动态信息流。
     *
     * @param meetingId 运动会ID，唯一标识一届运动会
     * @return 该运动会下所有动态的 VO 列表，按时间倒序排列
     */
    @ApiOperation("查询运动会动态列表(按时间倒序)")
    @GetMapping("/meeting/{meetingId}")
    @ApiImplicitParam(name = "meetingId", value = "运动会ID", required = true)
    public R<List<PostVo>> list(@PathVariable Long meetingId) {
        List<PostVo> list = postService.listByMeetingId(meetingId);
        return R.ok(list);
    }

    /**
     * 动态详情（含评论树形结构）
     * <p>
     * 查看某条动态的完整详情，包含动态本身的内容和该动态下的所有评论。
     * 评论数据以树形结构返回：一级评论在顶层，子回复嵌套在一级评论的 replies 字段中。
     * 使用场景：用户点击某条动态进入详情页面，查看完整内容和所有评论。
     *
     * @param postId 动态ID，唯一标识一条动态
     * @return 动态详情 VO，包含动态信息、发布者信息以及评论树形列表
     */
    @ApiOperation("动态详情(含评论)")
    @GetMapping("/{postId}")
    @ApiImplicitParam(name = "postId", value = "动态ID", required = true)
    public R<PostVo> detail(@PathVariable Long postId) {
        PostVo vo = postService.detail(postId);
        return R.ok(vo);
    }

    /**
     * 点赞动态
     * <p>
     * 用户对某条动态进行点赞操作。底层使用 Redis Set 记录点赞用户ID集合，
     * 如果用户已经点赞过则抛出异常提示"已经点赞过了"，防止重复点赞。
     * 点赞成功后动态的点赞计数 +1。
     * 使用场景：用户阅读了一条精彩的动态，点击点赞按钮表达认同。
     *
     * @param postId 动态ID，唯一标识一条动态
     * @return 统一响应对象 R，附带"点赞"操作成功消息
     */
    @ApiOperation("点赞动态")
    @PostMapping("/{postId}/like")
    @ApiImplicitParam(name = "postId", value = "动态ID", required = true)
    public R<Void> like(@PathVariable Long postId) {
        Long userId = getUserId();
        return R.to(postService.like(postId, userId), "点赞");
    }

    /**
     * 取消点赞
     * <p>
     * 用户取消对某条动态的点赞。底层从 Redis Set 中移除该用户ID，
     * 移除成功后动态的点赞计数 -1。如果用户之前未点赞，则操作无效返回 false。
     * 使用场景：用户误点赞或改变主意，点击取消点赞按钮。
     *
     * @param postId 动态ID，唯一标识一条动态
     * @return 统一响应对象 R，附带"取消点赞"操作消息，true=成功/false=未点赞无需取消
     */
    @ApiOperation("取消点赞")
    @DeleteMapping("/{postId}/like")
    @ApiImplicitParam(name = "postId", value = "动态ID", required = true)
    public R<Void> unlike(@PathVariable Long postId) {
        Long userId = getUserId();
        return R.to(postService.unlike(postId, userId), "取消点赞");
    }

    /**
     * 从请求头中获取当前登录用户ID
     * <p>
     * 设计说明：用户认证由网关层（Gateway）统一完成，认证通过后网关将 userId
     * 注入到 HTTP 请求头中，下游微服务直接从请求头读取即可，无需再次解析 Token。
     * 如果请求头中不存在 userId，则抛出运行时异常，表示用户未登录或认证已失效。
     *
     * @return 当前登录用户的唯一标识ID
     * @throws RuntimeException 如果请求头中不存在 userId 或值为空
     */
    private Long getUserId() {
        String userIdStr = request.getHeader("userId");
        if (userIdStr == null || userIdStr.isEmpty()) {
            throw new RuntimeException("用户未登录");
        }
        return Long.valueOf(userIdStr);
    }
}
