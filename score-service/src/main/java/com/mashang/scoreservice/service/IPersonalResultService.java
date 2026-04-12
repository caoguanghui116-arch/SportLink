package com.mashang.scoreservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mashang.scoreservice.domain.entity.PersonalResult;
import com.mashang.scoreservice.domain.query.create.PersonalResultQuery;
import com.mashang.scoreservice.domain.vo.PersonalResultVo;
import com.mashang.scoreservice.domain.vo.RankingVo;

import java.util.List;

public interface IPersonalResultService extends IService<PersonalResult> {

    /**
     * 个人成绩录入（录入/更新，自动计算排名）
     * @param query 成绩参数
     * @return 返回操作行数
     */
    int entry(PersonalResultQuery query);

    /**
     * 批量成绩导入
     * @param queryList 成绩列表
     * @return 返回操作行数
     */
    int batchImport(List<PersonalResultQuery> queryList);

    /**
     * 查询项目个人成绩列表（按成绩降序）
     * @param itemId 项目id
     * @return 成绩列表
     */
    List<PersonalResultVo> listByItemId(Long itemId);

    /**
     * 查询用户个人成绩
     * @param userId 用户id
     * @return 成绩列表
     */
    List<PersonalResultVo> listByUserId(Long userId);

    /**
     * 查询排行榜（Redis Cache-Aside）
     * @param meetingId 运动会id
     * @return 排行榜列表
     */
    List<RankingVo> getRanking(Long meetingId);

}
