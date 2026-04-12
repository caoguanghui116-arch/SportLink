package com.mashang.socialservice.controller;

import com.mashang.socialservice.domain.entity.R;
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

@Api(tags = "评论互动")
@RestController
@RequestMapping("/social/comment")
public class CommentController {

    @Autowired
    private ICommentService commentService;

    @Autowired
    private HttpServletRequest request;

    @ApiOperation("添加评论")
    @PostMapping("/add")
    public R<CommentVo> add(@RequestBody @Validated CommentQuery commentQuery) {
        Long userId = getUserId();
        CommentVo vo = commentService.add(commentQuery, userId);
        return R.ok(vo);
    }

    @ApiOperation("删除评论")
    @DeleteMapping("/delete/{commentId}")
    @ApiImplicitParam(name = "commentId", value = "评论id")
    public R delete(@PathVariable Long commentId) {
        Long userId = getUserId();
        return R.toResult(commentService.delete(commentId, userId));
    }

    @ApiOperation("查询动态评论列表(树形结构)")
    @GetMapping("/list/{postId}")
    @ApiImplicitParam(name = "postId", value = "动态id")
    public R<List<CommentVo>> list(@PathVariable Long postId) {
        List<CommentVo> list = commentService.listByPostId(postId);
        return R.ok(list);
    }

    @ApiOperation("点赞评论")
    @PostMapping("/like/{commentId}")
    @ApiImplicitParam(name = "commentId", value = "评论id")
    public R like(@PathVariable Long commentId) {
        Long userId = getUserId();
        return R.to(commentService.like(commentId, userId), "点赞");
    }

    /**
     * 从请求头获取用户ID
     */
    private Long getUserId() {
        String userIdStr = request.getHeader("userId");
        if (userIdStr == null || userIdStr.isEmpty()) {
            throw new RuntimeException("用户未登录");
        }
        return Long.valueOf(userIdStr);
    }

}
