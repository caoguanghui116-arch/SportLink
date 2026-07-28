package com.mashang.userservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mashang.userservice.domain.entity.Athlete;
import com.mashang.userservice.domain.query.update.AnnouncementUpdateQuery;

public interface AthleteMapper extends BaseMapper<Athlete> {

    int updateAthlete(AnnouncementUpdateQuery announcementUpdateQuery);
}
