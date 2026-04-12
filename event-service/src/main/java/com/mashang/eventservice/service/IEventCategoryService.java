package com.mashang.eventservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mashang.eventservice.domain.entity.EventCategory;

import java.util.List;

public interface IEventCategoryService extends IService<EventCategory> {

    int addCategory(EventCategory category);

    List<EventCategory> listByMeetingId(Long meetingId);
}
