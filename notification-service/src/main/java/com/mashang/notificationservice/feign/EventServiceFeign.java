package com.mashang.notificationservice.feign;

import com.mashang.notificationservice.config.FeignConfig;
import com.mashang.common.common.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "event-service", configuration = FeignConfig.class)
public interface EventServiceFeign {

    /**
     * 查询赛事详情（用于推送比赛提醒通知）
     */
    @GetMapping("/schedule/detail/{scheduleId}")
    R getScheduleDetail(@PathVariable("scheduleId") Long scheduleId);

}
