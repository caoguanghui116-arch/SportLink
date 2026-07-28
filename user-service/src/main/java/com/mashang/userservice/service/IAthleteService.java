package com.mashang.userservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mashang.userservice.domain.entity.Athlete;
import com.mashang.userservice.domain.query.create.AthleteCreateQuery;
import com.mashang.userservice.domain.query.update.AnnouncementUpdateQuery;

import java.util.List;

public interface IAthleteService extends IService<Athlete> {

    int add(AthleteCreateQuery athlete);

    int update(AnnouncementUpdateQuery athlete);

    int delete(Long athleteId);

    List<Athlete> listAll();

    List<Athlete> listByDeptId(Long deptId);
}
