package com.mashang.registrationservice.feign;

import com.mashang.registrationservice.config.FeignConfig;
import com.mashang.registrationservice.domain.entity.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "user-service", configuration = FeignConfig.class)
public interface UserServiceFeign {

    @GetMapping("/user/info/{userId}")
    R<Map<String, Object>> getUserInfo(@PathVariable("userId") Long userId);
}
