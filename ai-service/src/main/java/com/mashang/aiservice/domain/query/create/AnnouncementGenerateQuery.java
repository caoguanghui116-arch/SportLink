package com.mashang.aiservice.domain.query.create;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Query for auto-generating announcements via AI.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "自动公告生成请求参数")
public class AnnouncementGenerateQuery {

    @NotNull(message = "赛事ID不能为空")
    @Schema(description = "赛事ID", example = "1", required = true)
    private Long meetingId;

    @NotBlank(message = "公告主题不能为空")
    @Schema(description = "公告主题/类型", example = "开幕式通知", required = true)
    private String topic;
}
