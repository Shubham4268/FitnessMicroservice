package com.example.aiservice.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import java.util.Map;

@Service
@Slf4j
public class GeminiService {
    private final WebClient webClient;

    @Value("${GEMINI_API_URL}")
    private String geminiApiUrl;

    @Value("${GEMINI_API_KEY}")
    private String geminiApiKey;

    public GeminiService(WebClient.Builder webClientBuilder) {
        this.webClient = webClientBuilder.build();
    }

    public String getAnswer(String question){
        Map<String,Object> requestBody = Map.of(
                "contents", new Object[]{
                        Map.of("parts",new Object[]{
                                Map.of("text", question)
                        })
                }
        );
        String response = webClient.post()
                .uri(geminiApiUrl + geminiApiKey)
                .header("Content-Type", "application/json")
                .bodyValue(requestBody)
                .retrieve()
                .bodyToMono(String.class)
                .block();
        return response;
    }
}