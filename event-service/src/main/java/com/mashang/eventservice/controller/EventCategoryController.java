package com.mashang.eventservice.controller;

import com.mashang.eventservice.domain.entity.EventCategory;
import com.mashang.common.common.R;
import com.mashang.eventservice.service.IEventCategoryService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 项目分类控制器
 * <p>
 * 核心职责：管理运动会下比赛项目的分类信息，提供分类的增删改查 RESTful API。
 * <p>
 * 业务背景：一场运动会通常包含多个比赛项目，需要按类别（如田径类、球类、水上项目等）
 * 进行分组管理。分类数据按 sortOrder 字段升序排列，支持拖拽排序。
 * <p>
 * 设计思路：分类数据以 meetingId 为维度隔离，不同运动会的分类独立管理。
 * Service 层按 meetingId 建立缓存 Key，实现按运动会的缓存粒度。
 *
 * @author mashang
 */
@Api(tags = "项目分类")
@RestController
@RequestMapping("/basic/setup/category")
public class EventCategoryController {

    /** 项目分类服务接口 —— 负责分类的增删改查及缓存管理 */
    @Autowired
    private IEventCategoryService eventCategoryService;

    /**
     * 添加分类
     * <p>
     * 在指定运动会下新增一个项目分类。
     * Service 层会校验同一运动会下分类名称是否重复，
     * 校验通过后写入 DB 并删除该运动会对应的缓存。
     *
     * @param category 分类实体对象，需包含 categoryName、meetingId、sortOrder 等字段
     * @return R&lt;Void&gt; 操作结果
     */
    @ApiOperation("添加分类")
    @PostMapping
    public R<Void> add(@RequestBody @Validated EventCategory category) {
        return R.toResult(eventCategoryService.addCategory(category));
    }

    /**
     * 修改分类
     * <p>
     * 根据分类 ID 更新分类信息（如名称、排序序号等）。
     * 更新成功后清除对应运动会的分类缓存，保证数据一致性。
     *
     * @param category 分类实体对象，必须包含 categoryId 用于定位记录
     * @return R&lt;Void&gt; 操作结果
     */
    @ApiOperation("修改分类")
    @PutMapping
    public R<Void> update(@RequestBody @Validated EventCategory category) {
        return R.toResult(eventCategoryService.updateById(category));
    }

    /**
     * 删除分类
     * <p>
     * 根据分类 ID 物理删除分类记录。
     * 删除前应确保该分类下没有关联的比赛项目，否则可能导致数据不完整。
     *
     * @param categoryId 分类 ID，通过路径参数传递
     * @return R&lt;Void&gt; 操作结果
     */
    @ApiOperation("删除分类")
    @DeleteMapping("/{categoryId}")
    @ApiImplicitParam(name = "categoryId", value = "分类ID", required = true)
    public R<Void> delete(@PathVariable Long categoryId) {
        return R.toResult(eventCategoryService.removeById(categoryId));
    }

    /**
     * 查询运动会分类列表
     * <p>
     * 获取指定运动会下的所有分类，按 sortOrder 升序排列。
     * Service 层采用 Cache-Aside 模式：
     * 先查 Redis（key = event:category:meeting:{meetingId}），
     * 未命中则查 DB（过滤 delFlag=0 的有效记录），
     * 结果写入 Redis 缓存 30 分钟。
     *
     * @param meetingId 运动会 ID，通过路径参数传递
     * @return R&lt;List&lt;EventCategory&gt;&gt; 该运动会的分类列表
     */
    @ApiOperation("查询运动会分类列表")
    @GetMapping("/meeting/{meetingId}")
    @ApiImplicitParam(name = "meetingId", value = "运动会ID", required = true)
    public R<List<EventCategory>> list(@PathVariable Long meetingId) {
        return R.ok(eventCategoryService.listByMeetingId(meetingId));
    }
}
