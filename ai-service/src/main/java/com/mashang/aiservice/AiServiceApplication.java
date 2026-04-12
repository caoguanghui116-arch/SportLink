package com.mashang.aiservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * AI Service - AI orchestration layer using RAG (Retrieval Augmented Generation) pattern.
 *
 * This service does NOT use database access (no MyBatis, no MySQL, no Redis).
 * It acts as an AI orchestration layer:
 * 1. Accepts user natural language questions
 * 2. Calls other microservices via Feign to gather context data
 * 3. Combines context with prompts and sends to AI model
 * 4. Returns natural language responses
 */
@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.mashang.aiservice.feign")
public class AiServiceApplication {

    public static void main(String[] args) {
        SpringApplication.run(AiServiceApplication.class, args);
    }

}
