package com.mashang.aiservice.service;

import com.mashang.aiservice.domain.vo.AnnouncementVo;
import com.mashang.aiservice.domain.vo.ChatVo;
import com.mashang.aiservice.domain.vo.ScoreAnalysisVo;

/**
 * AI Service interface - RAG-based intelligent services.
 *
 * Key design pattern: Retrieval Augmented Generation (RAG)
 * 1. Accept user question/request
 * 2. Call relevant microservices via Feign to gather context data
 * 3. Build a comprehensive prompt with context + user question
 * 4. Send to AI model for natural language generation
 * 5. Return formatted response
 */
public interface IAIService {

    /**
     * Intelligent Q&A with RAG pattern.
     * Parses user question, determines which service data is needed,
     * fetches context, and generates a natural language answer.
     *
     * @param question  user's natural language question
     * @param userId    the ID of the user asking
     * @return chat response with answer, source, and confidence
     */
    ChatVo chat(String question, Long userId);

    /**
     * Auto-generate announcement text for a sports meeting.
     * Gets meeting info from event-service, then uses AI to generate
     * professional announcement content.
     *
     * @param meetingId  the meeting to generate announcement for
     * @param topic      announcement topic/type
     * @return generated announcement with title and content
     */
    AnnouncementVo generateAnnouncement(Long meetingId, String topic);

    /**
     * Generate score analysis report.
     * Gets score data from score-service, then uses AI to analyze
     * and produce insights, highlights, and suggestions.
     *
     * @param meetingId  the meeting to analyze
     * @param itemId     optional item filter (null for all items)
     * @return analysis report with summary, highlights, and suggestions
     */
    ScoreAnalysisVo analyzeScore(Long meetingId, Long itemId);

    /**
     * Intelligent customer service / FAQ assistant.
     * Answers system usage questions and guides users through
     * the registration and participation flow without needing
     * external service context.
     *
     * @param question  user's question about the system
     * @return chat response with answer
     */
    ChatVo assistant(String question);
}
