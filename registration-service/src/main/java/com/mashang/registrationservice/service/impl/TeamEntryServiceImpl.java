package com.mashang.registrationservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.registrationservice.domain.entity.TeamEntry;
import com.mashang.registrationservice.domain.entity.TeamMember;
import com.mashang.registrationservice.domain.query.create.TeamEntryQuery;
import com.mashang.registrationservice.domain.vo.TeamEntryVo;
import com.mashang.registrationservice.domain.vo.TeamMemberVo;
import com.mashang.registrationservice.mapper.TeamEntryMapper;
import com.mashang.registrationservice.mapper.TeamMemberMapper;
import com.mashang.registrationservice.mapping.PersonalEntryMapping;
import com.mashang.registrationservice.service.ITeamEntryService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
public class TeamEntryServiceImpl extends ServiceImpl<TeamEntryMapper, TeamEntry> implements ITeamEntryService {

    @Autowired
    private TeamEntryMapper teamEntryMapper;

    @Autowired
    private TeamMemberMapper teamMemberMapper;

    @Override
    @Transactional
    public int enroll(TeamEntryQuery query) {
        LambdaQueryWrapper<TeamEntry> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamEntry::getTeamName, query.getTeamName())
                .eq(TeamEntry::getMeetingId, query.getMeetingId())
                .eq(TeamEntry::getDelFlag, 0);

        if (teamEntryMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("团队名称已存在");
        }

        TeamEntry entry = PersonalEntryMapping.INSTANCE.toEntity(query);
        if (entry.getMaxMembers() == null) {
            entry.setMaxMembers(10);
        }
        entry.setStatus(1L);
        int rows = teamEntryMapper.insert(entry);

        TeamMember captain = new TeamMember();
        captain.setTeamEntryId(entry.getTeamEntryId());
        captain.setUserId(query.getCaptainId());
        teamMemberMapper.insert(captain);

        return rows;
    }

    @Override
    public int addMember(Long teamEntryId, Long userId, Long captainId) {
        TeamEntry team = teamEntryMapper.selectById(teamEntryId);
        if (team == null) {
            throw new RuntimeException("团队不存在");
        }
        if (!team.getCaptainId().equals(captainId)) {
            throw new RuntimeException("仅队长可添加成员");
        }

        int currentCount = teamMemberMapper.countByTeamEntryId(teamEntryId);
        if (currentCount >= team.getMaxMembers()) {
            throw new RuntimeException("团队人数已达上限");
        }

        LambdaQueryWrapper<TeamMember> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamMember::getTeamEntryId, teamEntryId)
                .eq(TeamMember::getUserId, userId)
                .eq(TeamMember::getDelFlag, 0);

        if (teamMemberMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("该成员已在团队中");
        }

        TeamMember member = new TeamMember();
        member.setTeamEntryId(teamEntryId);
        member.setUserId(userId);
        return teamMemberMapper.insert(member);
    }

    @Override
    public int removeMember(Long memberId, Long captainId) {
        TeamMember member = teamMemberMapper.selectById(memberId);
        if (member == null) {
            throw new RuntimeException("成员不存在");
        }

        TeamEntry team = teamEntryMapper.selectById(member.getTeamEntryId());
        if (!team.getCaptainId().equals(captainId)) {
            throw new RuntimeException("仅队长可移除成员");
        }
        if (team.getCaptainId().equals(member.getUserId())) {
            throw new RuntimeException("不能移除队长");
        }

        return teamMemberMapper.deleteById(memberId);
    }

    @Override
    public int cancel(Long teamEntryId, Long captainId) {
        TeamEntry team = teamEntryMapper.selectById(teamEntryId);
        if (team == null) {
            throw new RuntimeException("团队不存在");
        }
        if (!team.getCaptainId().equals(captainId)) {
            throw new RuntimeException("仅队长可取消报名");
        }

        team.setStatus(2L);
        return teamEntryMapper.updateById(team);
    }

    @Override
    public List<TeamEntryVo> listByMeetingId(Long meetingId) {
        return teamEntryMapper.selectByMeetingId(meetingId);
    }

    @Override
    public TeamEntryVo detail(Long teamEntryId) {
        TeamEntryVo vo = teamEntryMapper.selectDetailById(teamEntryId);
        if (vo != null) {
            vo.setMembers(teamMemberMapper.selectByTeamEntryId(teamEntryId));
        }
        return vo;
    }

    @Override
    public List<TeamMemberVo> listMembers(Long teamEntryId) {
        return teamMemberMapper.selectByTeamEntryId(teamEntryId);
    }
}
