package com.mashang.userservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mashang.userservice.domain.entity.Announcement;
import com.mashang.userservice.domain.query.create.AnnouncementQuery;

import java.util.List;

public interface IAnnouncementService extends IService<Announcement> {

    int publish(AnnouncementQuery query, Long publisherId);

    int update(Announcement announcement);

    int delete(Long id);

    List<Announcement> listAll();

    List<Announcement> listByMeetingId(Long meetingId);

    Announcement detail(Long id);

    int publishDraft(Long id);
}
