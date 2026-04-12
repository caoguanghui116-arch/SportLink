package com.mashang.userservice.controller;

import com.mashang.userservice.domain.entity.Athlete;
import com.mashang.userservice.domain.entity.R;
import com.mashang.userservice.service.IAthleteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "运动员管理")
@RestController
@RequestMapping("/usercenter/athlete")
public class AthleteController {

    @Autowired
    private IAthleteService athleteService;

    @ApiOperation("添加运动员")
    @PostMapping("/add")
    public R add(@RequestBody Athlete athlete) {
        return R.toResult(athleteService.add(athlete));
    }

    @ApiOperation("修改运动员信息")
    @PutMapping("/update")
    public R update(@RequestBody Athlete athlete) {
        return R.toResult(athleteService.update(athlete));
    }

    @ApiOperation("删除运动员")
    @DeleteMapping("/delete/{athleteId}")
    @ApiImplicitParam(name = "athleteId", value = "运动员ID")
    public R delete(@PathVariable Long athleteId) {
        return R.toResult(athleteService.delete(athleteId));
    }

    @ApiOperation("运动员列表")
    @GetMapping("/list")
    public R<List<Athlete>> list() {
        return R.ok(athleteService.listAll());
    }

    @ApiOperation("按院系查询运动员")
    @GetMapping("/list/{deptId}")
    @ApiImplicitParam(name = "deptId", value = "院系ID")
    public R<List<Athlete>> listByDept(@PathVariable Long deptId) {
        return R.ok(athleteService.listByDeptId(deptId));
    }
}
