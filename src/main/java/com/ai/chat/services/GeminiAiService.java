package com.ai.chat.services;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

import com.ai.chat.models.ChatMessage;

@Service
public class GeminiAiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    @Value("${gemini.model}")
    private String model;

    private final RestTemplate restTemplate = new RestTemplate();

    public String askGemini(List<ChatMessage> history, String userMessage) {

        String url = "https://generativelanguage.googleapis.com/v1beta/models/"
                + model
                + ":generateContent?key="
                + apiKey;

        List<Map<String, Object>> contents = new ArrayList<>();

        // Previous chat history
        for (ChatMessage msg : history) {

            Map<String, Object> part = new HashMap<>();
            part.put("text", msg.getContent());

            Map<String, Object> content = new HashMap<>();

            // Gemini uses "user" and "model"
            String role = msg.getRole().equals("assistant") ? "model" : "user";

            content.put("role", role);
            content.put("parts", List.of(part));

            contents.add(content);
        }

        // Current user message
        Map<String, Object> userPart = new HashMap<>();
        userPart.put("text", userMessage);

        Map<String, Object> userContent = new HashMap<>();
        userContent.put("role", "user");
        userContent.put("parts", List.of(userPart));

        contents.add(userContent);

        // Request body
        Map<String, Object> body = new HashMap<>();
        body.put("contents", contents);

        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);

        HttpEntity<Map<String, Object>> entity =
                new HttpEntity<>(body, headers);

        ResponseEntity<Map> response =
                restTemplate.postForEntity(url, entity, Map.class);

        // Extract response text
        List candidates = (List) response.getBody().get("candidates");

        Map firstCandidate = (Map) candidates.get(0);

        Map content = (Map) firstCandidate.get("content");

        List parts = (List) content.get("parts");

        Map firstPart = (Map) parts.get(0);

        return firstPart.get("text").toString();
    }
}