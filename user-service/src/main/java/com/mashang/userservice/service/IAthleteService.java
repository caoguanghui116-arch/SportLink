package com.mashang.userservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mashang.userservice.domain.entity.Athlete;

import java.util.List;

public interface IAthleteService extends IService<Athlete> {

    int add(Athlete athlete);

    int update(Athlete athlete);

    int delete(Long athleteId);

    List<Athlete> listAll();

    List<Athlete> listByDeptId(Long deptId);
}
