package com.mashang.socialservice.feign;

import com.mashang.socialservice.config.FeignConfig;
import com.mashang.socialservice.domain.entity.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "user-service", configuration = FeignConfig.class)
public interface UserServiceFeign {

    @GetMapping("/user/info/{userId}")
    R<Map<String, Object>> getUserInfo(@PathVariable("userId") Long userId);

}
