package com.mashang.aiservice.domain.query.create;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotNull;

/**
 * Query for AI-driven score analysis report.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "成绩分析报告请求参数")
public class ScoreAnalysisQuery {

    @NotNull(message = "赛事ID不能为空")
    @Schema(description = "赛事ID", example = "1", required = true)
    private Long meetingId;

    @Schema(description = "项目ID（可选，不传则分析全部项目）", example = "5")
    private Long itemId;
}
