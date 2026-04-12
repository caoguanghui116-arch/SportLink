package com.mashang.aiservice.feign;

import com.mashang.aiservice.config.FeignConfig;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestParam;

import java.util.Map;

/**
 * Feign client for event-service.
 * Provides competition/event related data for RAG context.
 */
@FeignClient(
    name = "event-service",
    configuration = FeignConfig.class,
    fallbackFactory = EventServiceFeignFallbackFactory.class
)
public interface EventServiceFeign {

    /**
     * Get event item information by item ID.
     */
    @GetMapping("/basic/setup/item/{itemId}")
    Map<String, Object> getItemInfo(@PathVariable("itemId") Long itemId);

    /**
     * Get schedule information for a specific user.
     */
    @GetMapping("/schedule/user/{userId}")
    Map<String, Object> getScheduleInfo(@PathVariable("userId") Long userId);

    /**
     * Get venue information by venue ID.
     */
    @GetMapping("/basic/setup/venue/{venueId}")
    Map<String, Object> getVenueInfo(@PathVariable("venueId") Long venueId);

    /**
     * Get sports meeting information by meeting ID.
     */
    @GetMapping("/basic/setup/meeting/{meetingId}")
    Map<String, Object> getMeetingInfo(@PathVariable("meetingId") Long meetingId);

    /**
     * Get all schedules for a specific meeting.
     */
    @GetMapping("/schedule/meeting/{meetingId}")
    Map<String, Object> getScheduleByMeetingId(@PathVariable("meetingId") Long meetingId);
}
