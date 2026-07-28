package com.mashang.scoreservice.feign;

import com.mashang.common.common.R;
import com.mashang.scoreservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service",configuration = FeignConfig.class)
public interface UserServiceFeign {

    @GetMapping("/user/{userId}")
    R<Object> getUserById(@PathVariable("userId") Long userId);

    @GetMapping("/user/userId")
    Long getUserId();
}
