package com.mashang.notificationservice.mapping;

import com.mashang.notificationservice.domain.entity.Notification;
import com.mashang.notificationservice.domain.query.create.NotificationQuery;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface NotificationMapping {

    NotificationMapping INSTANCE = Mappers.getMapper(NotificationMapping.class);

    // 通知参数转实体
    Notification toEntity(NotificationQuery notificationQuery);

}
