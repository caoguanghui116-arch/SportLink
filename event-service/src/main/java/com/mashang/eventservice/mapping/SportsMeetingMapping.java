package com.mashang.eventservice.mapping;

import com.mashang.eventservice.domain.entity.SportsMeeting;
import com.mashang.eventservice.domain.query.create.BasicSetupQuery;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface SportsMeetingMapping {

    SportsMeetingMapping INSTANCE= Mappers.getMapper(SportsMeetingMapping.class);

    //添加转实体
    SportsMeeting toEntity(BasicSetupQuery basicSetupQuery);
}
