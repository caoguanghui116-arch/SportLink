package com.mashang.registrationservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mashang.registrationservice.domain.entity.TeamEntry;
import com.mashang.registrationservice.domain.vo.TeamEntryVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TeamEntryMapper extends BaseMapper<TeamEntry> {

    List<TeamEntryVo> selectByMeetingId(@Param("meetingId") Long meetingId);

    TeamEntryVo selectDetailById(@Param("teamEntryId") Long teamEntryId);
}
