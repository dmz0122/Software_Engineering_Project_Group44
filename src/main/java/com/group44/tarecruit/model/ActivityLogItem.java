package com.group44.tarecruit.model;

public record ActivityLogItem(
        String id,
        String category,
        String actorUserId,
        String targetUserId,
        String title,
        String message,
        String createdAt
) {
}
