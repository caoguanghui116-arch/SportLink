package com.mashang.registrationservice.feign;

import com.mashang.registrationservice.config.FeignConfig;
import com.mashang.registrationservice.domain.entity.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "event-service", configuration = FeignConfig.class)
public interface EventServiceFeign {

    @GetMapping("/basic/setup/item/{itemId}")
    R<Map<String, Object>> getItemInfo(@PathVariable("itemId") Long itemId);
}
