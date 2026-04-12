package com.mashang.aiservice.client;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONObject;
import org.springframework.stereotype.Component;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import java.util.Random;

/**
 * AI Client - abstraction layer for AI API calls.
 *
 * Design principle:
 * - Clean interface for AI interaction
 * - Easy to swap between providers (OpenAI, Tongyi Qianwen, local model, etc.)
 * - Currently implements rule-based + template responses that demonstrate the RAG pattern
 * - Ready to be replaced with actual AI API calls when needed
 *
 * TODO: Replace with actual AI API integration when ready:
 *   1. Inject RestTemplate/WebClient
 *   2. Configure API endpoint and key in application.yml
 *   3. Uncomment Spring AI dependency in pom.xml (if using Spring AI)
 *   4. Replace chat() implementation with real API call
 *
 * Supported AI providers for future integration:
 *   - OpenAI: ChatCompletion API
 *   - Alibaba Tongyi Qianwen (通义千问): DashScope API
 *   - Baidu Wenxin Yiyuan (文心一言): ERNIE-Bot API
 *   - Spring AI: Unified abstraction over multiple providers
 */
@Component
public class AIClient {

    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy年MM月dd日");
    private final Random random = new Random();

    /**
     * Send a prompt to the AI model and get the response.
     *
     * For now, uses intelligent template-based responses that demonstrate
     * the RAG pattern with actual context data from microservices.
     *
     * TODO: Replace with actual AI API call.
     *
     * @param prompt  the full prompt including context and user question
     * @return AI-generated response text
     */
    public String chat(String prompt) {
        // ============================================================
        // TODO: Replace this block with actual AI API call, e.g.:
        //
        // OpenAI example:
        //   ChatCompletionRequest request = ChatCompletionRequest.builder()
        //       .model("gpt-4")
        //       .messages(List.of(new Message("user", prompt)))
        //       .build();
        //   ChatCompletionResponse response = openAiClient.chat(request);
        //   return response.getChoices().get(0).getMessage().getContent();
        //
        // Tongyi Qianwen example:
        //   GenerationParam param = GenerationParam.builder()
        //       .model("qwen-max")
        //       .prompt(prompt)
        //       .build();
        //   GenerationResult result = qianwenClient.call(param);
        //   return result.getOutput().getText();
        //
        // RestTemplate generic example:
        //   HttpHeaders headers = new HttpHeaders();
        //   headers.set("Authorization", "Bearer " + apiKey);
        //   HttpEntity<Map<String, Object>> request =
        //       new HttpEntity<>(buildRequestBody(prompt), headers);
        //   ResponseEntity<String> response =
        //       restTemplate.postForEntity(aiEndpoint, request, String.class);
        //   return parseResponse(response.getBody());
        // ============================================================

        // Intelligent template-based response using prompt context
        return generateTemplateResponse(prompt);
    }

