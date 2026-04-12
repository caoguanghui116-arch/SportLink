package com.mashang.eventservice.controller;

import com.mashang.eventservice.domain.entity.R;
import com.mashang.eventservice.domain.entity.ScoreRule;
import com.mashang.eventservice.service.IScoreRuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "管理端--积分规则")
@RestController
@RequestMapping("/basic/setup/score-rule")
public class ScoreRuleController {

    @Autowired
    private IScoreRuleService scoreRuleService;

    @ApiOperation("添加积分规则")
    @PostMapping("/add")
    public R add(@RequestBody ScoreRule scoreRule) {
        return R.toResult(scoreRuleService.add(scoreRule));
    }

    @ApiOperation("修改积分规则")
    @PutMapping("/update")
    public R update(@RequestBody ScoreRule scoreRule) {
        return R.toResult(scoreRuleService.update(scoreRule));
    }

    @ApiOperation("删除积分规则")
    @DeleteMapping("/delete/{ruleId}")
    @ApiImplicitParam(name = "ruleId", value = "规则ID")
    public R delete(@PathVariable Long ruleId) {
        return R.toResult(scoreRuleService.delete(ruleId));
    }

    @ApiOperation("查询运动会积分规则列表")
    @GetMapping("/list/{meetingId}")
    @ApiImplicitParam(name = "meetingId", value = "运动会ID")
    public R<List<ScoreRule>> list(@PathVariable Long meetingId) {
        return R.ok(scoreRuleService.listByMeetingId(meetingId));
    }
}
