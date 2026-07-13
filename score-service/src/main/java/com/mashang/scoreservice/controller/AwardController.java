package com.mashang.scoreservice.controller;

import com.mashang.common.common.R;
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

/**
 * 奖项管理控制器
 * <p>
 * 核心职责：提供奖项的创建与查询 RESTful API 接口。
 * 设计思路：
 * <ul>
 *   <li>奖项归属于运动会，按运动会维度进行管理和查询</li>
 *   <li>奖项数据读多写少，查询接口使用 Redis 缓存加速</li>
 *   <li>新增奖项后立即删除对应运动会的缓存，保证数据一致性（Cache Aside 模式）</li>
 * </ul>
 *
 * @author SportLink Team
 */
@Api(tags = "奖项管理")
@RestController
@RequestMapping("/award")
public class AwardController {

    /** 奖项服务接口，负责奖项的增删改查业务逻辑 */
    @Autowired
    private IAwardService awardService;

    /**
     * 添加奖项
     * <p>
     * 为一届运动会新增一个奖项配置，如"最佳运动员"、"最佳团队奖"等。
     * 写入成功后会自动清除该运动会的奖项缓存。
     * 使用场景：管理员在运动会配置阶段设置各类奖项。
     *
     * @param query 奖项创建请求体，包含 meetingId（运动会ID）、name（奖项名称）、description（奖项描述）
     * @return 统一响应对象 R，result>0 表示添加成功
     */
    @ApiOperation("添加奖项")
    @PostMapping
    public R<Void> addAward(@RequestBody @Validated AwardQuery query) {
        return R.toResult(awardService.add(query));
    }

    /**
     * 查询运动会奖项列表
     * <p>
     * 按运动会 ID 查询该运动会设置的所有奖项，数据优先从 Redis 缓存读取，
     * 缓存未命中时回源数据库查询并回写缓存。
     * 使用场景：前端页面展示某届运动会的奖项设置清单、颁奖页面展示可颁奖项。
     *
     * @param meetingId 运动会ID，唯一标识一届运动会
     * @return 该运动会下所有奖项的 VO 列表
     */
    @ApiOperation("查询运动会奖项列表")
    @GetMapping("/meeting/{meetingId}")
    @ApiImplicitParam(name = "meetingId", value = "运动会ID", required = true)
    public R<List<AwardVo>> awardList(@PathVariable Long meetingId) {
        return R.ok(awardService.listByMeetingId(meetingId));
    }
}
