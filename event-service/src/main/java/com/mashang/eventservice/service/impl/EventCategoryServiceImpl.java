package com.mashang.eventservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.eventservice.domain.entity.EventCategory;
import com.mashang.eventservice.mapper.EventCategoryMapper;
import com.mashang.eventservice.service.IEventCategoryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class EventCategoryServiceImpl extends ServiceImpl<EventCategoryMapper, EventCategory> implements IEventCategoryService {

    @Autowired
    private EventCategoryMapper eventCategoryMapper;

    @Override
    public int addCategory(EventCategory category) {
        LambdaQueryWrapper<EventCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EventCategory::getCategoryName, category.getCategoryName())
                .eq(EventCategory::getMeetingId, category.getMeetingId());
        if (eventCategoryMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("该分类名称已存在");
        }
        return eventCategoryMapper.insert(category);
    }

    @Override
    public List<EventCategory> listByMeetingId(Long meetingId) {
        LambdaQueryWrapper<EventCategory> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EventCategory::getMeetingId, meetingId)
                .eq(EventCategory::getDelFlag, 0)
                .orderByAsc(EventCategory::getSortOrder);
        return eventCategoryMapper.selectList(wrapper);
    }
}
