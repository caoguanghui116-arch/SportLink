package com.mashang.registrationservice.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.mashang.common.common.R;
import com.mashang.registrationservice.domain.query.create.PersonalEntryQuery;
import com.mashang.registrationservice.domain.query.create.TeamEntryQuery;
import com.mashang.registrationservice.domain.vo.PersonalEntryVo;
import com.mashang.registrationservice.domain.vo.TeamEntryVo;
import com.mashang.registrationservice.domain.vo.TeamMemberVo;
import com.mashang.registrationservice.service.IPersonalEntryService;
import com.mashang.registrationservice.service.ITeamEntryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 报名服务控制器 —— 提供个人报名与团队报名的 REST API
 *
 * <p>核心职责：
 * <ul>
 *   <li><b>个人报名</b>：报名、取消报名、查询报名列表、报名详情、统计项目报名人数</li>
 *   <li><b>团队报名</b>：报名、取消报名、添加/移除队员、查询报名列表、报名详情、队员列表</li>
 * </ul>
 *
 * <p>设计要点：
 * <ul>
 *   <li><b>流量控制</b>：个人报名和团队报名接口使用 {@link SentinelResource @SentinelResource} 注解，
 *       搭配自定义 blockHandler 方法，在报名高峰流量超过 Sentinel QPS 阈值时自动降级，
 *       返回友好提示"报名人数过多，系统正在排队处理，请稍后再试"，避免服务雪崩</li>
 *   <li><b>参数校验</b>：报名接口使用 {@code @Validated} 对请求体做 JSR-303 校验，
 *       确保必填字段（如用户ID、项目ID、运动会ID）不为空</li>
 *   <li><b>权限控制</b>：队长操作（添加/移除队员、取消团队报名）需传入 captainId 做身份校验，
 *       由 Service 层判断是否为该团队的队长</li>
 *   <li><b>接口路径</b>：统一挂载在 {@code /registration} 下，
 *       个人报名子路径为 {@code /personal}，团队报名子路径为 {@code /team}</li>
 * </ul>
 *
 * @author mashang
 */
@Api(tags = "报名服务")
@RestController
@RequestMapping("/registration")
public class RegistrationController {

    /** 个人报名业务服务接口 */
    @Autowired
    private IPersonalEntryService personalEntryService;

    /** 团队报名业务服务接口 */
    @Autowired
    private ITeamEntryService teamEntryService;

    // ==================== 个人报名 ====================

    /**
     * 个人报名
     *
     * <p>运动员以个人身份报名参加某个运动会项目。
     * 接口受 Sentinel 流量控制保护，报名高峰超过 QPS 阈值时触发
     * {@link #enrollBlockHandler(PersonalEntryQuery, BlockException)} 降级逻辑。
     * 请求体中的 {@link PersonalEntryQuery} 经 {@code @Validated} 校验，确保用户ID、
     * 项目ID、运动会ID 等必填字段不为空。
     *
     * @param query 个人报名参数对象，包含 userId、itemId、meetingId 等
     * @return 统一响应体，报名成功时 code=200，被限流时返回友好提示
     */
    @ApiOperation("个人报名")
    @SentinelResource(value = "personalEntry", blockHandler = "enrollBlockHandler")
    @PostMapping("/personal")
    public R<Void> personalEnroll(@RequestBody @Validated PersonalEntryQuery query) {
        return R.toResult(personalEntryService.enroll(query));
    }

    /**
     * 个人报名限流降级处理
     *
     * <p>当个人报名接口请求量超过 Sentinel 设定 QPS 阈值时，由 Sentinel 框架自动调用此方法，
     * 返回友好提示信息，避免大量请求直接打到 Service 层导致服务不可用。
     *
     * @param query 原始报名请求参数
     * @param ex    限流异常对象（BlockException）
     * @return 统一错误响应体，提示用户稍后再试
     */
    public R<Void> enrollBlockHandler(PersonalEntryQuery query, BlockException ex) {
        return R.fail("报名人数过多，系统正在排队处理，请稍后再试");
    }

