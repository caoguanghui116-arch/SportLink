package com.mashang.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.userservice.domain.entity.Athlete;
import com.mashang.userservice.mapper.AthleteMapper;
import com.mashang.userservice.service.IAthleteService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AthleteServiceImpl extends ServiceImpl<AthleteMapper, Athlete> implements IAthleteService {

    @Autowired
    private AthleteMapper athleteMapper;

    @Override
    public int add(Athlete athlete) {
        return athleteMapper.insert(athlete);
    }

    @Override
    public int update(Athlete athlete) {
        return athleteMapper.updateById(athlete);
    }

    @Override
    public int delete(Long athleteId) {
        return athleteMapper.deleteById(athleteId);
    }

    @Override
    public List<Athlete> listAll() {
        return athleteMapper.selectList(null);
    }

    @Override
    public List<Athlete> listByDeptId(Long deptId) {
        LambdaQueryWrapper<Athlete> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Athlete::getDeptId, deptId);
        return athleteMapper.selectList(wrapper);
    }
}
