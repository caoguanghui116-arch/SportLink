package com.mashang.aiservice.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Announcement generation response VO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "公告生成响应")
public class AnnouncementVo {

    @Schema(description = "生成的公告标题")
    private String title;

    @Schema(description = "生成的公告正文内容")
    private String content;

    @Schema(description = "适用的赛事名称")
    private String meetingName;

    @Schema(description = "公告生成时间戳")
    private Long generateTime;
}
