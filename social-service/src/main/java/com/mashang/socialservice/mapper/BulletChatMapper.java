package com.mashang.socialservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mashang.socialservice.domain.entity.BulletChat;
import com.mashang.socialservice.domain.vo.BulletChatVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface BulletChatMapper extends BaseMapper<BulletChat> {

    List<BulletChatVo> selectByMeetingId(@Param("meetingId") Long meetingId, @Param("limit") int limit);

}
