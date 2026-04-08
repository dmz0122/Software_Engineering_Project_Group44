package com.group44.tarecruit.model;

public record NotificationItem(
        String id,
        String userId,
        String title,
        String message,
        String createdAt
) {
}
