package com.mashang.userservice.controller;

import com.mashang.userservice.domain.entity.Announcement;
import com.mashang.common.common.R;
import com.mashang.userservice.domain.query.create.AnnouncementQuery;
import com.mashang.userservice.domain.query.update.AnnouncementUpdateQuery;
import com.mashang.userservice.service.IAnnouncementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * 公告管理控制器
 *
 * 核心职责：
 * - 公告发布：创建新公告，关联发布人和所属运动会
 * - 公告修改：更新公告标题、内容等信息
 * - 公告删除：软删除公告（通过 delFlag 标记，而非物理删除）
 * - 公告列表：查询全部公告或按运动会筛选
 * - 公告详情：根据ID获取公告完整内容
 * - 草稿发布：将状态为草稿的公告正式发布
 *
 * 设计思路：
 * - 公告支持草稿/已发布两种状态，草稿状态下仅创建者可见
 * - 按运动会ID筛选公告，支持多运动会场景下的公告隔离
 * - 所有业务逻辑委托给 IAnnouncementService 处理
 */
@Api(tags = "公告管理")
@RestController
@RequestMapping("/announcement")
public class AnnouncementController {

    /** 公告服务接口 */
    @Autowired
    private IAnnouncementService announcementService;

    /**
     * 发布新公告
     *
     * @param query       公告请求体，包含 title、content、meetingId 等字段
     * @return 统一响应体，ok 表示发布成功
     */
    @ApiOperation("发布公告")
    @PostMapping
    public R<Void> publish(@RequestBody @Validated AnnouncementQuery query) {
        return R.toResult(announcementService.publish(query));
    }

    /**
     * 修改公告
     *
     * @param announcementQuery 公告修改参数实体，包含要更新的字段（id 必填）
     * @return 统一响应体，ok 表示修改成功
     */
    @ApiOperation("修改公告")
    @PutMapping
    public R<Void> update(@RequestBody @Validated AnnouncementUpdateQuery announcementQuery) {
        return R.toResult(announcementService.update(announcementQuery));
    }

    /**
     * 删除公告（软删除）
     *
     * 实际不删除数据库记录，而是将 delFlag 字段标记为已删除状态
     *
     * @param id 公告ID
     * @return 统一响应体，ok 表示删除成功
     */
    @ApiOperation("删除公告")
    @DeleteMapping("/{id}")
    @ApiImplicitParam(name = "id", value = "公告ID", required = true)
    public R<Void> delete(@PathVariable Long id) {
        return R.toResult(announcementService.delete(id));
    }

    /**
     * 获取所有公告列表
     *
     * @return 统一响应体，data 字段为公告实体列表
     */
    @ApiOperation("公告列表")
    @GetMapping
    public R<List<Announcement>> list() {
        return R.ok(announcementService.listAll());
    }

    /**
     * 按运动会ID获取公告列表
     *
     * 使用场景：进入某个运动会详情页时，展示该运动会相关的所有公告
     *
     * @param meetingId 运动会ID
     * @return 统一响应体，data 字段为该运动会的公告列表
     */
    @ApiOperation("运动会公告列表")
    @GetMapping("/meeting/{meetingId}")
    @ApiImplicitParam(name = "meetingId", value = "运动会ID", required = true)
    public R<List<Announcement>> listByMeeting(@PathVariable Long meetingId) {
        return R.ok(announcementService.listByMeetingId(meetingId));
    }

    /**
     * 获取公告详情
     *
     * @param id 公告ID
     * @return 统一响应体，data 字段为公告完整实体
     */
    @ApiOperation("公告详情")
    @GetMapping("/{id}")
    @ApiImplicitParam(name = "id", value = "公告ID", required = true)
    public R<Announcement> detail(@PathVariable Long id) {
        return R.ok(announcementService.detail(id));
    }

    /**
     * 将草稿状态的公告正式发布
     *
     * 使用场景：管理员先保存公告为草稿，审核无误后调用此接口正式发布
     *
     * @param id 公告ID
     * @return 统一响应体，ok 表示发布成功
     */
    @ApiOperation("发布草稿")
    @PutMapping("/{id}/publish")
    @ApiImplicitParam(name = "id", value = "公告ID", required = true)
    public R<Void> publishDraft(@PathVariable Long id) {
        return R.toResult(announcementService.publishDraft(id));
    }
}
