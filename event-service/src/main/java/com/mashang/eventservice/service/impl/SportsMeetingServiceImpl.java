package com.mashang.eventservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.eventservice.common.KeyCommon;
import com.mashang.eventservice.domain.entity.SportsMeeting;
import com.mashang.eventservice.domain.query.create.BasicSetupQuery;
import com.mashang.eventservice.mapper.SportsMeetingMapper;
import com.mashang.eventservice.mapping.SportsMeetingMapping;
import com.mashang.eventservice.service.ISportsMeetingService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

/**
 * 运动会管理服务 —— 写 DB 后清除缓存。
 *
 * 运动会信息变化频率极低（一场运动会持续数天），天然适合缓存。
 * 新增运动会时清除列表缓存，下次查询重新加载。
 */
@Service
public class SportsMeetingServiceImpl extends ServiceImpl<SportsMeetingMapper, SportsMeeting> implements ISportsMeetingService {

    @Autowired
    private SportsMeetingMapper sportsMeetingMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    /**
     * 新增运动会 —— 写 DB 后清除运动会列表缓存。
     */
    @Override
    public int addMeeting(BasicSetupQuery addQuery) {
        // 届数/名称唯一性校验
        LambdaQueryWrapper<SportsMeeting> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(SportsMeeting::getMeetingSession, addQuery.getMeetingSession())
                .or()
                .eq(SportsMeeting::getMeetingName, addQuery.getMeetingName());

        if (sportsMeetingMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("届数或名称已存在");
        }

        int rows = sportsMeetingMapper.insert(SportsMeetingMapping.INSTANCE.toEntity(addQuery));

        // 新增后清除缓存
        if (rows > 0) {
            redisTemplate.delete(KeyCommon.buildKey());
        }
        return rows;
    }
}
