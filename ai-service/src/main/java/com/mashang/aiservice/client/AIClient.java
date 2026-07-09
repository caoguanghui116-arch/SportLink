package com.mashang.aiservice.client;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Component;

/**
 * AI 客户端 —— 基于 Spring AI 封装大模型调用，屏蔽底层 API 差异。
 *
 * 架构设计（可插拔）：
 * - Spring AI 提供统一的 ChatModel 接口，切换模型只需改配置，不改变代码
 * - 当前接入：阿里云通义千问 (DashScope / qwen-plus)
 * - 可选替代：OpenAI (ChatGPT) / Google (Gemini) / 百度 (文心一言) / 本地 Ollama
 *
 * 切换模型方法：
 * 1. 修改 pom.xml 中的 Spring AI starter（如 spring-ai-openai-spring-boot-starter）
 * 2. 修改 application.yml 中的 spring.ai 配置（api-key, model, temperature）
 * 3. 代码无需任何改动 —— AIClient 只依赖 ChatClient 接口
 *
 * 为什么不用 RestTemplate 直接调 API：
 * - Spring AI 内置重试、速率限制、流式输出、Prompt 模板等工程化功能
 * - ChatClient 提供 Fluent API，代码简洁易维护
 * - 统一抽象层，未来切换模型零成本
 */
@Slf4j
@Component
public class AIClient {

    /** Spring AI ChatClient —— 核心对话入口 */
    private final ChatClient chatClient;

    /**
     * 构造器注入 ChatModel（由 Spring AI 自动配置，无需手动创建 Bean）
     * ChatClient 基于 ChatModel 构建，提供更高层的调用封装
     */
    public AIClient(ChatModel chatModel) {
        this.chatClient = ChatClient.builder(chatModel).build();
    }

    /**
     * 向大模型发送 prompt 并获取回复。
     *
     * 容错策略：
     * - AI 服务调用失败时返回友好的错误提示，而非抛出异常
     * - 避免因 AI 服务不可用导致整个接口 500 错误
     *
     * @param prompt 完整的 prompt（含 RAG 上下文 + 用户问题 + 指令约束）
     * @return AI 生成的回复文本；失败时返回错误提示
     */
    public String chat(String prompt) {
        try {
            String response = chatClient.prompt()
                    .user(prompt)           // 设置用户消息（包含完整 Prompt）
                    .call()                 // 同步调用（非流式）
                    .content();             // 提取回复文本
            log.info("AI response generated, length: {}",
                    response != null ? response.length() : 0);
            return response != null ? response : "抱歉，AI 服务暂时无法响应，请稍后再试。";
        } catch (Exception e) {
            log.error("AI API call failed", e);
            return "抱歉，AI 服务暂时不可用（" + e.getMessage() + "），请稍后再试或联系管理员。";
        }
    }
}
