package com.mashang.userservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.userservice.domain.entity.Announcement;
import com.mashang.userservice.domain.query.create.AnnouncementQuery;
import com.mashang.userservice.domain.query.update.AnnouncementUpdateQuery;
import com.mashang.userservice.mapper.AnnouncementMapper;
import com.mashang.userservice.service.IAnnouncementService;
import com.mashang.userservice.utils.JWTUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements IAnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Override
    public int publish(AnnouncementQuery query) {

        Announcement announcement = new Announcement();
        announcement.setTitle(query.getTitle());
        announcement.setContent(query.getContent());
        announcement.setMeetingId(query.getMeetingId());
        announcement.setPublishUserId(JWTUtil.getUserId());
        announcement.setStatus(0L);
        return announcementMapper.insert(announcement);
    }

    @Override
    public int update(AnnouncementUpdateQuery announcement) {
        return announcementMapper.updateAnnouncement(announcement);
    }

    @Override
    public int delete(Long id) {
        return announcementMapper.deleteById(id);
    }

    @Override
    public List<Announcement> listAll() {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        return announcementMapper.selectList(wrapper);
    }

    @Override
    public List<Announcement> listByMeetingId(Long meetingId) {
        LambdaQueryWrapper<Announcement> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(Announcement::getMeetingId, meetingId)
                .eq(Announcement::getStatus, 1)
                .eq(Announcement::getDelFlag, 0)
                .orderByDesc(Announcement::getCreateTime);
        return announcementMapper.selectList(wrapper);
    }

    @Override
    public Announcement detail(Long id) {
        return announcementMapper.selectById(id);
    }

    @Override
    public int publishDraft(Long id) {
        Announcement announcement = announcementMapper.selectById(id);
        if (announcement == null) {
            throw new RuntimeException("公告不存在");
        }
        announcement.setStatus(1L);
        return announcementMapper.updateById(announcement);
    }
}
