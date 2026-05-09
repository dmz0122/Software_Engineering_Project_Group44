package com.group44.tarecruit.service;

public record AiConfiguration(
        boolean enabled,
        String baseUrl,
        String apiKey,
        String model,
        int timeoutSeconds
) {
    public boolean isUsable() {
        return enabled
                && baseUrl != null
                && !baseUrl.isBlank()
                && apiKey != null
                && !apiKey.isBlank()
                && model != null
                && !model.isBlank();
    }
}