    /**
     * 取消个人报名
     *
     * @param entryId 报名记录ID（路径参数，必填）
     * @param userId  当前操作用户ID（请求参数），用于校验操作人是否为报名者本人
     * @return 统一响应体，取消成功时 code=200
     */
    @ApiOperation("取消个人报名")
    @PutMapping("/personal/{entryId}/cancel")
    @ApiImplicitParam(name = "entryId", value = "报名ID", required = true)
    public R<Void> personalCancel(@PathVariable Long entryId, @RequestParam Long userId) {
        return R.toResult(personalEntryService.cancel(entryId, userId));
    }

    /**
     * 查询指定用户的个人报名列表
     *
     * <p>返回用户在当前运动会中的所有个人报名记录，前端在"我的报名"页面调用。
     *
     * @param userId 用户ID（路径参数，必填）
     * @return 统一响应体，data 为 {@link PersonalEntryVo} 列表
     */
    @ApiOperation("查询用户个人报名列表")
    @GetMapping("/personal/user/{userId}")
    @ApiImplicitParam(name = "userId", value = "用户ID", required = true)
    public R<List<PersonalEntryVo>> personalList(@PathVariable Long userId) {
        return R.ok(personalEntryService.listByUserId(userId));
    }

    /**
     * 查询个人报名详情
     *
     * @param entryId 报名记录ID（路径参数，必填）
     * @return 统一响应体，data 为 {@link PersonalEntryVo} 对象
     */
    @ApiOperation("个人报名详情")
    @GetMapping("/personal/{entryId}")
    @ApiImplicitParam(name = "entryId", value = "报名ID", required = true)
    public R<PersonalEntryVo> personalDetail(@PathVariable Long entryId) {
        return R.ok(personalEntryService.detail(entryId));
    }

    /**
     * 查询指定项目的报名人数
     *
     * <p>用于前端展示每个项目当前的报名热度，也可作为报名上限校验的辅助数据。
     *
     * @param itemId 项目ID（路径参数，必填）
     * @return 统一响应体，data 为当前报名人数（Integer）
     */
    @ApiOperation("查询项目报名人数")
    @GetMapping("/personal/item/{itemId}/count")
    @ApiImplicitParam(name = "itemId", value = "项目ID", required = true)
    public R<Integer> countByItemId(@PathVariable Long itemId) {
        return R.ok(personalEntryService.countByItemId(itemId));
    }

    // ==================== 团队报名 ====================

    /**
     * 团队报名
     *
     * <p>队长以团队身份报名参加团队项目，报名成功后队长成为团队的第一位成员。
     * 接口受 Sentinel 流量控制保护，报名高峰超过 QPS 阈值时触发
     * {@link #teamEnrollBlockHandler(TeamEntryQuery, BlockException)} 降级逻辑。
     *
     * @param query 团队报名参数对象，包含 teamName、captainId、itemId、meetingId 等
     * @return 统一响应体，报名成功时 code=200，被限流时返回友好提示
     */
    @ApiOperation("团队报名")
    @SentinelResource(value = "teamEntry", blockHandler = "teamEnrollBlockHandler")
    @PostMapping("/team")
    public R<Void> teamEnroll(@RequestBody @Validated TeamEntryQuery query) {
        return R.toResult(teamEntryService.enroll(query));
    }

    /**
     * 团队报名限流降级处理
     *
     * <p>当团队报名接口请求量超过 Sentinel 设定 QPS 阈值时自动降级，返回友好提示。
     *
     * @param query 原始报名请求参数
     * @param ex    限流异常对象（BlockException）
     * @return 统一错误响应体，提示用户稍后再试
     */
    public R<Void> teamEnrollBlockHandler(TeamEntryQuery query, BlockException ex) {
        return R.fail("团队报名人数过多，请稍后再试");
    }

