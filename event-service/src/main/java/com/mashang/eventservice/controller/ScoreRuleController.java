package com.mashang.eventservice.controller;

import com.mashang.common.common.R;
import com.mashang.eventservice.domain.entity.ScoreRule;
import com.mashang.eventservice.service.IScoreRuleService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 积分规则控制器
 * <p>
 * 核心职责：管理运动会的名次积分规则，提供规则的增删改查 RESTful API。
 * <p>
 * 业务背景：在运动会中，不同名次对应不同的积分值（如第一名 10 分、第二名 8 分），
 * 系统需要灵活配置这些规则以适配不同赛事的积分体系。
 * <p>
 * 设计思路：每条积分规则关联一个 meetingId（运动会）和一个 rank（名次），
 * 同一运动会下名次不可重复（Service 层进行唯一性校验）。
 * 查询结果按 rank 升序排列（第一名在前），缓存 key 以 meetingId 为维度。
 *
 * @author mashang
 */
@Api(tags = "积分规则")
@RestController
@RequestMapping("/basic/setup/score-rule")
public class ScoreRuleController {

    /** 积分规则服务接口 —— 负责规则的增删改查及缓存管理 */
    @Autowired
    private IScoreRuleService scoreRuleService;

    /**
     * 添加积分规则
     * <p>
     * 为指定运动会新增一条名次积分规则。
     * Service 层会校验同一运动会下该名次的规则是否已存在，
     * 校验通过后写入 DB 并删除该运动会对应的 Redis 缓存。
     *
     * @param scoreRule 积分规则实体对象，需包含 meetingId、rank、score 等字段
     * @return R&lt;Void&gt; 操作结果
     */
    @ApiOperation("添加积分规则")
    @PostMapping
    public R<Void> add(@RequestBody @Validated ScoreRule scoreRule) {
        return R.toResult(scoreRuleService.add(scoreRule));
    }

    /**
     * 修改积分规则
     * <p>
     * 根据规则 ID 更新积分规则（如修改积分值）。
     * 更新成功后清除对应运动会维度的 Redis 缓存。
     *
     * @param scoreRule 积分规则实体对象，必须包含 ruleId 用于定位记录
     * @return R&lt;Void&gt; 操作结果
     */
    @ApiOperation("修改积分规则")
    @PutMapping
    public R<Void> update(@RequestBody @Validated ScoreRule scoreRule) {
        return R.toResult(scoreRuleService.update(scoreRule));
    }

    /**
     * 删除积分规则
     * <p>
     * 根据规则 ID 物理删除积分规则记录。
     * Service 层会先查出该规则对应的运动会 ID，删除 DB 记录后再清除该运动会缓存。
     *
     * @param ruleId 规则 ID，通过路径参数传递
     * @return R&lt;Void&gt; 操作结果
     */
    @ApiOperation("删除积分规则")
    @DeleteMapping("/{ruleId}")
    @ApiImplicitParam(name = "ruleId", value = "规则ID", required = true)
    public R<Void> delete(@PathVariable Long ruleId) {
        return R.toResult(scoreRuleService.delete(ruleId));
    }

    /**
     * 查询运动会积分规则列表
     * <p>
     * 获取指定运动会下的所有积分规则，按名次（rank）升序排列。
     * Service 层采用 Cache-Aside 模式：
     * 先查 Redis（key = event:score_rule:meeting:{meetingId}），
     * 未命中则查 DB（过滤 delFlag=0，按 rank 升序），
     * 结果写入 Redis 缓存 30 分钟。
     *
     * @param meetingId 运动会 ID，通过路径参数传递
     * @return R&lt;List&lt;ScoreRule&gt;&gt; 该运动会的积分规则列表
     */
    @ApiOperation("查询运动会积分规则列表")
    @GetMapping("/meeting/{meetingId}")
    @ApiImplicitParam(name = "meetingId", value = "运动会ID", required = true)
    public R<List<ScoreRule>> list(@PathVariable Long meetingId) {
        return R.ok(scoreRuleService.listByMeetingId(meetingId));
    }
}
