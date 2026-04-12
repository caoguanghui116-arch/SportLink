package com.mashang.userservice.controller;

import com.mashang.userservice.domain.entity.R;
import com.mashang.userservice.domain.entity.Team;
import com.mashang.userservice.service.ITeamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Api(tags = "团体管理")
@RestController
@RequestMapping("/usercenter/team")
public class TeamController {

    @Autowired
    private ITeamService teamService;

    @ApiOperation("添加团体")
    @PostMapping("/add")
    public R add(@RequestBody Team team) {
        return R.toResult(teamService.add(team));
    }

    @ApiOperation("修改团体信息")
    @PutMapping("/update")
    public R update(@RequestBody Team team) {
        return R.toResult(teamService.update(team));
    }

    @ApiOperation("删除团体")
    @DeleteMapping("/delete/{teamId}")
    @ApiImplicitParam(name = "teamId", value = "团体ID")
    public R delete(@PathVariable Long teamId) {
        return R.toResult(teamService.delete(teamId));
    }

    @ApiOperation("团体列表")
    @GetMapping("/list")
    public R<List<Team>> list() {
        return R.ok(teamService.listAll());
    }

    @ApiOperation("按院系查询团体")
    @GetMapping("/list/{deptId}")
    @ApiImplicitParam(name = "deptId", value = "院系ID")
    public R<List<Team>> listByDept(@PathVariable Long deptId) {
        return R.ok(teamService.listByDeptId(deptId));
    }
}
