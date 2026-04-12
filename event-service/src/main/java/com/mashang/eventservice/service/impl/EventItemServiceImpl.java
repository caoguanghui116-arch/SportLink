package com.mashang.eventservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.eventservice.common.KeyCommon;
import com.mashang.eventservice.domain.entity.EventItem;
import com.mashang.eventservice.domain.entity.R;
import com.mashang.eventservice.domain.query.create.EventItemQuery;
import com.mashang.eventservice.domain.query.update.EventItemUpdate;
import com.mashang.eventservice.domain.vo.EventItemVo;
import com.mashang.eventservice.mapper.EventItemMapper;
import com.mashang.eventservice.mapping.EventItemMapping;
import com.mashang.eventservice.service.IEventItemService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;
import com.mashang.common.constants.CacheConstants;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class EventItemServiceImpl extends ServiceImpl<EventItemMapper, EventItem> implements IEventItemService {

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private EventItemMapper eventItemMapper;

    @Override
    public int addProject(EventItemQuery addQuery) {

        LambdaQueryWrapper<EventItem> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(EventItem::getItemName, addQuery.getItemName());

        if (eventItemMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("项目名称已经存在");
        }
        return eventItemMapper.insert(EventItemMapping.INSTANCE.toEntity(addQuery));
    }

    @Override
    public int updateProject(EventItemUpdate updateQuery) {

        LambdaUpdateWrapper<EventItem> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(EventItem::getItemId, updateQuery.getItemId())
                .setEntity(EventItemMapping.INSTANCE.toEntity(updateQuery));

        if (eventItemMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("项目名称已经存在");
        }

        return eventItemMapper.insert(EventItemMapping.INSTANCE.toEntity(updateQuery));
    }

    @Override
    public int deleteProject(Long itemId) {

        return eventItemMapper.deleteById(itemId);
    }

    @Override
    public List<EventItemVo> allItem() {

        List<EventItemVo> EventItemVoList = (List<EventItemVo>) redisTemplate.opsForValue().get(KeyCommon.buildKey());
        if (EventItemVoList != null) {
            return EventItemVoList;
        }
        List<EventItemVo> eventItemVos = eventItemMapper.allItem();

        //防止缓存穿透（数据库也没有）
        if (eventItemVos == null) {
            redisTemplate.opsForValue().set(KeyCommon.buildKey(), "NULL", 2, TimeUnit.MINUTES);
            return null;
        }
        //写缓存（设置过期时间）
        redisTemplate.opsForValue().set(
                KeyCommon.buildKey(),
                eventItemVos,
                1000,
                TimeUnit.HOURS
        );
        return eventItemMapper.allItem();
    }


}
