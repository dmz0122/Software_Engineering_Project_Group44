package com.group44.tarecruit.service;

import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public final class AiConfigurationLoader {
    private AiConfigurationLoader() {
    }

    public static AiConfiguration load() {
        Properties properties = new Properties();
        try (InputStream inputStream = AiConfigurationLoader.class.getClassLoader().getResourceAsStream("llm.properties")) {
            if (inputStream != null) {
                properties.load(inputStream);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to load llm.properties.", exception);
        }

        boolean enabled = Boolean.parseBoolean(read("ai.enabled", properties, "TA_RECRUIT_AI_ENABLED", "true"));
        String baseUrl = read("ai.baseUrl", properties, "TA_RECRUIT_AI_BASE_URL", "");
        String apiKey = read("ai.apiKey", properties, "TA_RECRUIT_AI_API_KEY", "");
        String model = read("ai.model", properties, "TA_RECRUIT_AI_MODEL", "gpt-4o");
        int timeoutSeconds = Integer.parseInt(read("ai.timeoutSeconds", properties, "TA_RECRUIT_AI_TIMEOUT_SECONDS", "45"));
        return new AiConfiguration(enabled, baseUrl, apiKey, model, timeoutSeconds);
    }

    private static String read(String propertyKey, Properties properties, String envKey, String fallback) {
        String envValue = System.getenv(envKey);
        if (envValue != null && !envValue.isBlank()) {
            return envValue.trim();
        }
        String propertyValue = properties.getProperty(propertyKey);
        if (propertyValue != null && !propertyValue.isBlank()) {
            return propertyValue.trim();
        }
        return fallback;
    }
}
