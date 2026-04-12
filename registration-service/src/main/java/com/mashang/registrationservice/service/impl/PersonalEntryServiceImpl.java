package com.mashang.registrationservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.registrationservice.common.KeyCommon;
import com.mashang.registrationservice.domain.entity.PersonalEntry;
import com.mashang.registrationservice.domain.query.create.PersonalEntryQuery;
import com.mashang.registrationservice.domain.vo.PersonalEntryVo;
import com.mashang.registrationservice.feign.EventServiceFeign;
import com.mashang.registrationservice.mapper.PersonalEntryMapper;
import com.mashang.registrationservice.mapping.PersonalEntryMapping;
import com.mashang.registrationservice.service.IPersonalEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class PersonalEntryServiceImpl extends ServiceImpl<PersonalEntryMapper, PersonalEntry> implements IPersonalEntryService {

    @Autowired
    private PersonalEntryMapper personalEntryMapper;

    @Autowired
    private EventServiceFeign eventServiceFeign;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Override
    public int enroll(PersonalEntryQuery query) {
        LambdaQueryWrapper<PersonalEntry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PersonalEntry::getUserId, query.getUserId())
                .eq(PersonalEntry::getItemId, query.getItemId())
                .eq(PersonalEntry::getMeetingId, query.getMeetingId())
                .eq(PersonalEntry::getStatus, 1);

        if (personalEntryMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("您已报名该项目，请勿重复报名");
        }

        PersonalEntry entry = PersonalEntryMapping.INSTANCE.toEntity(query);
        entry.setStatus(1L);
        return personalEntryMapper.insert(entry);
    }

    @Override
    public int cancel(Long entryId, Long userId) {
        LambdaQueryWrapper<PersonalEntry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(PersonalEntry::getEntryId, entryId)
                .eq(PersonalEntry::getUserId, userId);

        PersonalEntry entry = personalEntryMapper.selectOne(wrapper);
        if (entry == null) {
            throw new RuntimeException("报名记录不存在");
        }

        entry.setStatus(2L);
        return personalEntryMapper.updateById(entry);
    }

    @Override
    public List<PersonalEntryVo> listByUserId(Long userId) {
        return personalEntryMapper.selectByUserId(userId);
    }

    @Override
    public PersonalEntryVo detail(Long entryId) {
        return personalEntryMapper.selectDetailById(entryId);
    }

    @Override
    public int countByItemId(Long itemId) {
        List<PersonalEntryVo> cacheList = (List<PersonalEntryVo>) redisTemplate.opsForValue()
                .get(KeyCommon.buildKey(itemId));
        if (cacheList != null) {
            return cacheList.size();
        }
        return personalEntryMapper.countByItemId(itemId);
    }
}
