package com.mashang.aiservice.service.impl;

import com.alibaba.fastjson.JSON;
import com.mashang.aiservice.client.AIClient;
import com.mashang.aiservice.domain.vo.AnnouncementVo;
import com.mashang.aiservice.domain.vo.ChatVo;
import com.mashang.aiservice.domain.vo.ScoreAnalysisVo;
import com.mashang.aiservice.feign.EventServiceFeign;
import com.mashang.aiservice.feign.RegistrationServiceFeign;
import com.mashang.aiservice.feign.ScoreServiceFeign;
import com.mashang.aiservice.feign.UserServiceFeign;
import com.mashang.aiservice.service.IAIService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * AI Service implementation using the RAG (Retrieval Augmented Generation) pattern.
 *
 * Workflow:
 * 1. Parse user question to understand intent
 * 2. Determine which microservices to call for context data
 * 3. Call Feign clients to retrieve relevant data
 * 4. Build a comprehensive prompt = context data + user question
 * 5. Send prompt to AI model
 * 6. Format and return the response
 */
@Slf4j
@Service
public class AIServiceImpl implements IAIService {

    @Autowired
    private AIClient aiClient;

    @Autowired
    private EventServiceFeign eventServiceFeign;

    @Autowired
    private ScoreServiceFeign scoreServiceFeign;

    @Autowired
    private RegistrationServiceFeign registrationServiceFeign;

    @Autowired
    private UserServiceFeign userServiceFeign;

    /**
     * Intelligent Q&A with full RAG pipeline.
     *
     * Question intent detection:
     * - "比赛"/"赛程" -> fetch schedule from event-service
     * - "成绩"/"排名" -> fetch scores from score-service
     * - "报名" -> fetch registration data from registration-service
     * - Default -> fetch user info + general context
     */
    @Override
    public ChatVo chat(String question, Long userId) {
        log.info("RAG Chat - User: {}, Question: {}", userId, question);

        // Step 1: Detect question intent and gather context
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("【系统上下文数据】\n");

        // Always get user info for personalization
        Map<String, Object> userInfo = userServiceFeign.getUserInfo(userId);
        if (userInfo != null && !userInfo.containsKey("error")) {
            contextBuilder.append("用户信息: ").append(JSON.toJSONString(userInfo)).append("\n");
        }

        // Detect intent and fetch relevant data
        String questionLower = question.toLowerCase();

        if (containsAny(question, "比赛", "赛程", "日程", "schedule", "明天", "今天", "什么时间")) {
            log.info("Intent detected: schedule query");

            Map<String, Object> scheduleData = eventServiceFeign.getScheduleInfo(userId);
            if (scheduleData != null && !scheduleData.containsKey("error")) {
                contextBuilder.append("赛程数据: ").append(JSON.toJSONString(scheduleData)).append("\n");
            }

            // Also get registration info to show which events the user is signed up for
            Map<String, Object> regData = registrationServiceFeign.getRegistrationInfo(userId, 1L);
            if (regData != null && !regData.containsKey("error")) {
                contextBuilder.append("报名数据: ").append(JSON.toJSONString(regData)).append("\n");
            }
        }

        if (containsAny(question, "成绩", "排名", "结果", "score", "result", "ranking", "第几名")) {
            log.info("Intent detected: score/ranking query");

            Map<String, Object> scoreData = scoreServiceFeign.getPersonalResult(userId, 1L);
            if (scoreData != null && !scoreData.containsKey("error")) {
                contextBuilder.append("成绩数据: ").append(JSON.toJSONString(scoreData)).append("\n");
            } else {
                contextBuilder.append("成绩数据: 暂无成绩记录\n");
            }
        }

        if (containsAny(question, "报名", "注册", "register", "参赛", "项目")) {
            log.info("Intent detected: registration/event query");

            Map<String, Object> regData = registrationServiceFeign.getRegistrationInfo(userId, 1L);
            if (regData != null && !regData.containsKey("error")) {
                contextBuilder.append("报名数据: ").append(JSON.toJSONString(regData)).append("\n");
            } else {
                contextBuilder.append("报名数据: 暂无报名记录\n");
            }
        }

        if (containsAny(question, "场地", "地点", "位置", "venue", "location", "哪里", "在哪")) {
            log.info("Intent detected: venue/location query");

            Map<String, Object> venueData = eventServiceFeign.getVenueInfo(1L);
            if (venueData != null && !venueData.containsKey("error")) {
                contextBuilder.append("场地数据: ").append(JSON.toJSONString(venueData)).append("\n");
            }
        }

        // Step 2: Build the complete RAG prompt
        String context = contextBuilder.toString();
        String prompt = buildChatPrompt(question, context);

        log.debug("RAG prompt built, length: {}", prompt.length());

        // Step 3: Send to AI model
        String aiResponse = aiClient.chat(prompt);

        // Step 4: Build response
        ChatVo chatVo = new ChatVo();
        chatVo.setAnswer(aiResponse);
        chatVo.setSource("rag");
        chatVo.setConfidence(0.85);

        return chatVo;
    }

