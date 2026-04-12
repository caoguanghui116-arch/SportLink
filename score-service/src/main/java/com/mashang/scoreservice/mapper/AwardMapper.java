package com.mashang.scoreservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mashang.scoreservice.domain.entity.Award;
import com.mashang.scoreservice.domain.vo.AwardVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface AwardMapper extends BaseMapper<Award> {

    /**
     * 根据运动会id查询奖项列表
     */
    List<AwardVo> selectByMeetingId(@Param("meetingId") Long meetingId);

}
