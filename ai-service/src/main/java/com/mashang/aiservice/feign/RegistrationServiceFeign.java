package com.mashang.aiservice.feign;

import com.mashang.aiservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Feign client for registration-service.
 * Provides registration/enrollment data for RAG context.
 */
@FeignClient(
    name = "registration-service",
    configuration = FeignConfig.class,
    fallbackFactory = RegistrationServiceFeignFallbackFactory.class
)
public interface RegistrationServiceFeign {

    /**
     * Get registration information for a user in a meeting.
     */
    @GetMapping("/registration/info")
    Map<String, Object> getRegistrationInfo(
            @RequestParam("userId") Long userId,
            @RequestParam("meetingId") Long meetingId);

    /**
     * Count registered participants by item ID.
     */
    @GetMapping("/registration/count/{itemId}")
    Map<String, Object> countByItemId(@PathVariable("itemId") Long itemId);

    /**
     * Get all registrations for a meeting.
     */
    @GetMapping("/registration/meeting/{meetingId}")
    Map<String, Object> getRegistrationsByMeetingId(@PathVariable("meetingId") Long meetingId);
}
