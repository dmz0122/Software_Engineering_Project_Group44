package com.group44.tarecruit.data;

import com.group44.tarecruit.model.NotificationItem;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class NotificationRepository {
    private static final List<String> HEADER = List.of("id", "userId", "title", "message", "createdAt");

    private final Path filePath;

    public NotificationRepository(Path filePath) {
        this.filePath = filePath;
    }

    public List<NotificationItem> findAll() {
        List<List<String>> rows = CsvUtils.read(filePath);
        if (rows.isEmpty()) {
            return List.of();
        }
        List<NotificationItem> notifications = new ArrayList<>();
        for (int index = 1; index < rows.size(); index++) {
            List<String> row = rows.get(index);
            notifications.add(new NotificationItem(
                    valueAt(row, 0),
                    valueAt(row, 1),
                    valueAt(row, 2),
                    valueAt(row, 3),
                    valueAt(row, 4)
            ));
        }
        return notifications;
    }

    public void saveAll(List<NotificationItem> notifications) {
        List<List<String>> rows = notifications.stream()
                .map(notification -> List.of(
                        notification.id(),
                        notification.userId(),
                        notification.title(),
                        notification.message(),
                        notification.createdAt()
                ))
                .toList();
        CsvUtils.write(filePath, HEADER, rows);
    }

    public void append(NotificationItem notification) {
        List<NotificationItem> updated = new ArrayList<>(findAll());
        updated.add(notification);
        saveAll(updated);
    }

    private String valueAt(List<String> row, int index) {
        return index < row.size() ? row.get(index) : "";
    }
}
