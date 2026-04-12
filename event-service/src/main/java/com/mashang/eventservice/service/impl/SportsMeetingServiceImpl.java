package com.mashang.eventservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.eventservice.domain.entity.R;
import com.mashang.eventservice.domain.entity.SportsMeeting;
import com.mashang.eventservice.domain.query.create.BasicSetupQuery;
import com.mashang.eventservice.mapper.SportsMeetingMapper;
import com.mashang.eventservice.mapping.SportsMeetingMapping;
import com.mashang.eventservice.service.ISportsMeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Service
public class SportsMeetingServiceImpl extends ServiceImpl<SportsMeetingMapper, SportsMeeting> implements ISportsMeetingService {

    @Autowired
    private SportsMeetingMapper sportsMeetingMapper;

    @Override
    public int addMeeting(BasicSetupQuery addQuery) {
        LambdaQueryWrapper<SportsMeeting> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SportsMeeting::getMeetingSession, addQuery.getMeetingSession())
                .or()
                .eq(SportsMeeting::getMeetingName, addQuery.getMeetingName());

        if (sportsMeetingMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("届数或名称已存在");
        }

        return sportsMeetingMapper.insert(SportsMeetingMapping.INSTANCE.toEntity(addQuery));
    }
}
