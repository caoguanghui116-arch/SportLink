package com.mashang.socialservice.controller;

import com.mashang.socialservice.domain.entity.R;
import com.mashang.socialservice.domain.query.create.BulletChatQuery;
import com.mashang.socialservice.domain.vo.BulletChatVo;
import com.mashang.socialservice.service.IBulletChatService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

@Api(tags = "弹幕互动")
@RestController
@RequestMapping("/social/bullet")
public class BulletChatController {

    @Autowired
    private IBulletChatService bulletChatService;

    @Autowired
    private HttpServletRequest request;

    @ApiOperation("发送弹幕")
    @PostMapping("/send")
    public R<BulletChatVo> send(@RequestBody @Validated BulletChatQuery bulletChatQuery) {
        Long userId = getUserId();
        BulletChatVo vo = bulletChatService.send(bulletChatQuery, userId);
        return R.ok(vo);
    }

    @ApiOperation("查询运动会弹幕列表(最新100条)")
    @GetMapping("/list/{meetingId}")
    @ApiImplicitParam(name = "meetingId", value = "运动会id")
    public R<List<BulletChatVo>> list(@PathVariable Long meetingId) {
        List<BulletChatVo> list = bulletChatService.listByMeetingId(meetingId);
        return R.ok(list);
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
