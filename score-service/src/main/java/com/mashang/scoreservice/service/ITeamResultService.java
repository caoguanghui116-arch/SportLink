package com.mashang.scoreservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mashang.scoreservice.domain.entity.TeamResult;
import com.mashang.scoreservice.domain.query.create.TeamResultQuery;
import com.mashang.scoreservice.domain.vo.TeamResultVo;

import java.util.List;

public interface ITeamResultService extends IService<TeamResult> {

    /**
     * 团体成绩录入（录入/更新，自动计算排名）
     * @param query 成绩参数
     * @return 返回操作行数
     */
    int entry(TeamResultQuery query);

    /**
     * 查询项目团体成绩列表
     * @param itemId 项目id
     * @return 成绩列表
     */
    List<TeamResultVo> listByItemId(Long itemId);

}
