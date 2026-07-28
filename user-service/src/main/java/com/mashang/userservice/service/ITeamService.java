package com.mashang.userservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mashang.userservice.domain.entity.Team;
import com.mashang.userservice.domain.query.create.TeamCreateQuery;
import com.mashang.userservice.domain.query.update.TeamUpdateQuery;

import java.util.List;

public interface ITeamService extends IService<Team> {

    int add(TeamCreateQuery team);

    int update(TeamUpdateQuery team);

    int delete(Long teamId);

    List<Team> listAll();

    List<Team> listByDeptId(Long deptId);
}
