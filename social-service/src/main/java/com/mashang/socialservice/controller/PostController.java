package com.mashang.socialservice.controller;

import com.mashang.socialservice.domain.entity.R;
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

@Api(tags = "社区动态")
@RestController
@RequestMapping("/social/post")
public class PostController {

    @Autowired
    private IPostService postService;

    @Autowired
    private HttpServletRequest request;

    @ApiOperation("发布动态")
    @PostMapping("/publish")
    public R<PostVo> publish(@RequestBody @Validated PostQuery postQuery) {
        Long userId = getUserId();
        PostVo vo = postService.publish(postQuery, userId);
        return R.ok(vo);
    }

    @ApiOperation("删除动态")
    @DeleteMapping("/delete/{postId}")
    @ApiImplicitParam(name = "postId", value = "动态id")
    public R delete(@PathVariable Long postId) {
        Long userId = getUserId();
        return R.toResult(postService.delete(postId, userId));
    }

    @ApiOperation("查询运动会动态列表(按时间倒序)")
    @GetMapping("/list/{meetingId}")
    @ApiImplicitParam(name = "meetingId", value = "运动会id")
    public R<List<PostVo>> list(@PathVariable Long meetingId) {
        List<PostVo> list = postService.listByMeetingId(meetingId);
        return R.ok(list);
    }

    @ApiOperation("动态详情(含评论)")
    @GetMapping("/detail/{postId}")
    @ApiImplicitParam(name = "postId", value = "动态id")
    public R<PostVo> detail(@PathVariable Long postId) {
        PostVo vo = postService.detail(postId);
        return R.ok(vo);
    }

    @ApiOperation("点赞动态")
    @PostMapping("/like/{postId}")
    @ApiImplicitParam(name = "postId", value = "动态id")
    public R like(@PathVariable Long postId) {
        Long userId = getUserId();
        return R.to(postService.like(postId, userId), "点赞");
    }

    @ApiOperation("取消点赞")
    @DeleteMapping("/like/{postId}")
    @ApiImplicitParam(name = "postId", value = "动态id")
    public R unlike(@PathVariable Long postId) {
        Long userId = getUserId();
        return R.to(postService.unlike(postId, userId), "取消点赞");
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
