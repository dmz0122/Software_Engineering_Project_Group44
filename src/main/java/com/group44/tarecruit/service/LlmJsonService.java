package com.group44.tarecruit.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

public interface LlmJsonService {
    Optional<JsonNode> completeJson(String cacheKey, String systemPrompt, String userPrompt);
}
