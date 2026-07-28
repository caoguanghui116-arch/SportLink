package com.mashang.notificationservice.controller;

import com.mashang.common.common.R;
import com.mashang.notificationservice.domain.query.create.AnnouncementQuery;
import com.mashang.notificationservice.domain.vo.AnnouncementVo;
import com.mashang.notificationservice.service.IAnnouncementService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.List;

/**
 * 公告管理控制器（通知服务）
 *
 * <p>提供面向管理员和普通用户的公告 REST API，核心功能包括：
 * <ul>
 *   <li>发布公告 —— 仅管理员角色（roleId=1）可操作，需携带有效 JWT</li>
 *   <li>修改公告 —— 支持编辑已存在的公告内容和属性</li>
 *   <li>删除公告 —— 物理或逻辑删除指定公告</li>
 *   <li>查询公告列表 —— 全局公告列表 & 按运动会ID筛选</li>
 *   <li>公告详情 —— 查看单条公告完整内容</li>
 *   <li>发布草稿公告 —— 将草稿状态公告转为已发布</li>
 * </ul>
 *
 * <p>设计思路：
 * <ul>
 *   <li>发布接口从 {@link HttpServletRequest} 中提取 JWT 获取当前登录用户，
 *       并通过 header 中的 {@code roleId} 做管理员权限校验，避免越权操作</li>
 *   <li>修改接口要求公告存在性校验，不存在则返回友好错误提示</li>
 *   <li>接口路径挂载在 {@code /notification/announcement} 下，与消息通知接口职责分离</li>
 * </ul>
 *
 * @author mashang
 */
@Api(tags = "公告管理(通知服务)")
@RestController
@RequestMapping("/notification/announcement")
public class AnnouncementController {

    /** 公告业务服务接口，封装公告的增删改查及状态变更逻辑 */
    @Autowired
    private IAnnouncementService announcementService;

    /**
     * 发布公告
     *
     * <p>仅管理员角色可操作。流程：
     * <ol>
     *   <li>从请求头的 JWT Token 中解析出当前登录用户ID</li>
     *   <li>校验用户是否登录（userId 不为 0）</li>
     *   <li>校验请求头中的 roleId 是否为管理员（roleId=1）</li>
     *   <li>通过 {@code @Validated} 校验请求体参数后执行发布</li>
     * </ol>
     *
     * @param request HTTP 请求对象，用于提取 JWT Token 和 roleId 请求头
     * @param query   公告发布参数对象，包含标题、内容、关联运动会ID等
     * @return 统一响应体，发布成功时 code=200
     */
    @ApiOperation("发布公告")
    @PostMapping
    public R<?> publish(HttpServletRequest request, @RequestBody @Validated AnnouncementQuery query) {

        Long userId = Long.valueOf(request.getHeader("X-User-Id"));
        if (userId == 0L) {
            return R.fail("未登录或登录已过期");
        }
        String roleIdHeader = request.getHeader("roleId");
        if (roleIdHeader == null || !"1".equals(roleIdHeader)) {
            return R.fail(403, "无管理员权限");
        }
        return R.toResult(announcementService.publish(query, userId));
    }

    /**
     * 修改公告
     *
     * <p>修改前会校验公告是否存在，不存在则返回错误信息，避免空更新。
     *
     * @param announcementId 公告ID（路径参数，必填）
     * @param query          公告修改参数对象，包含更新后的标题、内容等
     * @return 统一响应体，修改成功时 code=200，公告不存在时返回错误信息
     */
    @ApiOperation("修改公告")
    @PutMapping("/{announcementId}")
    @ApiImplicitParam(name = "announcementId", value = "公告ID", required = true)
    public R<Void> update(@PathVariable Long announcementId, @RequestBody @Validated AnnouncementQuery query) {
        if (announcementService.detail(announcementId) == null) {
            return R.fail("公告不存在");
        }
        return R.toResult(announcementService.update(announcementId, query));
    }

    /**
     * 删除公告
     *
     * @param announcementId 公告ID（路径参数名称为 "id"）
     * @return 统一响应体，删除成功时 code=200
     */
    @ApiOperation("删除公告")
    @DeleteMapping("/{id}")
    @ApiImplicitParam(name = "id", value = "公告ID", required = true)
    public R<Void> delete(@PathVariable("id") Long announcementId) {
        return R.toResult(announcementService.delete(announcementId));
    }

    /**
     * 查询全量公告列表
     *
     * <p>返回所有公告，前端可按发布时间倒序展示。适用于公告中心首页。
     *
     * @return 统一响应体，data 为 {@link AnnouncementVo} 列表
     */
    @ApiOperation("查询公告列表")
    @GetMapping
    public R<List<AnnouncementVo>> list() {
        return R.ok(announcementService.listAll());
    }

    /**
     * 查询指定运动会的公告列表
     *
     * <p>按运动会ID筛选公告，适用于运动会详情页的"赛事公告"Tab。
     *
     * @param meetingId 运动会ID（路径参数，必填）
     * @return 统一响应体，data 为 {@link AnnouncementVo} 列表
     */
    @ApiOperation("查询运动会公告")
    @GetMapping("/meeting/{meetingId}")
    @ApiImplicitParam(name = "meetingId", value = "运动会ID", required = true)
    public R<List<AnnouncementVo>> listByMeetingId(@PathVariable Long meetingId) {
        return R.ok(announcementService.listByMeetingId(meetingId));
    }

    /**
     * 查询公告详情
     *
     * @param announcementId 公告ID（路径参数名称为 "id"）
     * @return 统一响应体，data 为 {@link AnnouncementVo} 对象
     */
    @ApiOperation("公告详情")
    @GetMapping("/{id}")
    @ApiImplicitParam(name = "id", value = "公告ID", required = true)
    public R<AnnouncementVo> detail(@PathVariable("id") Long announcementId) {
        return R.ok(announcementService.detail(announcementId));
    }

    /**
     * 发布草稿公告
     *
     * <p>将指定公告从草稿状态（status=0）变更为已发布状态（status=1），
     * 相当于对已保存的草稿执行"正式发布"操作。
     *
     * @param announcementId 公告ID（路径参数名称为 "id"）
     * @return 统一响应体，发布成功时 code=200
     */
    @ApiOperation("发布草稿公告")
    @PutMapping("/{id}/publish")
    @ApiImplicitParam(name = "id", value = "公告ID", required = true)
    public R<Void> publishDraft(@PathVariable("id") Long announcementId) {
        return R.toResult(announcementService.publishDraft(announcementId));
    }
}
