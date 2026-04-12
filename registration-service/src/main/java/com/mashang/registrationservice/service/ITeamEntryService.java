package com.mashang.registrationservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mashang.registrationservice.domain.entity.TeamEntry;
import com.mashang.registrationservice.domain.query.create.TeamEntryQuery;
import com.mashang.registrationservice.domain.vo.TeamEntryVo;
import com.mashang.registrationservice.domain.vo.TeamMemberVo;

import java.util.List;

public interface ITeamEntryService extends IService<TeamEntry> {

    int enroll(TeamEntryQuery query);

    int addMember(Long teamEntryId, Long userId, Long captainId);

    int removeMember(Long memberId, Long captainId);

    int cancel(Long teamEntryId, Long captainId);

    List<TeamEntryVo> listByMeetingId(Long meetingId);

    TeamEntryVo detail(Long teamEntryId);

    List<TeamMemberVo> listMembers(Long teamEntryId);
}
