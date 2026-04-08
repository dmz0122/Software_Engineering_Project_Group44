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
            List<String> lines = Files.readAllLines(path, StandardCharsets.UTF_8);
            List<List<String>> rows = new ArrayList<>();
            for (String line : lines) {
                if (!line.isBlank()) {
                    rows.add(parseLine(line));
                }
            }
            return rows;
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to read CSV: " + path, exception);
        }
    }

    public static void write(Path path, List<String> header, List<List<String>> rows) {
        try {
            Files.createDirectories(path.getParent());
            List<String> lines = new ArrayList<>();
            lines.add(formatLine(header));
            lines.addAll(rows.stream().map(CsvUtils::formatLine).toList());
            Files.write(path, lines, StandardCharsets.UTF_8);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to write CSV: " + path, exception);
        }
    }

    private static List<String> parseLine(String line) {
        List<String> fields = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;
        for (int index = 0; index < line.length(); index++) {
            char currentChar = line.charAt(index);
            if (currentChar == '"') {
                if (inQuotes && index + 1 < line.length() && line.charAt(index + 1) == '"') {
                    current.append('"');
                    index++;
                } else {
                    inQuotes = !inQuotes;
                }
            } else if (currentChar == ',' && !inQuotes) {
                fields.add(current.toString());
                current.setLength(0);
            } else {
                current.append(currentChar);
            }
        }
        fields.add(current.toString());
        return fields;
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
