package com.mashang.userservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mashang.userservice.domain.entity.Announcement;
import com.mashang.userservice.domain.query.create.AnnouncementQuery;
import com.mashang.userservice.domain.query.update.AnnouncementUpdateQuery;

public interface AnnouncementMapper extends BaseMapper<Announcement> {

    int updateAnnouncement(AnnouncementUpdateQuery announcement);

}
