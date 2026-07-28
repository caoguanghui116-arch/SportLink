package com.mashang.userservice.controller;

import com.mashang.userservice.domain.entity.Athlete;
import com.mashang.common.common.R;
import com.mashang.userservice.domain.query.create.AthleteCreateQuery;
import com.mashang.userservice.domain.query.update.AnnouncementUpdateQuery;
import com.mashang.userservice.service.IAthleteService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 运动员管理控制器
 *
 * 核心职责：
 * - 运动员增删改查：管理参赛运动员的基本信息
 * - 按院系筛选：查询指定院系下的所有运动员
 *
 * 设计思路：
 * - 运动员通过 deptId 字段关联所属院系
 * - 查询结果使用 Redis 缓存（Cache Aside 模式），TTL 30 分钟
 * - 写操作（增/改/删）后立即清除相关缓存，保证数据一致性
 * - 控制器仅做参数校验和结果封装，业务逻辑委托给 IAthleteService
 */
@Api(tags = "运动员管理")
@RestController
@RequestMapping("/athlete")
public class AthleteController {

    /** 运动员服务接口 */
    @Autowired
    private IAthleteService athleteService;

    /**
     * 添加运动员
     *
     * @param athlete 运动员实体，需包含 name、deptId 等字段
     * @return 统一响应体，ok 表示添加成功
     */
    @ApiOperation("添加运动员")
    @PostMapping
    public R<Void> add(@RequestBody @Validated AthleteCreateQuery athlete) {
        return R.toResult(athleteService.add(athlete));
    }

    /**
     * 修改运动员信息
     *
     * @param athlete 运动员实体，id 字段必填
     * @return 统一响应体，ok 表示修改成功
     */
    @ApiOperation("修改运动员信息")
    @PutMapping
    public R<Void> update(@RequestBody @Validated AnnouncementUpdateQuery athlete) {
        return R.toResult(athleteService.update(athlete));
    }

    /**
     * 删除运动员
     *
     * @param athleteId 运动员ID
     * @return 统一响应体，ok 表示删除成功
     */
    @ApiOperation("删除运动员")
    @DeleteMapping("/{athleteId}")
    @ApiImplicitParam(name = "athleteId", value = "运动员ID", required = true)
    public R<Void> delete(@PathVariable Long athleteId) {
        return R.toResult(athleteService.delete(athleteId));
    }

    /**
     * 获取所有运动员列表
     *
     * 查询使用 Redis 缓存（Cache Aside 模式），TTL 30 分钟
     *
     * @return 统一响应体，data 字段为运动员列表
     */
    @ApiOperation("运动员列表")
    @GetMapping
    public R<List<Athlete>> list() {
        return R.ok(athleteService.listAll());
    }

    /**
     * 按院系查询运动员
     *
     * 使用场景：在报名管理或赛程编排中，按院系筛选运动员
     * 查询使用 Redis 缓存，Key 格式为 athlete:dept:{deptId}
     *
     * @param deptId 院系ID
     * @return 统一响应体，data 字段为该院系下的运动员列表
     */
    @ApiOperation("按院系查询运动员")
    @GetMapping("/dept/{deptId}")
    @ApiImplicitParam(name = "deptId", value = "院系ID", required = true)
    public R<List<Athlete>> listByDept(@PathVariable Long deptId) {
        return R.ok(athleteService.listByDeptId(deptId));
    }
}
