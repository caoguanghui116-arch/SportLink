package com.mashang.aiservice.controller;

import com.mashang.common.common.R;
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
 * AI 智能服务控制器 —— 提供基于 RAG（检索增强生成）模式的 AI 功能。
 *
 * 核心能力：
 * 1. 智能问答（RAG模式）：解析用户自然语言问题 → 从各微服务检索上下文数据 → AI 生成自然语言回答
 * 2. 自动公告生成：获取运动会信息 → AI 编写专业的赛事公告
 * 3. 成绩分析报告：获取成绩数据 → AI 分析并生成包含亮点、统计和改进建议的报告
 * 4. 智能客服：解答系统使用问题，引导用户完成报名和参赛流程
 *
 * RAG 工作流程：
 * 用户提问 "我明天有什么比赛？"
 * → AI 解析意图（查询赛程）
 * → 通过 Feign 调用 event-service 获取赛程数据
 * → 将赛程数据作为上下文注入 AI 提示词
 * → AI 生成自然语言回答 "明天上午9点你在体育馆A区参加100米预赛"
 *
 * 注意：
 * - 本服务使用 Knife4j 4.x（OpenAPI 3），注解为 @Tag/@Operation（非 Swagger2 的 @Api/@ApiOperation）
 * - 当依赖的微服务不可用时，Feign 降级工厂返回 error Map，AI 会诚实回复 "数据暂不可用"
 */
@Tag(name = "AI智能服务", description = "基于RAG模式的AI智能服务接口")
@RestController
@RequestMapping("/ai")
public class AIController {

    /** AI 服务接口，实际注入的是 AIServiceImpl（封装了 LLM 调用和上下文检索逻辑） */
    @Autowired
    private IAIService aiService;

    /**
     * 智能问答（RAG 模式）—— 核心接口。
     *
     * 处理流程：
     * 1. 接收用户的自然语言问题
     * 2. AI 解析问题意图（查询赛程/成绩/报名/场馆/...）
     * 3. 根据意图调用对应的微服务 Feign 接口获取上下文数据
     * 4. 将上下文数据 + 用户问题组装成 Prompt 发送给大模型
     * 5. 大模型生成自然语言回答后返回
     *
     * 示例问题：
     * - "我明天有什么比赛？" → 查赛程 → AI 格式化回答
     * - "我的100米成绩是多少？" → 查成绩 → AI 格式化回答
     * - "我在哪里比赛？" → 查场馆 → AI 格式化回答
     * - "现在团体总分排名如何？" → 查排名 → AI 格式化回答
     *
     * @param query 包含 question（用户问题）和 userId（当前用户ID，用于个性化查询）
     * @return AI 生成的回答（ChatVo 包含自然语言答案和引用的数据来源）
     */
    @Operation(summary = "智能问答（RAG模式）", description = "基于检索增强生成模式的智能问答，自动从各微服务获取上下文数据后由AI生成自然语言回答")
    @PostMapping("/chat")
    public R<ChatVo> chat(@RequestBody @Validated ChatQuery query) {
        ChatVo result = aiService.chat(query.getQuestion(), query.getUserId());
        return R.ok(result, "智能问答完成");
    }

    /**
     * 自动生成赛事公告。
     *
     * 处理流程：
     * 1. 根据 meetingId 调用 event-service 获取运动会详细信息
     * 2. 根据 topic（公告主题）构建专业 Prompt
     * 3. AI 生成格式规范的公告文本
     *
     * 使用场景：
     * - 管理员需要发布赛事延期通知
     * - 需要生成开幕式/闭幕式公告
     * - 需要发布比赛规则变更通知
     *
     * @param query 包含 meetingId（运动会ID）和 topic（公告主题/类型）
     * @return AI 生成的公告内容
     */
    @Operation(summary = "自动公告生成", description = "根据赛事信息自动生成专业的赛事公告内容")
    @PostMapping("/announcement/generate")
    public R<AnnouncementVo> generateAnnouncement(@RequestBody @Validated AnnouncementGenerateQuery query) {
        AnnouncementVo result = aiService.generateAnnouncement(query.getMeetingId(), query.getTopic());
        return R.ok(result, "公告生成成功");
    }

    /**
     * 成绩分析报告生成。
     *
     * 处理流程：
     * 1. 调用 score-service 获取指定运动会/项目的成绩数据
     * 2. 统计数据分布、最高分、平均分、标准差等指标
     * 3. AI 根据数据生成自然语言分析报告，包含：
     *    - 成绩亮点（破纪录、超出预期的表现）
     *    - 统计分析（各院系/团体对比）
     *    - 改进建议（针对落后项目的训练建议）
     *
     * @param query 包含 meetingId（运动会ID）和 itemId（项目ID，可选）
     * @return AI 生成的分析报告
     */
    @Operation(summary = "成绩分析报告", description = "基于成绩数据生成专业的分析报告，包含亮点、统计和改进建议")
    @PostMapping("/analysis/score")
    public R<ScoreAnalysisVo> analyzeScore(@RequestBody @Validated ScoreAnalysisQuery query) {
        ScoreAnalysisVo result = aiService.analyzeScore(query.getMeetingId(), query.getItemId());
        return R.ok(result, "成绩分析完成");
    }

    /**
     * 智能客服助手 —— 不需要外部数据上下文的 FAQ 问答。
     *
     * 与 /chat 的区别：
     * - /chat：RAG 模式，需要调用微服务获取数据后才能回答
     * - /assistant：纯知识问答，不调用外部服务，直接由 AI 回答
     *
     * 适用场景：
     * - "如何报名参加运动会？"
     * - "报名截止时间是什么时候？"
     * - "团体项目和个人项目可以同时参加吗？"
     * - "比赛规则是什么？"
     *
     * @param query 包含用户问题的请求体
     * @return AI 生成的客服回答
     */
    @Operation(summary = "智能客服", description = "解答系统使用问题，引导用户完成报名和参赛流程")
    @PostMapping("/assistant")
    public R<ChatVo> assistant(@RequestBody @Validated ChatQuery query) {
        ChatVo result = aiService.assistant(query.getQuestion());
        return R.ok(result, "智能客服回答完成");
    }
}
