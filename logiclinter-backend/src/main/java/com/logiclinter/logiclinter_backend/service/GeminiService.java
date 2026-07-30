package com.logiclinter.logiclinter_backend.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Service
public class GeminiService {

    @Value("${gemini.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String analyzeCode(String language, String codeSnippet) {
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-3.5-flash:generateContent?key=" + apiKey;
        String prompt = "Review this " + language + " code and return a JSON object with EXACTLY three fields: " +
                "\"mainBug\" (a brief 1-sentence description of the main error), " +
                "\"tip\" (a quick sentence on how to fix it), and " +
                "\"refactoredCode\" (the corrected code).\n\n" +
                "Code:\n" + codeSnippet;

        // Clean and escape string manually to ensure valid JSON format
        String escapedPrompt = prompt
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\b", "\\b")
            .replace("\f", "\\f")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t");

        String requestBody = "{\"contents\":[{\"parts\":[{\"text\":\"" + escapedPrompt + "\"}]}],\"generationConfig\":{\"response_mime_type\":\"application/json\"}}";

        try {
            HttpClient client = HttpClient.newHttpClient();
            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(endpoint))
                    .header("Content-Type", "application/json")
                    .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                    .build();

            HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
            
            // Parse the Gemini response JSON wrapper and extract only the inner text content
            JsonNode rootNode = objectMapper.readTree(response.body());
            
            if (rootNode.has("error")) {
                return response.body(); // Return the raw error if API returned one
            }

            JsonNode textNode = rootNode.path("candidates")
                                        .path(0)
                                        .path("content")
                                        .path("parts")
                                        .path(0)
                                        .path("text");

            if (!textNode.isMissingNode()) {
                return textNode.asText();
            } else {
                return "{\"error\": \"Invalid response structure received from AI engine.\"}";
            }

        } catch (Exception e) {
            return "{\"error\": \"Failed to connect to AI engine: " + e.getMessage() + "\"}";
        }
    }
}