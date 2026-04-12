package com.mashang.eventservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mashang.eventservice.domain.entity.ScoreRule;

import java.util.List;

public interface IScoreRuleService extends IService<ScoreRule> {

    int add(ScoreRule scoreRule);

    int update(ScoreRule scoreRule);

    int delete(Long ruleId);

    List<ScoreRule> listByMeetingId(Long meetingId);
}
