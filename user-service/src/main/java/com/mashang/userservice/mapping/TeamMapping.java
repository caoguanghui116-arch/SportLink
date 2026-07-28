package com.mashang.userservice.mapping;

import com.mashang.userservice.domain.entity.Team;
import com.mashang.userservice.domain.query.create.TeamCreateQuery;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface TeamMapping {

    TeamMapping INSTANCE= Mappers.getMapper(TeamMapping.class);

    //团队添加实体转团队实体、
    Team team(TeamCreateQuery teamCreateQuery);
}
