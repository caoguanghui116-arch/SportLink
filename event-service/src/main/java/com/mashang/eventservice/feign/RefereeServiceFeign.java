package com.mashang.eventservice.feign;

import com.mashang.eventservice.config.FeignConfig;
import com.mashang.eventservice.domain.entity.R;
import com.mashang.eventservice.domain.vo.RefereeVo;
import io.swagger.annotations.ApiOperation;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;

import java.util.List;

@FeignClient(name = "user-service",configuration = FeignConfig.class)
public interface RefereeServiceFeign {

    @GetMapping("/all")
    R<List<RefereeVo>> allReferee();


}
