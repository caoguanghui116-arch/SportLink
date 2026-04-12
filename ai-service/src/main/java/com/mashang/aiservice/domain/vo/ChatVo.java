package com.mashang.aiservice.domain.vo;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

/**
 * Chat response VO - AI intelligent Q&A response.
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "智能问答响应")
public class ChatVo {

    @Schema(description = "AI生成的回答内容")
    private String answer;

    @Schema(description = "回答来源（rag=检索增强生成, faq=知识库匹配, rule=规则匹配）")
    private String source;

    @Schema(description = "回答置信度 0.0~1.0")
    private Double confidence;
}