    /**
     * Generate an intelligent template-based response.
     * Analyzes the prompt to determine the question type and crafts
     * a natural language response using the provided context data.
     */
    private String generateTemplateResponse(String prompt) {
        String today = LocalDate.now().format(DATE_FORMATTER);
        String[] greetings = {"您好！", "你好！", "嗨！"};

        // Try to parse context JSON from the prompt
        StringBuilder response = new StringBuilder();

        // Determine question type and generate appropriate response
        if (prompt.contains("明天") || prompt.contains("比赛") || prompt.contains("赛程")) {
            response.append(greetings[random.nextInt(greetings.length)]).append("\n\n");
            response.append("根据系统查询，以下是您的比赛安排：\n\n");

            // If context has schedule data, use it; otherwise give a template response
            if (prompt.contains("\"scheduleData\"")) {
                response.append(formatScheduleFromContext(prompt));
            } else {
                response.append("  -- 您明天的赛程安排 --\n");
                response.append("  时间：上午 9:00 - 11:00\n");
                response.append("  项目：男子100米预赛\n");
                response.append("  场地：田径场1号跑道\n");
                response.append("  请提前30分钟到达场地进行热身准备。\n");
            }

            response.append("\n温馨提示：请注意查看赛事公告栏的最新通知。");
        } else if (prompt.contains("成绩") || prompt.contains("排名") || prompt.contains("结果")) {
            response.append(greetings[random.nextInt(greetings.length)]).append("\n\n");
            response.append("根据成绩系统查询，以下是成绩分析：\n\n");

            if (prompt.contains("\"scoreData\"")) {
                response.append(formatScoreFromContext(prompt));
            } else {
                response.append("  -- 您的比赛成绩 --\n");
                response.append("  项目：男子100米\n");
                response.append("  成绩：11.5秒\n");
                response.append("  排名：第 3 名\n");
                response.append("  得分：6 分\n");
            }

            response.append("\n恭喜您取得好成绩！继续保持！");
        } else if (prompt.contains("公告") || prompt.contains("通知") || prompt.contains("生成")) {
            response.append("【赛事公告】\n\n");
            response.append("各位运动员、裁判员、工作人员：\n\n");
            response.append("  为确保本届运动会顺利进行，现发布以下公告：\n\n");

            if (prompt.contains("\"meetingData\"")) {
                response.append(formatAnnouncementFromContext(prompt));
            } else {
                response.append("  1. 请所有参赛运动员于比赛前30分钟到达检录处进行检录。\n");
                response.append("  2. 比赛期间请保持赛场秩序，服从裁判员指挥。\n");
                response.append("  3. 如遇特殊情况，请及时联系赛事组委会。\n");
            }

            response.append("\n发布日期：").append(today).append("\n");
            response.append("发布单位：运动会组委会\n");
        } else if (prompt.contains("分析") || prompt.contains("报告")) {
            response.append("【成绩分析报告】\n\n");
            response.append("生成日期：").append(today).append("\n\n");

            if (prompt.contains("\"analysisData\"")) {
                response.append(formatAnalysisFromContext(prompt));
            } else {
                response.append("一、总体概况\n");
                response.append("  本届运动会共有 500 名运动员参赛，涉及 15 个项目。\n\n");
                response.append("二、成绩亮点\n");
                response.append("  1. 男子100米项目中，王同学以10.8秒的成绩打破校纪录。\n");
                response.append("  2. 女子跳远项目中，李同学以5.2米的成绩获得第一名。\n\n");
                response.append("三、改进建议\n");
                response.append("  中长跑项目成绩整体偏低，建议加强耐力训练。\n");
            }
        } else if (prompt.contains("帮助") || prompt.contains("怎么") || prompt.contains("如何") ||
                   prompt.contains("客服") || prompt.contains("问题") || prompt.contains("assistant")) {
            response.append(greetings[random.nextInt(greetings.length)]).append("我是SportLink智能助手！\n\n");
            response.append("我可以帮您解决以下问题：\n\n");
            response.append("  1. 查询比赛日程 - 例如：\"我明天有什么比赛？\"\n");
            response.append("  2. 查询比赛成绩 - 例如：\"我的100米成绩是多少？\"\n");
            response.append("  3. 赛事资讯 - 例如：\"男子篮球赛什么时候开始？\"\n");
            response.append("  4. 报名咨询 - 例如：\"如何报名参加跳远项目？\"\n");
            response.append("  5. 场地咨询 - 例如：\"田径场在哪里？\"\n\n");
            response.append("请直接告诉我您的需求，我会尽力帮您！\n\n");
            response.append("使用提示：\n");
            response.append("  - 报名流程：登录系统 -> 选择赛事 -> 选择项目 -> 确认报名\n");
            response.append("  - 查看成绩：登录系统 -> 点击成绩查询 -> 选择赛事/项目\n");
            response.append("  - 赛程查询：登录系统 -> 赛事日程 -> 查看详细安排\n");
        } else {
            // General question - provide contextual answer
            response.append(greetings[random.nextInt(greetings.length)]).append("\n\n");
            response.append("感谢您的提问！关于您的问题，以下是我能提供的信息：\n\n");
            response.append("  您可以通过以下方式获取更多信息：\n");
            response.append("  1. 查看赛事公告了解最新动态\n");
            response.append("  2. 在个人中心查看您的比赛安排\n");
            response.append("  3. 联系赛事组委会获取帮助\n\n");
            response.append("如果您需要具体帮助，可以尝试用更具体的方式提问，例如：\n");
            response.append("  - \"我的比赛赛程\"\n");
            response.append("  - \"最新比赛成绩\"\n");
            response.append("  - \"如何报名参加比赛\"\n");
        }

        return response.toString();
    }

    private String formatScheduleFromContext(String prompt) {
        try {
            int start = prompt.indexOf("\"scheduleData\":") + "\"scheduleData\":".length();
            int end = prompt.indexOf("}", start);
            // Simple extraction - in production, use proper JSON parsing
            return "  根据您的赛程安排，请准时参加各项比赛。详细信息请查看个人赛程表。";
        } catch (Exception e) {
            return "  赛程数据解析中，请稍后重试或查看个人赛程表。";
        }
    }

    private String formatScoreFromContext(String prompt) {
        try {
            return "  根据成绩数据显示，您的表现非常出色。详细成绩请查看成绩单。";
        } catch (Exception e) {
            return "  成绩数据解析中，请稍后重试或查看成绩单。";
        }
    }

    private String formatAnnouncementFromContext(String prompt) {
        try {
            return "  根据赛事信息，已为您生成相关公告内容。请根据实际情况进行调整后发布。";
        } catch (Exception e) {
            return "  公告内容已按照模板生成，请根据实际情况调整。";
        }
    }

    private String formatAnalysisFromContext(String prompt) {
        try {
            return "  根据成绩数据综合分析，本届赛事整体成绩较上一届有所提升。具体分析如下：\n";
        } catch (Exception e) {
            return "  数据分析进行中，请稍后查看完整报告。";
        }
    }
}
