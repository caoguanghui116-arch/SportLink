package com.mashang.notificationservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mashang.notificationservice.domain.entity.Announcement;
import com.mashang.notificationservice.domain.vo.AnnouncementVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AnnouncementMapper extends BaseMapper<Announcement> {

    /**
     * 查询所有公告（按时间倒序）
     */
    List<AnnouncementVo> selectAll();

    /**
     * 根据运动会ID查询公告（按时间倒序）
     */
    List<AnnouncementVo> selectByMeetingId(@Param("meetingId") Long meetingId);

    /**
     * 根据ID查询公告详情
     */
    AnnouncementVo selectById(@Param("announcementId") Long announcementId);

}
