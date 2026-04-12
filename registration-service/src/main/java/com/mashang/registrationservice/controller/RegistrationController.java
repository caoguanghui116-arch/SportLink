package com.mashang.registrationservice.controller;

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

@Api(tags = "报名服务")
@RestController
@RequestMapping("/registration")
public class RegistrationController {

    @Autowired
    private IPersonalEntryService personalEntryService;

    @Autowired
    private ITeamEntryService teamEntryService;

    @ApiOperation("个人报名")
    @PostMapping("/personal/enroll")
    public R personalEnroll(@RequestBody @Validated PersonalEntryQuery query) {
        return R.toResult(personalEntryService.enroll(query));
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

    @ApiOperation("团队报名")
    @PostMapping("/team/enroll")
    public R teamEnroll(@RequestBody @Validated TeamEntryQuery query) {
        return R.toResult(teamEntryService.enroll(query));
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
