package com.group44.tarecruit.service;

import com.group44.tarecruit.data.ActivityLogRepository;
import com.group44.tarecruit.model.ActivityLogItem;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class ActivityLogService {
    private final ActivityLogRepository activityLogRepository;

    public ActivityLogService(ActivityLogRepository activityLogRepository) {
        this.activityLogRepository = activityLogRepository;
    }

    public List<ActivityLogItem> getAllLogs() {
        return activityLogRepository.findAll().stream()
                .sorted(Comparator.comparing(ActivityLogItem::createdAt).reversed())
                .toList();
    }

    public List<ActivityLogItem> getRecentLogs(int limit) {
        return getAllLogs().stream()
                .limit(Math.max(limit, 0))
                .toList();
    }

    public void log(String category, String actorUserId, String targetUserId, String title, String message) {
        activityLogRepository.append(new ActivityLogItem(
                UUID.randomUUID().toString(),
                safe(category),
                safe(actorUserId),
                safe(targetUserId),
                safe(title),
                safe(message),
                LocalDateTime.now().toString()
        ));
    }

    private String safe(String value) {
        return value == null ? "" : value.trim();
    }
}
