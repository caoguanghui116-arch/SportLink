package com.mashang.socialservice.controller;

import com.mashang.common.common.R;
import com.mashang.socialservice.domain.query.create.CommentQuery;
import com.mashang.socialservice.domain.vo.CommentVo;
import com.mashang.socialservice.service.ICommentService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 评论互动控制器
 * <p>
 * 核心职责：提供评论的添加、删除、查询和点赞等 RESTful API 接口。
 * 设计思路：
 * <ul>
 *   <li><b>评论树形结构</b>：支持多层级回复（一级评论 + 二级/三级子回复），
 *       查询时以树形结构返回，前端可直接渲染嵌套的评论列表</li>
 *   <li><b>权限控制</b>：删除操作校验当前用户是否为评论发布者，防止越权删除他人评论</li>
 *   <li><b>计数联动</b>：添加/删除评论时同步更新所属动态的 commentCount 字段</li>
 *   <li><b>点赞机制</b>：与动态点赞一致，使用 Redis Set 记录点赞用户，支持去重判断</li>
 * </ul>
 *
 * @author SportLink Team
 */
@Api(tags = "评论互动")
@RestController
@RequestMapping("/social/comment")
public class CommentController {

    /** 评论服务接口，负责评论的添加、删除、查询、点赞等业务逻辑 */
    @Autowired
    private ICommentService commentService;

    /**
     * HTTP 请求对象
     * 用于从请求头中获取 userId（由上游网关在认证后注入），实现用户身份识别
     */
    @Autowired
    private HttpServletRequest request;

    /**
     * 添加评论
     * <p>
     * 用户对某条动态发表评论或回复其他评论。通过 parentId 字段区分：
     * <ul>
     *   <li>parentId 为 null：表示这是一条一级评论（直接评论动态）</li>
     *   <li>parentId 不为 null：表示这是一条子回复（回复某条已有评论）</li>
     * </ul>
     * 评论添加成功后会自动更新所属动态的评论计数，并清除相关缓存。
     * 使用场景：用户浏览动态时发表自己的看法，或与其他用户在评论区互动。
     *
     * @param commentQuery 评论创建请求体，包含 postId（动态ID）、parentId（父评论ID，可为null）、content（评论内容）
     * @return 统一响应对象 R，data 字段为创建成功的评论详情 VO
     */
    @ApiOperation("添加评论")
    @PostMapping
    public R<CommentVo> add(@RequestBody @Validated CommentQuery commentQuery) {
        Long userId = getUserId();
        CommentVo vo = commentService.add(commentQuery, userId);
        return R.ok(vo);
    }

    /**
     * 删除评论
     * <p>
     * 软删除一条评论，仅在数据库中将 delFlag 标记为删除状态。
     * 权限校验：只有评论的发布者本人才有权限删除自己的评论。
     * 删除成功后同步更新所属动态的 commentCount（减1），并清除相关缓存。
     * 使用场景：用户想要删除自己之前发布的某条评论。
     *
     * @param commentId 评论ID，唯一标识一条评论
     * @return 统一响应对象 R，result>0 表示删除成功
     */
    @ApiOperation("删除评论")
    @DeleteMapping("/{commentId}")
    @ApiImplicitParam(name = "commentId", value = "评论ID", required = true)
    public R<Void> delete(@PathVariable Long commentId) {
        Long userId = getUserId();
        return R.toResult(commentService.delete(commentId, userId));
    }

    /**
     * 查询动态评论列表（树形结构）
     * <p>
     * 获取某条动态下的所有评论，以树形结构组织返回：
     * <ul>
     *   <li>顶层列表：所有 parentId==null 的一级评论</li>
     *   <li>每个一级评论的 replies 字段：嵌套该评论的所有子回复</li>
     *   <li>子回复中继续嵌套更下级的回复（支持无限层级）</li>
     * </ul>
     * 数据优先从 Redis 缓存读取，缓存 Key 格式：social:comment:post:{postId}。
     * 使用场景：前端渲染动态详情页的评论区，展示嵌套的评论列表。
     *
     * @param postId 动态ID，唯一标识一条动态
     * @return 评论树形结构 VO 列表，一级评论在顶层，子回复嵌套在 replies 中
     */
    @ApiOperation("查询动态评论列表(树形结构)")
    @GetMapping("/post/{postId}")
    @ApiImplicitParam(name = "postId", value = "动态ID", required = true)
    public R<List<CommentVo>> list(@PathVariable Long postId) {
        List<CommentVo> list = commentService.listByPostId(postId);
        return R.ok(list);
    }

    /**
     * 点赞评论
     * <p>
     * 用户对某条评论进行点赞操作。底层使用 Redis Set 记录点赞用户ID，
     * 已点过赞则抛出异常，防止重复点赞。点赞成功后评论的 likeCount +1。
     * 使用场景：用户觉得某条评论很有帮助或很有趣，点击点赞按钮。
     *
     * @param commentId 评论ID，唯一标识一条评论
     * @return 统一响应对象 R，附带"点赞"操作成功消息
     */
    @ApiOperation("点赞评论")
    @PostMapping("/{commentId}/like")
    @ApiImplicitParam(name = "commentId", value = "评论ID", required = true)
    public R<Void> like(@PathVariable Long commentId) {
        Long userId = getUserId();
        return R.to(commentService.like(commentId, userId), "点赞");
    }

    /**
     * 从请求头中获取当前登录用户ID
     * <p>
     * 用户身份由上游 Gateway 网关认证后注入到 HTTP Header 中，
     * 下游服务无需重复校验 Token，直接从请求头获取即可。
     *
     * @return 当前登录用户的唯一标识ID
     * @throws RuntimeException 如果请求头中不存在 userId，表示用户未登录
     */
    private Long getUserId() {
        String userIdStr = request.getHeader("userId");
        if (userIdStr == null || userIdStr.isEmpty()) {
            throw new RuntimeException("用户未登录");
        }
        return Long.valueOf(userIdStr);
    }
}
