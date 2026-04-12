package com.mashang.scoreservice.controller;

import com.mashang.scoreservice.domain.entity.R;
import com.mashang.scoreservice.domain.query.create.AwardQuery;
import com.mashang.scoreservice.domain.vo.AwardVo;
import com.mashang.scoreservice.service.IAwardService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "奖项管理")
@RestController
@RequestMapping("/award")
public class AwardController {

    @Autowired
    private IAwardService awardService;

    @ApiOperation("添加奖项")
    @PostMapping("/add")
    public R addAward(@RequestBody @Validated AwardQuery query) {

        return R.toResult(awardService.add(query));
    }

    @ApiOperation("查询运动会奖项列表")
    @GetMapping("/list/{meetingId}")
    @ApiImplicitParam(name = "meetingId", value = "运动会id")
    public R<List<AwardVo>> awardList(@PathVariable Long meetingId) {

        return R.ok(awardService.listByMeetingId(meetingId));
    }

}
