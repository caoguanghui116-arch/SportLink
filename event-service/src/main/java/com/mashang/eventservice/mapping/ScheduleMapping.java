package com.mashang.eventservice.mapping;

import com.mashang.eventservice.domain.entity.Schedule;
import com.mashang.eventservice.domain.entity.SportsMeeting;
import com.mashang.eventservice.domain.query.create.BasicSetupQuery;
import com.mashang.eventservice.domain.query.create.ScheduleQuery;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ScheduleMapping {

    ScheduleMapping INSTANCE= Mappers.getMapper(ScheduleMapping.class);

    //赛程添加转赛程实体
    Schedule toEntity(ScheduleQuery scheduleQuery);

}
