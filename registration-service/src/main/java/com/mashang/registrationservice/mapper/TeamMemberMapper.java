package com.mashang.registrationservice.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.mashang.registrationservice.domain.entity.TeamMember;
import com.mashang.registrationservice.domain.vo.TeamMemberVo;
import org.apache.ibatis.annotations.Param;

import java.util.List;

public interface TeamMemberMapper extends BaseMapper<TeamMember> {

    List<TeamMemberVo> selectByTeamEntryId(@Param("teamEntryId") Long teamEntryId);

    int countByTeamEntryId(@Param("teamEntryId") Long teamEntryId);
}
