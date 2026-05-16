package com.group44.tarecruit.data;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public final class CsvUtils {
    private CsvUtils() {
    }

    public static List<List<String>> read(Path path) {
        try {
            if (!Files.exists(path)) {
                return List.of();
            }
            return parseRecords(Files.readString(path, StandardCharsets.UTF_8));
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read CSV: " + path, exception);
        }
    }

    public static void write(Path path, List<String> header, List<List<String>> rows) {
        try {
            if (path.getParent() != null) {
                Files.createDirectories(path.getParent());
            }
            List<String> lines = new ArrayList<>();
            lines.add(formatLine(header));
            lines.addAll(rows.stream().map(CsvUtils::formatLine).toList());
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write CSV: " + path, exception);
        }
    }

    private static List<List<String>> parseRecords(String content) {
        List<List<String>> rows = new ArrayList<>();
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        boolean hasContent = false;

        for (int index = 0; index < content.length(); index++) {
            char currentChar = content.charAt(index);
            if (currentChar == '"') {
                hasContent = true;
                if (inQuotes && index + 1 < content.length() && content.charAt(index + 1) == '"') {
                    current.append('"');
                    index++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (currentChar == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
                hasContent = true;
            } else if ((currentChar == '\n' || currentChar == '\r') && !inQuotes) {
                if (currentChar == '\r' && index + 1 < content.length() && content.charAt(index + 1) == '\n') {
                    index++;
                }
                fields.add(current.toString());
                if (hasContent || fields.stream().anyMatch(value -> !value.isBlank())) {
                    rows.add(List.copyOf(fields));
                }
                fields.clear();
                current.setLength(0);
                hasContent = false;
            } else {
                current.append(currentChar);
                hasContent = true;
            }
        }

        fields.add(current.toString());
        if (hasContent || fields.stream().anyMatch(value -> !value.isBlank())) {
            rows.add(List.copyOf(fields));
        }
        return rows;
    }

    private static String formatLine(List<String> fields) {
        return fields.stream()
                .map(CsvUtils::escapeField)
                .collect(Collectors.joining(","));
    }

    private static String escapeField(String value) {
        String safe = value == null ? "" : value;
        if (safe.contains(",") || safe.contains("\"") || safe.contains("\n")) {
            return "\"" + safe.replace("\"", "\"\"") + "\"";
        }
        return safe;
    }
}
