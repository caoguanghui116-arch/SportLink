package com.mashang.socialservice.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.socialservice.domain.entity.BulletChat;
import com.mashang.socialservice.domain.entity.R;
import com.mashang.socialservice.domain.query.create.BulletChatQuery;
import com.mashang.socialservice.domain.vo.BulletChatVo;
import com.mashang.socialservice.feign.UserServiceFeign;
import com.mashang.socialservice.mapper.BulletChatMapper;
import com.mashang.socialservice.mapping.BulletChatMapping;
import com.mashang.socialservice.service.IBulletChatService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Service
public class BulletChatServiceImpl extends ServiceImpl<BulletChatMapper, BulletChat> implements IBulletChatService {

    private static final String CACHE_KEY_PREFIX = "social:bullet:";
    private static final int BULLET_LIMIT = 100;
    private static final long CACHE_TTL = 5;

    @Autowired
    private BulletChatMapper bulletChatMapper;

    @Autowired
    private RedisTemplate<String, Object> redisTemplate;

    @Autowired
    private UserServiceFeign userServiceFeign;

    @Override
    public BulletChatVo send(BulletChatQuery bulletChatQuery, Long userId) {
        BulletChat bulletChat = BulletChatMapping.INSTANCE.toEntity(bulletChatQuery);
        bulletChat.setUserId(userId);

        bulletChatMapper.insert(bulletChat);

        // 清除缓存，下一次查询时重新加载
        String cacheKey = CACHE_KEY_PREFIX + bulletChatQuery.getMeetingId();
        redisTemplate.delete(cacheKey);

        BulletChatVo vo = new BulletChatVo();
        vo.setChatId(bulletChat.getChatId());
        vo.setMeetingId(bulletChat.getMeetingId());
        vo.setUserId(bulletChat.getUserId());
        vo.setContent(bulletChat.getContent());
        vo.setCreateTime(bulletChat.getCreateTime());
        vo.setUsername(getUsername(userId));

        return vo;
    }

    @Override
    public List<BulletChatVo> listByMeetingId(Long meetingId) {
        String cacheKey = CACHE_KEY_PREFIX + meetingId;

        // 先查Redis缓存
        List<BulletChatVo> cachedList = (List<BulletChatVo>) redisTemplate.opsForValue().get(cacheKey);
        if (cachedList != null) {
            return cachedList;
        }

        // 查数据库(最新100条)
        List<BulletChatVo> list = bulletChatMapper.selectByMeetingId(meetingId, BULLET_LIMIT);

        // 填充用户名
        if (list != null) {
            list.forEach(vo -> vo.setUsername(getUsername(vo.getUserId())));
        }

        // 写入缓存(5分钟过期)
        if (list != null) {
            redisTemplate.opsForValue().set(cacheKey, list, CACHE_TTL, TimeUnit.MINUTES);
        }

        return list;
    }

    /**
     * 通过Feign获取用户名
     */
    private String getUsername(Long userId) {
        try {
            R<Map<String, Object>> result = userServiceFeign.getUserInfo(userId);
            if (result != null && result.getData() != null && result.getData().get("username") != null) {
                return result.getData().get("username").toString();
            }
        } catch (Exception e) {
            // feign调用失败，返回默认值
        }
        return "用户" + userId;
    }

}
