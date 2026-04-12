package com.mashang.scoreservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mashang.scoreservice.domain.entity.Award;
import com.mashang.scoreservice.domain.query.create.AwardQuery;
import com.mashang.scoreservice.domain.vo.AwardVo;

import java.util.List;

public interface IAwardService extends IService<Award> {

    /**
     * 添加奖项
     * @param query 奖项参数
     * @return 返回操作行数
     */
    int add(AwardQuery query);

    /**
     * 查询运动会奖项列表
     * @param meetingId 运动会id
     * @return 奖项列表
     */
    List<AwardVo> listByMeetingId(Long meetingId);

}
