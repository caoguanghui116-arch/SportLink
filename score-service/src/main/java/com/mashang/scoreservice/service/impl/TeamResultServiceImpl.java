package com.mashang.scoreservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.conditions.update.LambdaUpdateWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.scoreservice.common.KeyCommon;
import com.mashang.scoreservice.domain.entity.TeamResult;
import com.mashang.scoreservice.domain.query.create.TeamResultQuery;
import com.mashang.scoreservice.domain.vo.TeamResultVo;
import com.mashang.scoreservice.mapper.TeamResultMapper;
import com.mashang.scoreservice.mapping.ScoreMapping;
import com.mashang.scoreservice.service.ITeamResultService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TeamResultServiceImpl extends ServiceImpl<TeamResultMapper, TeamResult> implements ITeamResultService {

    @Autowired
    private TeamResultMapper teamResultMapper;

    @Override
    public int entry(TeamResultQuery query) {

        // 检查是否已有成绩记录（同一团队报名同一项目）
        LambdaQueryWrapper<TeamResult> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(TeamResult::getTeamEntryId, query.getTeamEntryId())
                .eq(TeamResult::getItemId, query.getItemId())
                .eq(TeamResult::getMeetingId, query.getMeetingId());

        TeamResult existing = teamResultMapper.selectOne(wrapper);

        int result;
        if (existing != null) {
            // 已存在，更新成绩
            LambdaUpdateWrapper<TeamResult> updateWrapper = new LambdaUpdateWrapper<>();
            updateWrapper.eq(TeamResult::getTeamResultId, existing.getTeamResultId())
                    .set(TeamResult::getScore, query.getScore());
            result = teamResultMapper.update(null, updateWrapper);
        } else {
            // 不存在，新增
            TeamResult entity = ScoreMapping.INSTANCE.toEntity(query);
            entity.setStatus(1L);
            result = teamResultMapper.insert(entity);
        }

        // 自动重新计算该项目排名
        recalculateTeamRanking(query.getItemId());

        return result;
    }

    @Override
    public List<TeamResultVo> listByItemId(Long itemId) {
        return teamResultMapper.selectByItemId(itemId);
    }

    /**
     * 重新计算项目团体排名（按成绩降序）
     */
    private void recalculateTeamRanking(Long itemId) {
        List<TeamResultVo> resultList = teamResultMapper.selectByItemId(itemId);
        if (resultList != null && !resultList.isEmpty()) {
            for (int i = 0; i < resultList.size(); i++) {
                TeamResultVo vo = resultList.get(i);
                LambdaUpdateWrapper<TeamResult> updateWrapper = new LambdaUpdateWrapper<>();
                updateWrapper.eq(TeamResult::getTeamResultId, vo.getTeamResultId())
                        .set(TeamResult::getRank, (long) (i + 1));
                teamResultMapper.update(null, updateWrapper);
            }
        }
    }

}
