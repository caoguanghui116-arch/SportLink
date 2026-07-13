package com.mashang.eventservice.controller;

import com.mashang.common.common.R;
import com.mashang.eventservice.domain.query.create.BasicSetupQuery;
import com.mashang.eventservice.domain.query.create.EventItemQuery;
import com.mashang.eventservice.domain.query.update.EventItemUpdate;
import com.mashang.eventservice.service.IEventItemService;
import com.mashang.eventservice.service.ISportsMeetingService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * 基础配置控制器
 * <p>
 * 核心职责：提供运动会基础配置相关的 RESTful API，包括：
 * <ul>
 *   <li>运动会的创建与查询</li>
 *   <li>比赛项目的增删改查</li>
 * </ul>
 * <p>
 * 设计思路：将运动会和比赛项目的基础 CRUD 操作集中到一个 Controller 中，
 * 便于前端在"基础配置"页签下统一调用。复杂查询（如分页、关联查询）放在各自的专用 Controller 中。
 *
 * @author mashang
 */
@Api(tags = "基础配置")
@RestController
@RequestMapping("/basic/setup")
public class BasicSetupController {

    /** 运动会服务接口 —— 负责运动会的创建和查询 */
    @Autowired
    private ISportsMeetingService sportsMeetingService;

    /** 比赛项目服务接口 —— 负责比赛项目的增删改查 */
    @Autowired
    private IEventItemService eventItemService;

    /**
     * 创建运动会
     * <p>
     * 接收前端提交的运动会基本信息（届数、名称等），
     * 校验届数和名称是否唯一（Service 层完成唯一性校验），
     * 校验通过后写入数据库并清除相关 Redis 缓存。
     *
     * @param addQuery 运动会创建请求体，经 @Validated 校验参数合法性
     * @return R&lt;Void&gt; 操作结果，成功时不返回数据体，失败时返回错误信息
     */
    @ApiOperation("创建运动会")
    @PostMapping("/meeting")
    public R<Void> addMeeting(@RequestBody @Validated BasicSetupQuery addQuery) {
        return R.toResult(sportsMeetingService.addMeeting(addQuery));
    }

    /**
     * 添加比赛项目
     * <p>
     * 创建一个新的比赛项目，关联到指定的运动会。
     * Service 层会校验项目名称唯一性，写入 DB 后清除缓存。
     *
     * @param addQuery 比赛项目创建请求体，包含项目名称、所属运动会等信息
     * @return R&lt;Void&gt; 操作结果
     */
    @ApiOperation("添加比赛项目")
    @PostMapping("/project")
    public R<Void> addProject(@RequestBody @Validated EventItemQuery addQuery) {
        return R.toResult(eventItemService.addProject(addQuery));
    }

    /**
     * 修改比赛项目
     * <p>
     * 根据项目 ID 更新比赛项目的信息（如名称、类型、场地等）。
     * 更新成功后删除 Redis 缓存，下次查询时自动重建。
     *
     * @param updateQuery 比赛项目更新请求体，必须包含 itemId 用于定位记录
     * @return R&lt;Void&gt; 操作结果
     */
    @ApiOperation("修改比赛项目")
    @PutMapping("/project")
    public R<Void> updateProject(@RequestBody @Validated EventItemUpdate updateQuery) {
        return R.toResult(eventItemService.updateProject(updateQuery));
    }

    /**
     * 删除比赛项目
     * <p>
     * 根据项目 ID 物理删除比赛项目记录，同时清除相关 Redis 缓存。
     * 注意：此处为物理删除（DELETE），不是逻辑删除，删除后数据不可恢复。
     *
     * @param itemId 项目 ID，通过路径参数传递
     * @return R&lt;Void&gt; 操作结果
     */
    @ApiOperation("删除比赛项目")
    @DeleteMapping("/project/{itemId}")
    @ApiImplicitParam(name = "itemId", value = "项目ID", required = true)
    public R<Void> deleteProject(@PathVariable Long itemId) {
        return R.toResult(eventItemService.deleteProject(itemId));
    }

    /**
     * 查询项目详情
     * <p>
     * 根据项目 ID 获取单个比赛项目的详细信息。
     * 调用 MyBatis-Plus 内置的 getById 方法直接从 DB 查询，不走缓存。
     * 用于编辑页面回显数据等场景。
     *
     * @param itemId 项目 ID，通过路径参数传递
     * @return R&lt;?&gt; 项目详情数据（EventItem 实体）
     */
    @ApiOperation("查询项目详情")
    @GetMapping("/project/{itemId}")
    @ApiImplicitParam(name = "itemId", value = "项目ID", required = true)
    public R<?> getItemInfo(@PathVariable Long itemId) {
        return R.ok(eventItemService.getById(itemId));
    }

    /**
     * 查询所有运动会
     * <p>
     * 获取系统中所有运动会的列表。
     * Service 层采用 Cache-Aside 模式：先查 Redis 缓存，未命中再查 DB 并回写缓存。
     * 缓存 TTL 为 30 分钟，新增运动时会主动清除缓存。
     *
     * @return R&lt;?&gt; 运动会列表（List&lt;SportsMeeting&gt;）
     */
    @ApiOperation("查询所有运动会")
    @GetMapping("/meeting")
    public R<?> allMeeting() {
        return R.ok(sportsMeetingService.allMeeting());
    }
}
