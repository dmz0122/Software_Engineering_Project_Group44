package com.group44.tarecruit.service;

import com.group44.tarecruit.data.NotificationRepository;
import com.group44.tarecruit.model.NotificationItem;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class NotificationService {
    private final NotificationRepository notificationRepository;
    private final ActivityLogService activityLogService;

    public NotificationService(NotificationRepository notificationRepository) {
        this(notificationRepository, null);
    }

    public NotificationService(NotificationRepository notificationRepository, ActivityLogService activityLogService) {
        this.notificationRepository = notificationRepository;
        this.activityLogService = activityLogService;
    }

    public List<NotificationItem> getNotificationsForUser(String userId) {
        return notificationRepository.findAll().stream()
                .filter(item -> item.userId().equals(userId))
                .sorted(Comparator.comparing(NotificationItem::createdAt).reversed())
                .toList();
    }

    public void notifyUser(String userId, String title, String message) {
        NotificationItem notificationItem = new NotificationItem(
                UUID.randomUUID().toString(),
                userId,
                title,
                message,
                LocalDateTime.now().toString()
        );
        notificationRepository.append(notificationItem);
        if (activityLogService != null) {
            activityLogService.log("Notification", "", userId, title, message);
        }
    }
}
