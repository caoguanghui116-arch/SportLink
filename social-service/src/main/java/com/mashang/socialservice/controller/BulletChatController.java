package com.mashang.socialservice.controller;

import com.mashang.common.common.R;
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

/**
 * 弹幕互动控制器
 * <p>
 * 核心职责：提供弹幕消息的发送和查询 RESTful API 接口。
 * 设计思路：
 * <ul>
 *   <li><b>实时互动</b>：弹幕是运动会的实时互动形式，用户可以发送滚动弹幕为比赛加油助威</li>
 *   <li><b>数据量控制</b>：查询接口默认返回最新 100 条弹幕，防止数据量过大影响前端渲染性能</li>
 *   <li><b>无删除/无回复</b>：弹幕设计为轻量级的单向消息，不支持删除和回复操作，
 *       区别于评论/动态的复杂交互模式</li>
 *   <li><b>按运动会隔离</b>：每个运动会的弹幕独立存储和查询，不同运动会之间的弹幕互不可见</li>
 * </ul>
 *
 * @author SportLink Team
 */
@Api(tags = "弹幕互动")
@RestController
@RequestMapping("/social/bullet")
public class BulletChatController {

    /** 弹幕服务接口，负责弹幕的发送和查询业务逻辑 */
    @Autowired
    private IBulletChatService bulletChatService;

    /**
     * HTTP 请求对象
     * 用于从请求头中获取 userId（由上游网关在认证后注入），实现用户身份识别。
     * 弹幕发送需要登录验证，防止匿名垃圾弹幕。
     */
    @Autowired
    private HttpServletRequest request;

    /**
     * 发送弹幕
     * <p>
     * 用户向指定运动会发送一条弹幕消息，弹幕内容支持文字和简单的样式标记。
     * 发送成功后返回弹幕 VO 对象，包含自动生成的弹幕ID、发送时间、用户名等信息。
     * 使用场景：用户在观看比赛直播时发送"加油XXX""！""太厉害了"等弹幕消息。
     *
     * @param bulletChatQuery 弹幕发送请求体，包含 meetingId（运动会ID）、content（弹幕内容）、
     *                        color（弹幕颜色，可选）、position（弹幕位置，可选）
     * @return 统一响应对象 R，data 字段为发送成功的弹幕详情 VO
     */
    @ApiOperation("发送弹幕")
    @PostMapping
    public R<BulletChatVo> send(@RequestBody @Validated BulletChatQuery bulletChatQuery) {
        Long userId = getUserId();
        BulletChatVo vo = bulletChatService.send(bulletChatQuery, userId);
        return R.ok(vo);
    }

    /**
     * 查询运动会弹幕列表（最新100条）
     * <p>
     * 获取指定运动会下最近发送的弹幕消息，最多返回 100 条，按时间倒序排列。
     * 限制数量的原因是弹幕场景数据量大、刷新频率高，全量返回会影响前端渲染性能
     * 并增加网络传输负担。100 条已足够覆盖用户屏幕上的弹幕滚动显示。
     * 使用场景：用户进入比赛直播页面时加载最近的弹幕消息，或者前端轮询获取新弹幕。
     *
     * @param meetingId 运动会ID，唯一标识一届运动会
     * @return 该运动会最新 100 条弹幕的 VO 列表
     */
    @ApiOperation("查询运动会弹幕列表(最新100条)")
    @GetMapping("/meeting/{meetingId}")
    @ApiImplicitParam(name = "meetingId", value = "运动会ID", required = true)
    public R<List<BulletChatVo>> list(@PathVariable Long meetingId) {
        List<BulletChatVo> list = bulletChatService.listByMeetingId(meetingId);
        return R.ok(list);
    }

    /**
     * 从请求头中获取当前登录用户ID
     * <p>
     * 弹幕发送需要登录验证，防止匿名用户发送垃圾弹幕。
     * userId 由网关层在 JWT 认证通过后注入到请求头中。
     *
     * @return 当前登录用户的唯一标识ID
     * @throws RuntimeException 如果请求头中不存在 userId，表示用户未登录，拒绝发送弹幕
     */
    private Long getUserId() {
        String userIdStr = request.getHeader("userId");
        if (userIdStr == null || userIdStr.isEmpty()) {
            throw new RuntimeException("用户未登录");
        }
        return Long.valueOf(userIdStr);
    }
}
