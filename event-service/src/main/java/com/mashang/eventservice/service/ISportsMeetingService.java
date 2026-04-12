package com.mashang.eventservice.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.mashang.eventservice.domain.entity.SportsMeeting;
import com.mashang.eventservice.domain.query.create.BasicSetupQuery;
import org.springframework.web.bind.annotation.RequestBody;

public interface ISportsMeetingService extends IService<SportsMeeting> {

    /**
     * 赛事信息添加
     * @param addQuery 添加参数
     * @return 返回影响行数
     */
    int addMeeting(BasicSetupQuery addQuery);


}
