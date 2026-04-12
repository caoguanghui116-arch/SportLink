package com.mashang.notificationservice.feign;

import com.mashang.notificationservice.config.FeignConfig;
import com.mashang.notificationservice.domain.entity.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "user-service", configuration = FeignConfig.class)
public interface UserServiceFeign {

    /**
     * 根据用户ID获取用户信息（包含角色信息，用于权限校验）
     */
    @GetMapping("/user/info/{userId}")
    R getUserInfo(@PathVariable("userId") Long userId);

}
