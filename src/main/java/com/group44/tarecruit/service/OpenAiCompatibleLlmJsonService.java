package com.group44.tarecruit.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class OpenAiCompatibleLlmJsonService implements LlmJsonService {
    private final AiConfiguration configuration;
    private final HttpClient httpClient;
    private final ObjectMapper objectMapper;
    private final Map<String, JsonNode> cache;

    public OpenAiCompatibleLlmJsonService(AiConfiguration configuration) {
        this(configuration,
                HttpClient.newBuilder()
                        .connectTimeout(Duration.ofSeconds(Math.max(configuration.timeoutSeconds(), 10)))
                        .build(),
                new ObjectMapper());
    }

    OpenAiCompatibleLlmJsonService(AiConfiguration configuration, HttpClient httpClient, ObjectMapper objectMapper) {
        this.configuration = configuration;
        this.httpClient = httpClient;
        this.objectMapper = objectMapper;
        this.cache = new ConcurrentHashMap<>();
    }

    @Override
    public Optional<JsonNode> completeJson(String cacheKey, String systemPrompt, String userPrompt) {
        if (!configuration.isUsable()) {
            return Optional.empty();
        }
        JsonNode cached = cache.get(cacheKey);
        if (cached != null) {
            return Optional.of(cached.deepCopy());
        }

        try {
            ObjectNode requestBody = objectMapper.createObjectNode();
            requestBody.put("model", configuration.model());
            requestBody.put("temperature", 0.2);
            requestBody.set("response_format", jsonResponseFormat());
            requestBody.set("messages", buildMessages(systemPrompt, userPrompt));

            HttpRequest request = HttpRequest.newBuilder()
                    .uri(URI.create(normalizeBaseUrl(configuration.baseUrl()) + "/v1/chat/completions"))
                    .timeout(Duration.ofSeconds(Math.max(configuration.timeoutSeconds(), 10)))
                    .header("Content-Type", "application/json")
                    .header("Authorization", "Bearer " + configuration.apiKey())
                    .POST(HttpRequest.BodyPublishers.ofString(objectMapper.writeValueAsString(requestBody)))
                    .build();

            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() < 200 || response.statusCode() >= 300) {
                return Optional.empty();
            }

            JsonNode root = objectMapper.readTree(response.body());
            JsonNode contentNode = root.path("choices").path(0).path("message").path("content");
            if (contentNode.isMissingNode() || contentNode.asText().isBlank()) {
                return Optional.empty();
            }

            JsonNode parsedContent = objectMapper.readTree(contentNode.asText());
            cache.put(cacheKey, parsedContent.deepCopy());
            return Optional.of(parsedContent);
        } catch (InterruptedException exception) {
            Thread.currentThread().interrupt();
            return Optional.empty();
        } catch (IOException exception) {
            return Optional.empty();
        }
    }

    private ObjectNode jsonResponseFormat() {
        ObjectNode responseFormat = objectMapper.createObjectNode();
        responseFormat.put("type", "json_object");
        return responseFormat;
    }

    private ArrayNode buildMessages(String systemPrompt, String userPrompt) {
        ArrayNode messages = objectMapper.createArrayNode();
        messages.add(message("system", systemPrompt));
        messages.add(message("user", userPrompt));
        return messages;
    }

    private ObjectNode message(String role, String content) {
        ObjectNode message = objectMapper.createObjectNode();
        message.put("role", role);
        message.put("content", content);
        return message;
    }

    private String normalizeBaseUrl(String baseUrl) {
        return baseUrl.endsWith("/") ? baseUrl.substring(0, baseUrl.length() - 1) : baseUrl;
    }
}
