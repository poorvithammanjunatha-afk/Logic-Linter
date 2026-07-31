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

    @Value("${ai.api.key}")
    private String apiKey;

    private final ObjectMapper objectMapper = new ObjectMapper();

    public String analyzeCode(String language, String codeSnippet) {
        String endpoint = "https://generativelanguage.googleapis.com/v1beta/models/gemini-2.0-flash:generateContent?key=" + apiKey;
        
        // Clean and optimize payload to reduce token consumption
        String trimmedCode = (codeSnippet == null) ? "" : codeSnippet.replaceAll("(?m)^[ \t]*\r?\n", "").trim();

        String prompt = "Review this " + language + " code and return a JSON object with EXACTLY three fields: " +
                "\"mainBug\" (a brief 1-sentence description of the main error), " +
                "\"tip\" (a quick sentence on how to fix it), and " +
                "\"refactoredCode\" (the corrected code).\n\n" +
                "Code:\n" + trimmedCode;

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

        int maxRetries = 3;
        long waitTimeMillis = 3000; // Start with a 3-second delay for backoff

        for (int attempt = 1; attempt <= maxRetries; attempt++) {
            try {
                HttpClient client = HttpClient.newHttpClient();
                HttpRequest request = HttpRequest.newBuilder()
                        .uri(URI.create(endpoint))
                        .header("Content-Type", "application/json")
                        .POST(HttpRequest.BodyPublishers.ofString(requestBody))
                        .build();

                HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());
                
                // Parse the Gemini response JSON wrapper
                JsonNode rootNode = objectMapper.readTree(response.body());
                
                // If a 429 error is returned inside the body payload, handle it for retry
                if (response.statusCode() == 429 || rootNode.has("error")) {
                    JsonNode errorNode = rootNode.path("error");
                    int errorCode = errorNode.path("code").asInt(response.statusCode());
                    
                    if (errorCode == 429 && attempt < maxRetries) {
                        Thread.sleep(waitTimeMillis);
                        waitTimeMillis *= 2; // Exponential backoff scaling
                        continue;
                    }
                    
                    return response.body(); // Return raw error if retries are exhausted
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

            } catch (InterruptedException ie) {
                Thread.currentThread().interrupt();
                return "{\"error\": \"Request interrupted during retry wait: " + ie.getMessage() + "\"}";
            } catch (Exception e) {
                if (attempt == maxRetries) {
                    return "{\"error\": \"Failed to connect to AI engine after retries: " + e.getMessage() + "\"}";
                }
                try {
                    Thread.sleep(waitTimeMillis);
                    waitTimeMillis *= 2;
                } catch (InterruptedException ie) {
                    Thread.currentThread().interrupt();
                    return "{\"error\": \"Thread interrupted: " + ie.getMessage() + "\"}";
                }
            }
        }
        
        return "{\"error\": \"Rate limit exceeded (429). Please try again later.\"}";
    }
}