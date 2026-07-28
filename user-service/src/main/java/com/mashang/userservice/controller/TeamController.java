package com.mashang.userservice.controller;

import com.mashang.common.common.R;
import com.mashang.userservice.domain.entity.Team;
import com.mashang.userservice.domain.query.create.TeamCreateQuery;
import com.mashang.userservice.domain.query.update.TeamUpdateQuery;
import com.mashang.userservice.service.ITeamService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 团体管理控制器
 *
 * 核心职责：
 * - 团体增删改查：管理参赛团体（如篮球队、足球队等）的基本信息
 * - 按院系筛选：查询指定院系下的所有团体
 *
 * 设计思路：
 * - 团体通过 deptId 字段关联所属院系
 * - 团体名称全局唯一，添加时进行唯一性校验
 * - 查询结果使用 Redis 缓存（Cache Aside 模式），TTL 30 分钟
 * - 写操作（增/改/删）后立即清除所有相关缓存，保证数据一致性
 * - 控制器仅做参数校验和结果封装，业务逻辑委托给 ITeamService
 */
@Api(tags = "团体管理")
@RestController
@RequestMapping("/team")
public class TeamController {

    /** 团体服务接口 */
    @Autowired
    private ITeamService teamService;

    /**
     * 添加团体
     *
     * 添加前校验团体名称是否已存在（唯一性约束）
     *
     * @param team 团体实体，需包含 teamName、deptId 等字段
     * @return 统一响应体，ok 表示添加成功
     */
    @ApiOperation("添加团体")
    @PostMapping
    public R<Void> add(@RequestBody @Validated TeamCreateQuery team) {
        return R.toResult(teamService.add(team));
    }

    /**
     * 修改团体信息
     *
     * @param team 团体实体，id 字段必填
     * @return 统一响应体，ok 表示修改成功
     */
    @ApiOperation("修改团体信息")
    @PutMapping
    public R<Void> update(@RequestBody @Validated TeamUpdateQuery team) {
        return R.toResult(teamService.update(team));
    }

    /**
     * 删除团体
     *
     * @param teamId 团体ID
     * @return 统一响应体，ok 表示删除成功
     */
    @ApiOperation("删除团体")
    @DeleteMapping("/{teamId}")
    @ApiImplicitParam(name = "teamId", value = "团体ID", required = true)
    public R<Void> delete(@PathVariable Long teamId) {
        return R.toResult(teamService.delete(teamId));
    }

    /**
     * 获取所有团体列表
     *
     * 查询使用 Redis 缓存（Cache Aside 模式），TTL 30 分钟
     *
     * @return 统一响应体，data 字段为团体列表
     */
    @ApiOperation("团体列表")
    @GetMapping
    public R<List<Team>> list() {
        return R.ok(teamService.listAll());
    }

    /**
     * 按院系查询团体
     *
     * 使用场景：在赛程编排或报名管理中，按院系筛选该院系的参赛团体
     * 查询使用 Redis 缓存，Key 格式为 team:dept:{deptId}
     *
     * @param deptId 院系ID
     * @return 统一响应体，data 字段为该院系下的团体列表
     */
    @ApiOperation("按院系查询团体")
    @GetMapping("/dept/{deptId}")
    @ApiImplicitParam(name = "deptId", value = "院系ID", required = true)
    public R<List<Team>> listByDept(@PathVariable Long deptId) {
        return R.ok(teamService.listByDeptId(deptId));
    }
}