    /**
     * 添加团队成员
     *
     * <p>队长操作，将指定用户添加为团队成员。Service 层会校验操作者（captainId）是否为该团队队长。
     *
     * @param teamEntryId 团队报名ID（请求参数，必填）
     * @param userId      待添加的用户ID（请求参数，必填）
     * @param captainId   队长用户ID（请求参数），用于权限校验
     * @return 统一响应体，添加成功时 code=200
     */
    @ApiOperation("添加团队成员")
    @PostMapping("/team/member")
    @ApiImplicitParam(name = "teamEntryId", value = "团队报名ID", required = true)
    public R<Void> addMember(@RequestParam Long teamEntryId,
                          @RequestParam Long userId,
                          @RequestParam Long captainId) {
        return R.toResult(teamEntryService.addMember(teamEntryId, userId, captainId));
    }

    /**
     * 移除团队成员
     *
     * <p>队长操作，将指定成员从团队中移除。Service 层会校验操作者是否为该团队队长。
     *
     * @param memberId  团队成员记录ID（路径参数，必填）
     * @param captainId 队长用户ID（请求参数），用于权限校验
     * @return 统一响应体，移除成功时 code=200
     */
    @ApiOperation("移除团队成员")
    @DeleteMapping("/team/member/{memberId}")
    @ApiImplicitParam(name = "memberId", value = "成员ID", required = true)
    public R<Void> removeMember(@PathVariable Long memberId, @RequestParam Long captainId) {
        return R.toResult(teamEntryService.removeMember(memberId, captainId));
    }

    /**
     * 取消团队报名
     *
     * <p>队长操作，取消整个团队的报名。Service 层会校验操作者是否为该团队队长。
     *
     * @param teamEntryId 团队报名ID（路径参数，必填）
     * @param captainId   队长用户ID（请求参数），用于权限校验
     * @return 统一响应体，取消成功时 code=200
     */
    @ApiOperation("取消团队报名")
    @PutMapping("/team/{teamEntryId}/cancel")
    @ApiImplicitParam(name = "teamEntryId", value = "团队报名ID", required = true)
    public R<Void> teamCancel(@PathVariable Long teamEntryId, @RequestParam Long captainId) {
        return R.toResult(teamEntryService.cancel(teamEntryId, captainId));
    }

    /**
     * 查询指定运动会的团队报名列表
     *
     * <p>按运动会ID筛选所有团队的报名信息，适用于运动会管理员查看团队报名概况。
     *
     * @param meetingId 运动会ID（路径参数，必填）
     * @return 统一响应体，data 为 {@link TeamEntryVo} 列表
     */
    @ApiOperation("查询运动会团队报名列表")
    @GetMapping("/team/meeting/{meetingId}")
    @ApiImplicitParam(name = "meetingId", value = "运动会ID", required = true)
    public R<List<TeamEntryVo>> teamList(@PathVariable Long meetingId) {
        return R.ok(teamEntryService.listByMeetingId(meetingId));
    }

    /**
     * 查询团队报名详情
     *
     * @param teamEntryId 团队报名ID（路径参数，必填）
     * @return 统一响应体，data 为 {@link TeamEntryVo} 对象
     */
    @ApiOperation("团队报名详情")
    @GetMapping("/team/{teamEntryId}")
    @ApiImplicitParam(name = "teamEntryId", value = "团队报名ID", required = true)
    public R<TeamEntryVo> teamDetail(@PathVariable Long teamEntryId) {
        return R.ok(teamEntryService.detail(teamEntryId));
    }

    /**
     * 查询团队成员列表
     *
     * @param teamEntryId 团队报名ID（路径参数，必填）
     * @return 统一响应体，data 为 {@link TeamMemberVo} 列表
     */
    @ApiOperation("团队成员列表")
    @GetMapping("/team/{teamEntryId}/members")
    @ApiImplicitParam(name = "teamEntryId", value = "团队报名ID", required = true)
    public R<List<TeamMemberVo>> teamMembers(@PathVariable Long teamEntryId) {
        return R.ok(teamEntryService.listMembers(teamEntryId));
    }
}
