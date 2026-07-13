package com.mashang.eventservice.controller;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.mashang.common.common.PageQuery;
import com.mashang.common.common.TableDataInfo;
import com.mashang.common.constants.HttpStatus;
import com.mashang.common.common.R;
import com.mashang.eventservice.domain.query.create.ScheduleQuery;
import com.mashang.eventservice.domain.query.select.SchedulePageQuery;
import com.mashang.eventservice.domain.vo.RefereeVo;
import com.mashang.eventservice.domain.vo.ScheduleVo;
import com.mashang.eventservice.domain.vo.VenueVo;
import com.mashang.eventservice.feign.RefereeServiceFeign;
import com.mashang.eventservice.service.IEventItemService;
import com.mashang.eventservice.service.IScheduleService;
import com.mashang.eventservice.service.IVenueService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 赛程安排控制器
 * <p>
 * 核心职责：提供赛程管理相关的 RESTful API，包括：
 * <ul>
 *   <li>赛程新增与分页查询</li>
 *   <li>场地、比赛项目、裁判等基础数据的查询（供赛程编排时下拉选择）</li>
 * </ul>
 * <p>
 * 设计思路：赛程编排需要关联场地、项目、裁判三类核心资源，
 * 因此本 Controller 聚合了场地服务（IVenueService）、比赛项目服务（IEventItemService）、
 * 裁判服务（通过 Feign 远程调用 RefereeServiceFeign）和赛程服务（IScheduleService）。
 * 裁判数据通过 OpenFeign 跨服务调用获取，体现了微服务架构下的服务间协作模式。
 *
 * @author mashang
 */
@Api(tags = "赛程安排")
@RestController
@RequestMapping("/schedule")
public class ScheduleController {

    /** 场地服务接口 —— 提供所有场地的查询，用于赛程编排时选择场地 */
    @Autowired
    private IVenueService venueService;

    /** 比赛项目服务接口 —— 提供所有比赛项目的查询，用于赛程编排时选择项目 */
    @Autowired
    private IEventItemService eventItemService;

    /** 裁判服务 Feign 客户端 —— 跨服务调用裁判微服务，获取所有裁判列表 */
    @Autowired
    private RefereeServiceFeign refereeServiceFeign;

    /** 赛程服务接口 —— 负责赛程的创建和分页查询 */
    @Autowired
    private IScheduleService scheduleService;

    /**
     * 查询所有场地
     * <p>
     * 获取系统中所有可用场地列表，供前端赛程编排时下拉选择。
     * Service 层采用 Cache-Aside 模式，缓存 TTL 为 30 分钟。
     *
     * @return R&lt;List&lt;VenueVo&gt;&gt; 场地列表
     */
    @GetMapping("/venue")
    @ApiOperation("查询所有场地")
    public R<List<VenueVo>> allVenue() {
        return R.ok(venueService.allVenue());
    }

    /**
     * 查询所有比赛项目
     * <p>
     * 获取系统中所有比赛项目列表，供前端赛程编排时下拉选择。
     * Service 层采用 Cache-Aside 模式，缓存 TTL 为 30 分钟。
     *
     * @return R&lt;?&gt; 比赛项目列表（List&lt;EventItemVo&gt;）
     */
    @GetMapping("/item")
    @ApiOperation("查询所有比赛项目")
    public R<?> allItem() {
        return R.ok(eventItemService.allItem());
    }

    /**
     * 查询所有裁判
     * <p>
     * 通过 OpenFeign 远程调用裁判微服务，获取所有裁判列表。
     * 此为跨服务同步调用，要求 referee-service 微服务处于可用状态。
     * 如果裁判服务不可用，Feign 将抛出异常由全局异常处理器统一处理。
     *
     * @return R&lt;List&lt;RefereeVo&gt;&gt; 裁判列表
     */
    @GetMapping("/referee")
    @ApiOperation("查询所有裁判")
    public R<List<RefereeVo>> allReferee() {
        return refereeServiceFeign.allReferee();
    }

    /**
     * 添加赛程
     * <p>
     * 创建一条新的赛程记录，包含比赛项目、场地、裁判、比赛时间等信息。
     * Service 层会校验场地和裁判的时间冲突，避免同一资源在同一时段被重复安排。
     *
     * @param scheduleQuery 赛程创建请求体，包含比赛项目ID、场地ID、裁判ID、比赛时间等
     * @return R&lt;Void&gt; 操作结果
     */
    @ApiOperation("添加赛程")
    @PostMapping
    public R<Void> addSchedule(@RequestBody @Validated ScheduleQuery scheduleQuery) {
        return R.toResult(scheduleService.addSchedule(scheduleQuery));
    }

    /**
     * 赛程分页查询
     * <p>
     * 支持按比赛时间区间等条件进行分页查询，返回 TableDataInfo 格式的分页数据。
     * <p>
     * 返回格式说明：
     * <ul>
     *   <li>{@code code}: 状态码，成功时为 HttpStatus.SUCCESS</li>
     *   <li>{@code rows}: 当前页的记录列表</li>
     *   <li>{@code total}: 符合条件的总记录数</li>
     * </ul>
     * 与直接返回 R 不同，分页查询需要额外返回 total 字段供前端分页组件使用，
     * 因此使用 TableDataInfo 包装返回结果。
     *
     * @param pageQuery 分页参数（pageNum 当前页码、pageSize 每页条数）
     * @param schedulePageQuery 赛程查询条件（如比赛时间区间）
     * @return TableDataInfo&lt;ScheduleVo&gt; 分页数据
     */
    @ApiOperation(value = "赛程分页查询", notes = "支持按比赛时间区间查询")
    @PostMapping("/page")
    public TableDataInfo<ScheduleVo> page(@Validated PageQuery pageQuery, SchedulePageQuery schedulePageQuery) {
        Page<ScheduleVo> page = scheduleService.page(pageQuery, schedulePageQuery);
        TableDataInfo<ScheduleVo> rspData = new TableDataInfo<>();
        rspData.setCode(HttpStatus.SUCCESS);
        rspData.setRows(page.getRecords());
        rspData.setTotal(page.getTotal());
        return rspData;
    }
}
