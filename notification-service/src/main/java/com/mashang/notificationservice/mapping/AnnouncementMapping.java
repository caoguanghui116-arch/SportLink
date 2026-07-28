package com.mashang.notificationservice.mapping;

import com.mashang.notificationservice.domain.entity.Announcement;
import com.mashang.notificationservice.domain.query.create.AnnouncementQuery;
import com.mashang.notificationservice.domain.vo.AnnouncementVo;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AnnouncementMapping {

    AnnouncementMapping INSTANCE = Mappers.getMapper(AnnouncementMapping.class);

    // 公告参数转实体
    Announcement toEntity(AnnouncementQuery announcementQuery);

    //响应转实体
    Announcement entity(AnnouncementVo announcementVo);
}
