package com.group44.tarecruit.service;

import com.fasterxml.jackson.databind.JsonNode;

import java.util.Optional;

public class DisabledLlmJsonService implements LlmJsonService {
    @Override
    public Optional<JsonNode> completeJson(String cacheKey, String systemPrompt, String userPrompt) {
        return Optional.empty();
    }
}
