package com.mashang.aiservice.controller;

import com.mashang.aiservice.domain.entity.R;
import com.mashang.aiservice.domain.query.create.AnnouncementGenerateQuery;
import com.mashang.aiservice.domain.query.create.ChatQuery;
import com.mashang.aiservice.domain.query.create.ScoreAnalysisQuery;
import com.mashang.aiservice.domain.vo.AnnouncementVo;
import com.mashang.aiservice.domain.vo.ChatVo;
import com.mashang.aiservice.domain.vo.ScoreAnalysisVo;
import com.mashang.aiservice.service.IAIService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.validation.annotation.Validated;
import org.springframework.web.bind.annotation.*;

/**
 * AI Controller - Provides AI-powered intelligent services using the RAG pattern.
 *
 * Core capabilities:
 * - Intelligent Q&A: Natural language questions answered with context from microservices
 * - Auto announcement generation: AI-generated meeting announcements
 * - Score analysis: AI-powered performance analysis and reporting
 * - Smart assistant: FAQ and system usage guidance
 */
@Tag(name = "AI智能服务", description = "基于RAG模式的AI智能服务接口")
@RestController
@RequestMapping("/ai")
public class AIController {

    @Autowired
    private IAIService aiService;

    /**
     * Intelligent Q&A endpoint (RAG mode).
     *
     * Flow: Parse question -> Fetch context from microservices -> AI generates answer
     *
     * Example questions:
     * - "我明天有什么比赛？" -> fetches schedule from event-service -> AI formats
     * - "我的100米成绩是多少？" -> fetches scores from score-service -> AI formats
     * - "我在哪里比赛？" -> fetches venue from event-service -> AI formats
     */
    @Operation(summary = "智能问答（RAG模式）", description = "基于检索增强生成模式的智能问答，自动从各微服务获取上下文数据后由AI生成自然语言回答")
    @PostMapping("/chat")
    public R<ChatVo> chat(@RequestBody @Validated ChatQuery query) {
        ChatVo result = aiService.chat(query.getQuestion(), query.getUserId());
        return R.ok(result, "智能问答完成");
    }

    /**
     * Auto announcement generation.
     *
     * Flow: Get meeting info from event-service -> AI generates professional announcement
     */
    @Operation(summary = "自动公告生成", description = "根据赛事信息自动生成专业的赛事公告内容")
    @PostMapping("/announcement/generate")
    public R<AnnouncementVo> generateAnnouncement(@RequestBody @Validated AnnouncementGenerateQuery query) {
        AnnouncementVo result = aiService.generateAnnouncement(query.getMeetingId(), query.getTopic());
        return R.ok(result, "公告生成成功");
    }

    /**
     * Score analysis report generation.
     *
     * Flow: Get scores from score-service -> AI analyzes and generates report
     */
    @Operation(summary = "成绩分析报告", description = "基于成绩数据生成专业的分析报告，包含亮点、统计和改进建议")
    @PostMapping("/analysis/score")
    public R<ScoreAnalysisVo> analyzeScore(@RequestBody @Validated ScoreAnalysisQuery query) {
        ScoreAnalysisVo result = aiService.analyzeScore(query.getMeetingId(), query.getItemId());
        return R.ok(result, "成绩分析完成");
    }

    /**
     * Intelligent customer service assistant.
     *
     * Flow: Answer system usage questions and guide through workflows
     * without needing external service context.
     */
    @Operation(summary = "智能客服", description = "解答系统使用问题，引导用户完成报名和参赛流程")
    @PostMapping("/assistant")
    public R<ChatVo> assistant(@RequestBody @Validated ChatQuery query) {
        ChatVo result = aiService.assistant(query.getQuestion());
        return R.ok(result, "智能客服回答完成");
    }
}