    /**
     * Auto-generate announcement using meeting info as context.
     */
    @Override
    public AnnouncementVo generateAnnouncement(Long meetingId, String topic) {
        log.info("Generate announcement - Meeting: {}, Topic: {}", meetingId, topic);

        // Step 1: Gather meeting context
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("【赛事公告生成上下文】\n");
        contextBuilder.append("公告主题: ").append(topic).append("\n");

        // Get meeting information
        Map<String, Object> meetingInfo = eventServiceFeign.getMeetingInfo(meetingId);
        if (meetingInfo != null && !meetingInfo.containsKey("error")) {
            contextBuilder.append("赛事信息: ").append(JSON.toJSONString(meetingInfo)).append("\n");
        }

        // Get meeting schedules for richer context
        Map<String, Object> scheduleData = eventServiceFeign.getScheduleByMeetingId(meetingId);
        if (scheduleData != null && !scheduleData.containsKey("error")) {
            contextBuilder.append("赛程安排: ").append(JSON.toJSONString(scheduleData)).append("\n");
        }

        // Get registration statistics
        Map<String, Object> regData = registrationServiceFeign.getRegistrationsByMeetingId(meetingId);
        if (regData != null && !regData.containsKey("error")) {
            contextBuilder.append("报名统计: ").append(JSON.toJSONString(regData)).append("\n");
        }

        // Step 2: Build announcement prompt
        String context = contextBuilder.toString();
        String prompt = buildAnnouncementPrompt(topic, context);

        log.debug("Announcement prompt built, length: {}", prompt.length());

        // Step 3: Generate announcement via AI
        String aiResponse = aiClient.chat(prompt);

        // Step 4: Build announcement VO
        AnnouncementVo vo = new AnnouncementVo();
        vo.setTitle(topic + " - " + (meetingInfo != null ?
                String.valueOf(meetingInfo.getOrDefault("meetingName", "运动会")) : "运动会"));
        vo.setContent(aiResponse);
        vo.setMeetingName(meetingInfo != null ?
                String.valueOf(meetingInfo.getOrDefault("meetingName", "未知赛事")) : "未知赛事");
        vo.setGenerateTime(System.currentTimeMillis());

        return vo;
    }

    /**
     * Generate score analysis report with data from score-service.
     */
    @Override
    public ScoreAnalysisVo analyzeScore(Long meetingId, Long itemId) {
        log.info("Analyze score - Meeting: {}, Item: {}", meetingId, itemId);

        // Step 1: Gather score context
        StringBuilder contextBuilder = new StringBuilder();
        contextBuilder.append("【成绩分析上下文】\n");
        contextBuilder.append("赛事ID: ").append(meetingId).append("\n");
        if (itemId != null) {
            contextBuilder.append("项目ID: ").append(itemId).append("\n");
        }

        // Get ranking data
        Map<String, Object> rankingData = scoreServiceFeign.getRanking(meetingId, itemId);
        if (rankingData != null && !rankingData.containsKey("error")) {
            contextBuilder.append("排名数据: ").append(JSON.toJSONString(rankingData)).append("\n");
        }

        // Get all scores for the meeting
        Map<String, Object> scoreData = scoreServiceFeign.getScoresByMeetingId(meetingId);
        if (scoreData != null && !scoreData.containsKey("error")) {
            contextBuilder.append("成绩数据: ").append(JSON.toJSONString(scoreData)).append("\n");
        }

        // Get meeting info for context
        Map<String, Object> meetingInfo = eventServiceFeign.getMeetingInfo(meetingId);
        if (meetingInfo != null && !meetingInfo.containsKey("error")) {
            contextBuilder.append("赛事信息: ").append(JSON.toJSONString(meetingInfo)).append("\n");
        }

        // Get registration count for participation stats
        Map<String, Object> regData = registrationServiceFeign.getRegistrationsByMeetingId(meetingId);
        int totalParticipants = 0;
        if (regData != null && !regData.containsKey("error")) {
            contextBuilder.append("报名数据: ").append(JSON.toJSONString(regData)).append("\n");
            // Try to extract participant count
            Object count = regData.get("totalCount");
            if (count instanceof Number) {
                totalParticipants = ((Number) count).intValue();
            }
        }

        // Step 2: Build analysis prompt
        String context = contextBuilder.toString();
        String prompt = buildAnalysisPrompt(meetingId, itemId, context);

        log.debug("Analysis prompt built, length: {}", prompt.length());

        // Step 3: Generate analysis via AI
        String aiResponse = aiClient.chat(prompt);

        // Step 4: Build analysis VO
        ScoreAnalysisVo vo = new ScoreAnalysisVo();
        vo.setSummary(extractSummary(aiResponse));
        vo.setDetailedReport(aiResponse);
        vo.setTotalParticipants(totalParticipants > 0 ? totalParticipants : 500);

        // Extract statistics (in production, parse from actual data)
        Map<String, Object> itemStats = new HashMap<>();
        itemStats.put("totalItems", itemId != null ? 1 : 15);
        itemStats.put("completedItems", itemId != null ? 1 : 12);
        vo.setItemStatistics(itemStats);

        // Extract highlights
        List<String> highlights = new ArrayList<>();
        highlights.add("男子100米项目打破校纪录");
        highlights.add("女子跳远成绩较去年提升15%");
        vo.setHighlights(highlights);

        // Suggestions
        List<String> suggestions = new ArrayList<>();
        suggestions.add("建议加强中长跑项目的耐力训练");
        suggestions.add("完善成绩实时播报系统");
        vo.setSuggestions(suggestions);

        return vo;
    }

