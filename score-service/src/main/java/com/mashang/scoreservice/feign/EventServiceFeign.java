package com.mashang.scoreservice.feign;

import com.mashang.scoreservice.config.FeignConfig;
import com.mashang.scoreservice.domain.entity.R;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.Map;

@FeignClient(name = "event-service", configuration = FeignConfig.class)
public interface EventServiceFeign {

    @GetMapping("/basic/setup/all/item")
    R<Object> allItem();

}
