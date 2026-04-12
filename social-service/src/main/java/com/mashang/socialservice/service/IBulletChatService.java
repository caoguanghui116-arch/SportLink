package com.mashang.socialservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mashang.socialservice.domain.entity.BulletChat;
import com.mashang.socialservice.domain.query.create.BulletChatQuery;
import com.mashang.socialservice.domain.vo.BulletChatVo;

import java.util.List;

public interface IBulletChatService extends IService<BulletChat> {

    /**
     * 发送弹幕
     * @param bulletChatQuery 弹幕参数
     * @param userId 用户id
     * @return 返回弹幕VO
     */
    BulletChatVo send(BulletChatQuery bulletChatQuery, Long userId);

    /**
     * 查询运动会弹幕列表(最新100条)
     * @param meetingId 运动会id
     * @return 返回弹幕列表
     */
    List<BulletChatVo> listByMeetingId(Long meetingId);

}