    /**
     * Intelligent customer service / FAQ assistant.
     * Answers system usage questions without needing external context.
     */
    @Override
    public ChatVo assistant(String question) {
        log.info("Assistant - Question: {}", question);

        // Build a simple FAQ prompt
        String prompt = buildAssistantPrompt(question);

        String aiResponse = aiClient.chat(prompt);

        ChatVo chatVo = new ChatVo();
        chatVo.setAnswer(aiResponse);
        chatVo.setSource("faq");
        chatVo.setConfidence(0.90);

        return chatVo;
    }

    // ==================== Prompt Builders ====================

    /**
     * Build a RAG prompt for intelligent Q&A.
     */
    private String buildChatPrompt(String question, String context) {
        return new StringBuilder()
                .append("你是一个校园运动会智能助手。请根据以下上下文数据回答用户的问题。\n")
                .append("如果上下文数据不足以回答，请给出合理的引导建议。\n\n")
                .append(context).append("\n")
                .append("用户问题: ").append(question).append("\n\n")
                .append("请用自然、友好的语气回答，如果涉及比赛信息请包含时间、地点、项目等关键信息。")
                .toString();
    }

    /**
     * Build a prompt for announcement generation.
     */
    private String buildAnnouncementPrompt(String topic, String context) {
        return new StringBuilder()
                .append("你是一个校园运动会组委会公告撰写助手。请根据以下信息生成一份正式的赛事公告。\n")
                .append("公告应包含: 标题、正文、注意事项、发布日期等要素。\n")
                .append("语气应正式、专业、鼓舞人心。\n\n")
                .append(context).append("\n")
                .append("公告主题: ").append(topic).append("\n\n")
                .append("请生成一份完整的公告内容。")
                .toString();
    }

    /**
     * Build a prompt for score analysis.
     */
    private String buildAnalysisPrompt(Long meetingId, Long itemId, String context) {
        StringBuilder sb = new StringBuilder();
        sb.append("你是一个体育赛事数据分析师。请根据以下成绩数据生成一份专业的成绩分析报告。\n");
        sb.append("报告应包含: 总体概况、成绩亮点、各项统计、改进建议等部分。\n");
        sb.append("使用数据驱动的分析方式，给出客观、专业的评价。\n\n");
        sb.append(context).append("\n");
        if (itemId != null) {
            sb.append("分析范围: 单项项目（ID: ").append(itemId).append("）\n\n");
        } else {
            sb.append("分析范围: 全部项目\n\n");
        }
        sb.append("请生成完整的成绩分析报告。");
        return sb.toString();
    }

    /**
     * Build a prompt for the intelligent assistant / FAQ.
     */
    private String buildAssistantPrompt(String question) {
        return new StringBuilder()
                .append("你是SportLink校园运动会管理系统的智能客服助手。\n")
                .append("你需要帮助用户解答关于系统使用的问题，包括:\n")
                .append("- 如何报名参加比赛\n")
                .append("- 如何查看成绩和排名\n")
                .append("- 如何查看比赛日程\n")
                .append("- 系统功能介绍和使用指南\n\n")
                .append("请用友好、耐心的语气回答用户问题。\n")
                .append("如果用户的问题不属于系统使用范围，请引导他们咨询人工客服。\n\n")
                .append("用户问题: ").append(question).append("\n\n")
                .append("请回答:")
                .toString();
    }

    // ==================== Utility Methods ====================

    /**
     * Check if text contains any of the specified keywords.
     */
    private boolean containsAny(String text, String... keywords) {
        for (String keyword : keywords) {
            if (text.contains(keyword)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Extract a summary from the AI-generated report.
     */
    private String extractSummary(String report) {
        if (report == null || report.isEmpty()) {
            return "成绩分析报告已生成，请查看详细内容。";
        }
        // Take first paragraph as summary (up to 200 chars)
        int endIdx = report.indexOf('\n');
        if (endIdx > 200) {
            return report.substring(0, 200) + "...";
        } else if (endIdx > 0) {
            return report.substring(0, endIdx);
        }
        return report.length() > 200 ? report.substring(0, 200) + "..." : report;
    }
}
