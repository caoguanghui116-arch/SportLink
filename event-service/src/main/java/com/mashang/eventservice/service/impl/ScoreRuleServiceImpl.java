package com.mashang.eventservice.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.mashang.eventservice.domain.entity.ScoreRule;
import com.mashang.eventservice.mapper.ScoreRuleMapper;
import com.mashang.eventservice.service.IScoreRuleService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class ScoreRuleServiceImpl extends ServiceImpl<ScoreRuleMapper, ScoreRule> implements IScoreRuleService {

    @Autowired
    private ScoreRuleMapper scoreRuleMapper;

    @Override
    public int add(ScoreRule scoreRule) {
        LambdaQueryWrapper<ScoreRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScoreRule::getMeetingId, scoreRule.getMeetingId())
                .eq(ScoreRule::getRank, scoreRule.getRank());
        if (scoreRuleMapper.selectOne(wrapper) != null) {
            throw new RuntimeException("该名次的积分规则已存在");
        }
        return scoreRuleMapper.insert(scoreRule);
    }

    @Override
    public int update(ScoreRule scoreRule) {
        return scoreRuleMapper.updateById(scoreRule);
    }

    @Override
    public int delete(Long ruleId) {
        return scoreRuleMapper.deleteById(ruleId);
    }

    @Override
    public List<ScoreRule> listByMeetingId(Long meetingId) {
        LambdaQueryWrapper<ScoreRule> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(ScoreRule::getMeetingId, meetingId)
                .eq(ScoreRule::getDelFlag, 0)
                .orderByAsc(ScoreRule::getRank);
        return scoreRuleMapper.selectList(wrapper);
    }
}
