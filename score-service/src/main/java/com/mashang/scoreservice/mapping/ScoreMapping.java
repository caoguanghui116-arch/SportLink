package com.mashang.scoreservice.mapping;

import com.mashang.scoreservice.domain.entity.Award;
import com.mashang.scoreservice.domain.entity.PersonalResult;
import com.mashang.scoreservice.domain.entity.TeamResult;
import com.mashang.scoreservice.domain.query.create.AwardQuery;
import com.mashang.scoreservice.domain.query.create.PersonalResultQuery;
import com.mashang.scoreservice.domain.query.create.TeamResultQuery;
import org.mapstruct.Mapper;
import org.mapstruct.factory.Mappers;

@Mapper
public interface ScoreMapping {

    ScoreMapping INSTANCE = Mappers.getMapper(ScoreMapping.class);

    // 个人成绩添加转实体
    PersonalResult toEntity(PersonalResultQuery personalResultQuery);

    // 团体成绩添加转实体
    TeamResult toEntity(TeamResultQuery teamResultQuery);

    // 奖项添加转实体
    Award toEntity(AwardQuery awardQuery);

}
