package com.mashang.eventservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mashang.eventservice.domain.entity.EventItem;
import com.mashang.eventservice.domain.vo.EventItemVo;

import java.util.List;

public interface EventItemMapper extends BaseMapper<EventItem> {

    List<EventItemVo> allItem();

}
