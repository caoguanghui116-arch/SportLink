package com.mashang.scoreservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mashang.scoreservice.domain.entity.TeamResult;
import com.mashang.scoreservice.domain.vo.TeamResultVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TeamResultMapper extends BaseMapper<TeamResult> {

    /**
     * 根据项目id查询团体成绩列表
     */
    List<TeamResultVo> selectByItemId(@Param("itemId") Long itemId);

    /**
     * 根据运动会id查询团体成绩列表
     */
    List<TeamResultVo> selectByMeetingId(@Param("meetingId") Long meetingId);

}
