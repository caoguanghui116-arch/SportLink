package com.mashang.aiservice.domain.query.create;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

/**
 * Chat query request for AI intelligent Q&A (RAG mode).
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "智能问答请求参数")
public class ChatQuery {

    @NotBlank(message = "问题不能为空")
    @Schema(description = "用户提出的自然语言问题", example = "我明天有什么比赛？", required = true)
    private String question;

    @NotNull(message = "用户ID不能为空")
    @Schema(description = "提问用户ID", example = "1001", required = true)
    private Long userId;
}
