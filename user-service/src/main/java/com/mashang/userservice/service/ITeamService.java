package com.mashang.userservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mashang.userservice.domain.entity.Team;

import java.util.List;

public interface ITeamService extends IService<Team> {

    int add(Team team);

    int update(Team team);

    int delete(Long teamId);

    List<Team> listAll();

    List<Team> listByDeptId(Long deptId);
}
