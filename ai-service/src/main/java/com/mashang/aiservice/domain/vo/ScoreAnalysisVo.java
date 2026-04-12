package com.mashang.aiservice.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;
import java.util.Map;

/**
 * Score analysis report response VO.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "成绩分析报告响应")
public class ScoreAnalysisVo {

    @Schema(description = "分析报告摘要")
    private String summary;

    @Schema(description = "详细分析内容（AI生成的自然语言报告）")
    private String detailedReport;

    @Schema(description = "参赛人数统计")
    private Integer totalParticipants;

    @Schema(description = "各项成绩统计 key=项目名称, value=统计信息")
    private Map<String, Object> itemStatistics;

    @Schema(description = "成绩趋势/亮点列表")
    private List<String> highlights;

    @Schema(description = "建议/改进点列表")
    private List<String> suggestions;
}
