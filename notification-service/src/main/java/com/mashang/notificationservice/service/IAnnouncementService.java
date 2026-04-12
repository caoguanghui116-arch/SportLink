package com.mashang.notificationservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mashang.notificationservice.domain.entity.Announcement;
import com.mashang.notificationservice.domain.query.create.AnnouncementQuery;
import com.mashang.notificationservice.domain.vo.AnnouncementVo;

import java.util.List;

public interface IAnnouncementService extends IService<Announcement> {

    /**
     * 发布公告（status默认为草稿0，需要publishDraft来正式发布）
     * @param query 公告参数
     * @param publisherId 发布者ID（从JWT中获取）
     * @return 返回操作行数
     */
    int publish(AnnouncementQuery query, Long publisherId);

    /**
     * 修改公告
     * @param announcementId 公告ID
     * @param query 修改参数
     * @return 返回操作行数
     */
    int update(Long announcementId, AnnouncementQuery query);

    /**
     * 删除公告
     * @param announcementId 公告ID
     * @return 返回操作行数
     */
    int delete(Long announcementId);

    /**
     * 查询所有公告列表（按时间倒序）
     * @return 公告列表
     */
    List<AnnouncementVo> listAll();

    /**
     * 根据运动会ID查询公告列表（按时间倒序）
     * @param meetingId 运动会ID
     * @return 公告列表
     */
    List<AnnouncementVo> listByMeetingId(Long meetingId);

    /**
     * 查询公告详情
     * @param announcementId 公告ID
     * @return 公告详情
     */
    AnnouncementVo detail(Long announcementId);

    /**
     * 发布草稿公告（status 0 -> 1）
     * @param announcementId 公告ID
     * @return 返回操作行数
     */
    int publishDraft(Long announcementId);

}
