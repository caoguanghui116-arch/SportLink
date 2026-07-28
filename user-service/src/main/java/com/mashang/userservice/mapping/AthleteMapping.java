package com.mashang.userservice.mapping;

import com.mashang.userservice.domain.entity.Athlete;
import com.mashang.userservice.domain.query.create.AthleteCreateQuery;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface AthleteMapping {

    AthleteMapping INSTANCE= Mappers.getMapper(AthleteMapping.class);

    //运动员添加转实体
    Athlete athlete(AthleteCreateQuery query);

}
