package com.mashang.eventservice.mapping;

import com.mashang.eventservice.domain.entity.EventItem;
import com.mashang.eventservice.domain.entity.SportsMeeting;
import com.mashang.eventservice.domain.query.create.BasicSetupQuery;
import com.mashang.eventservice.domain.query.create.EventItemQuery;
import com.mashang.eventservice.domain.query.update.EventItemUpdate;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface EventItemMapping {

    EventItemMapping INSTANCE= Mappers.getMapper(EventItemMapping.class);

    //添加转实体
    EventItem toEntity(EventItemQuery eventItemQuery);

    //修改转实体
    EventItem toEntity(EventItemUpdate eventItemUpdate);
}
