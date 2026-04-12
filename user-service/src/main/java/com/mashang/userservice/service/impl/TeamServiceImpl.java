package com.mashang.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.userservice.domain.entity.Team;
import com.mashang.userservice.mapper.TeamMapper;
import com.mashang.userservice.service.ITeamService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamServiceImpl extends ServiceImpl<TeamMapper, Team> implements ITeamService {

    @Autowired
    private TeamMapper teamMapper;

    @Override
    public int add(Team team) {
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Team::getTeamName, team.getTeamName());
        if (teamMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("团队名称已存在");
        }
        return teamMapper.insert(team);
    }

    @Override
    public int update(Team team) {
        return teamMapper.updateById(team);
    }

    @Override
    public int delete(Long teamId) {
        return teamMapper.deleteById(teamId);
    }

    @Override
    public List<Team> listAll() {
        return teamMapper.selectList(null);
    }

    @Override
    public List<Team> listByDeptId(Long deptId) {
        LambdaQueryWrapper<Team> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Team::getDeptId, deptId);
        return teamMapper.selectList(wrapper);
    }
}
