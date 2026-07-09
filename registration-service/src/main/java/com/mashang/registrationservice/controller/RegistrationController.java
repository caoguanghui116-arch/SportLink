package com.mashang.registrationservice.controller;

import com.alibaba.csp.sentinel.annotation.SentinelResource;
import com.alibaba.csp.sentinel.slots.block.BlockException;
import com.mashang.registrationservice.domain.entity.R;
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
 * 报名服务 Controller —— 个人/团队报名的 REST API。
 *
 * 流量控制：
 * - 个人报名 & 团队报名接口标注了 @SentinelResource
 * - 报名高峰期间超过 QPS 阈值自动拒绝，返回友好提示
 * - 限流规则在 SentinelRuleConfig 中定义
 */
@Api(tags = "报名服务")
@RestController
@RequestMapping("/registration")
public class RegistrationController {

    @Autowired
    private IPersonalEntryService personalEntryService;

    @Autowired
    private ITeamEntryService teamEntryService;

    // ==================== 个人报名 ====================

    /**
     * 个人报名 —— Sentinel 限流保护。
     * 资源名 "personalEntry" 与 SentinelRuleConfig 中的规则对应
     */
    @ApiOperation("个人报名")
    @SentinelResource(value = "personalEntry", blockHandler = "enrollBlockHandler")
    @PostMapping("/personal/enroll")
    public R personalEnroll(@RequestBody @Validated PersonalEntryQuery query) {
        return R.toResult(personalEntryService.enroll(query));
    }

    /**
     * Sentinel 限流/降级后的 fallback 方法。
     * 参数签名必须与原方法完全一致，额外加上 BlockException 参数。
     */
    public R enrollBlockHandler(PersonalEntryQuery query, BlockException ex) {
        return R.fail("报名人数过多，系统正在排队处理，请稍后再试");
    }

    @ApiOperation("取消个人报名")
    @PutMapping("/personal/cancel/{entryId}")
    @ApiImplicitParam(name = "entryId", value = "报名ID")
    public R personalCancel(@PathVariable Long entryId, @RequestParam Long userId) {
        return R.toResult(personalEntryService.cancel(entryId, userId));
    }

    @ApiOperation("查询用户个人报名列表")
    @GetMapping("/personal/list/{userId}")
    public R<List<PersonalEntryVo>> personalList(@PathVariable Long userId) {
        return R.ok(personalEntryService.listByUserId(userId));
    }

    @ApiOperation("个人报名详情")
    @GetMapping("/personal/detail/{entryId}")
    public R<PersonalEntryVo> personalDetail(@PathVariable Long entryId) {
        return R.ok(personalEntryService.detail(entryId));
    }

    @ApiOperation("查询项目报名人数")
    @GetMapping("/personal/count/{itemId}")
    public R<Integer> countByItemId(@PathVariable Long itemId) {
        return R.ok(personalEntryService.countByItemId(itemId));
    }

    // ==================== 团队报名 ====================

    /**
     * 团队报名 —— Sentinel 限流保护。
     * 资源名 "teamEntry" 与 SentinelRuleConfig 中的规则对应
     */
    @ApiOperation("团队报名")
    @SentinelResource(value = "teamEntry", blockHandler = "teamEnrollBlockHandler")
    @PostMapping("/team/enroll")
    public R teamEnroll(@RequestBody @Validated TeamEntryQuery query) {
        return R.toResult(teamEntryService.enroll(query));
    }

    public R teamEnrollBlockHandler(TeamEntryQuery query, BlockException ex) {
        return R.fail("团队报名人数过多，请稍后再试");
    }

    @ApiOperation("添加团队成员")
    @PostMapping("/team/member/add")
    public R addMember(@RequestParam Long teamEntryId,
                       @RequestParam Long userId,
                       @RequestParam Long captainId) {
        return R.toResult(teamEntryService.addMember(teamEntryId, userId, captainId));
    }

    @ApiOperation("移除团队成员")
    @DeleteMapping("/team/member/{memberId}")
    public R removeMember(@PathVariable Long memberId, @RequestParam Long captainId) {
        return R.toResult(teamEntryService.removeMember(memberId, captainId));
    }

    @ApiOperation("取消团队报名")
    @PutMapping("/team/cancel/{teamEntryId}")
    public R teamCancel(@PathVariable Long teamEntryId, @RequestParam Long captainId) {
        return R.toResult(teamEntryService.cancel(teamEntryId, captainId));
    }

    @ApiOperation("查询运动会团队报名列表")
    @GetMapping("/team/list/{meetingId}")
    public R<List<TeamEntryVo>> teamList(@PathVariable Long meetingId) {
        return R.ok(teamEntryService.listByMeetingId(meetingId));
    }

    @ApiOperation("团队报名详情")
    @GetMapping("/team/detail/{teamEntryId}")
    public R<TeamEntryVo> teamDetail(@PathVariable Long teamEntryId) {
        return R.ok(teamEntryService.detail(teamEntryId));
    }

    @ApiOperation("团队成员列表")
    @GetMapping("/team/members/{teamEntryId}")
    public R<List<TeamMemberVo>> teamMembers(@PathVariable Long teamEntryId) {
        return R.ok(teamEntryService.listMembers(teamEntryId));
    }
}
