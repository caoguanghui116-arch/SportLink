package com.mashang.scoreservice.controller;

import com.mashang.scoreservice.domain.entity.R;
import com.mashang.scoreservice.domain.query.create.PersonalResultQuery;
import com.mashang.scoreservice.domain.query.create.TeamResultQuery;
import com.mashang.scoreservice.domain.vo.PersonalResultVo;
import com.mashang.scoreservice.domain.vo.RankingVo;
import com.mashang.scoreservice.domain.vo.TeamResultVo;
import com.mashang.scoreservice.service.IPersonalResultService;
import com.mashang.scoreservice.service.ITeamResultService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "成绩服务")
@RestController
@RequestMapping("/score")
public class ScoreController {

    @Autowired
    private IPersonalResultService personalResultService;

    @Autowired
    private ITeamResultService teamResultService;

    @ApiOperation("个人成绩录入")
    @PostMapping("/personal/entry")
    public R personalEntry(@RequestBody @Validated PersonalResultQuery query) {

        return R.toResult(personalResultService.entry(query));
    }

    @ApiOperation("团体成绩录入")
    @PostMapping("/team/entry")
    public R teamEntry(@RequestBody @Validated TeamResultQuery query) {

        return R.toResult(teamResultService.entry(query));
    }

    @ApiOperation("批量成绩导入")
    @PostMapping("/batch/import")
    public R batchImport(@RequestBody @Validated List<PersonalResultQuery> queryList) {

        return R.toResult(personalResultService.batchImport(queryList));
    }

    @ApiOperation("查询项目个人成绩列表")
    @GetMapping("/personal/list/{itemId}")
    @ApiImplicitParam(name = "itemId", value = "项目id")
    public R<List<PersonalResultVo>> personalList(@PathVariable Long itemId) {

        return R.ok(personalResultService.listByItemId(itemId));
    }

    @ApiOperation("查询项目团体成绩列表")
    @GetMapping("/team/list/{itemId}")
    @ApiImplicitParam(name = "itemId", value = "项目id")
    public R<List<TeamResultVo>> teamList(@PathVariable Long itemId) {

        return R.ok(teamResultService.listByItemId(itemId));
    }

    @ApiOperation("查询排行榜(Redis缓存)")
    @GetMapping("/ranking/{meetingId}")
    @ApiImplicitParam(name = "meetingId", value = "运动会id")
    public R<List<RankingVo>> ranking(@PathVariable Long meetingId) {

        return R.ok(personalResultService.getRanking(meetingId));
    }

    @ApiOperation("查询用户个人成绩")
    @GetMapping("/personal/user/{userId}")
    @ApiImplicitParam(name = "userId", value = "用户id")
    public R<List<PersonalResultVo>> userScores(@PathVariable Long userId) {

        return R.ok(personalResultService.listByUserId(userId));
    }

}
