package com.mashang.notificationservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.notificationservice.common.KeyCommon;
import com.mashang.notificationservice.domain.entity.Announcement;
import com.mashang.notificationservice.domain.query.create.AnnouncementQuery;
import com.mashang.notificationservice.domain.vo.AnnouncementVo;
import com.mashang.notificationservice.mapper.AnnouncementMapper;
import com.mashang.notificationservice.mapping.AnnouncementMapping;
import com.mashang.notificationservice.service.IAnnouncementService;
import com.mashang.notificationservice.utils.RedisUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Service
public class AnnouncementServiceImpl extends ServiceImpl<AnnouncementMapper, Announcement> implements IAnnouncementService {

    @Autowired
    private AnnouncementMapper announcementMapper;

    @Autowired
    private RedisUtil redisUtil;

    @Override
    public int publish(AnnouncementQuery query, Long publisherId) {
        Announcement announcement = AnnouncementMapping.INSTANCE.toEntity(query);
        announcement.setPublisherId(publisherId);
        announcement.setStatus(0L); // 默认为草稿
        announcement.setDelFlag(0L);
        int rows = announcementMapper.insert(announcement);
        // 清除缓存
        if (rows > 0) {
            clearAnnouncementCache();
        }
        return rows;
    }

    @Override
    public int update(Long announcementId, AnnouncementQuery query) {
        AnnouncementVo announcementVo = announcementMapper.selectById(announcementId);
        Announcement announcement = AnnouncementMapping.INSTANCE.entity(announcementVo);

        if (announcement == null) {
            throw new RuntimeException("公告不存在");
        }
        announcement.setTitle(query.getTitle());
        announcement.setContent(query.getContent());
        announcement.setMeetingId(query.getMeetingId());
        int rows = announcementMapper.updateById(announcement);
        // 清除缓存
        if (rows > 0) {
            clearAnnouncementCache();
        }
        return rows;
    }

    @Override
    public int delete(Long announcementId) {
        Announcement announcement = new Announcement();
        announcement.setAnnouncementId(announcementId);
        announcement.setDelFlag(1L);
        int rows = announcementMapper.updateById(announcement);
        // 清除缓存
        if (rows > 0) {
            clearAnnouncementCache();
        }
        return rows;
    }

    @Override
    public List<AnnouncementVo> listAll() {
        // 先从Redis获取缓存
        String cacheKey = KeyCommon.buildAnnouncementKey() + "all";
        List<AnnouncementVo> cachedList = redisUtil.getCacheObject(cacheKey);
        if (cachedList != null) {
            return cachedList;
        }

        List<AnnouncementVo> list = announcementMapper.selectAll();

        // 防止缓存穿透
        if (list == null || list.isEmpty()) {
            redisUtil.setCacheObject(cacheKey, "NULL", 2, TimeUnit.MINUTES);
            return list;
        }

        // 写缓存
        redisUtil.setCacheObject(cacheKey, list, 30, TimeUnit.MINUTES);
        return list;
    }

    @Override
    public List<AnnouncementVo> listByMeetingId(Long meetingId) {
        // 先从Redis获取缓存
        String cacheKey = KeyCommon.buildAnnouncementKey() + "meeting:" + meetingId;
        List<AnnouncementVo> cachedList = redisUtil.getCacheObject(cacheKey);
        if (cachedList != null) {
            return cachedList;
        }

        List<AnnouncementVo> list = announcementMapper.selectByMeetingId(meetingId);

        // 防止缓存穿透
        if (list == null || list.isEmpty()) {
            redisUtil.setCacheObject(cacheKey, "NULL", 2, TimeUnit.MINUTES);
            return list;
        }

        // 写缓存
        redisUtil.setCacheObject(cacheKey, list, 30, TimeUnit.MINUTES);
        return list;
    }

    @Override
    public AnnouncementVo detail(Long announcementId) {
        return announcementMapper.selectById(announcementId);
    }

    @Override
    public int publishDraft(Long announcementId) {
        LambdaUpdateWrapper<Announcement> wrapper = new LambdaUpdateWrapper<>();
        wrapper.eq(Announcement::getAnnouncementId, announcementId)
                .eq(Announcement::getStatus, 0L)
                .set(Announcement::getStatus, 1L);
        int rows = announcementMapper.update(null, wrapper);
        // 清除缓存
        if (rows > 0) {
            clearAnnouncementCache();
        }
        return rows;
    }

    /**
     * 清除公告相关缓存
     */
    private void clearAnnouncementCache() {
        String pattern = KeyCommon.buildAnnouncementKey() + "*";
        redisUtil.deleteObject(redisUtil.keys(pattern));
    }
}
