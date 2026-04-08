package com.group44.tarecruit.service;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class CvService {
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("pdf", "doc", "docx", "txt");

    private final Path uploadDirectory;

    public CvService(Path uploadDirectory) {
        this.uploadDirectory = uploadDirectory;
    }

    public StoredCv storeCv(Path sourceFile) {
        String fileName = sourceFile.getFileName().toString();
        String extension = extensionOf(fileName);
        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Unsupported CV format. Please upload PDF, DOC, DOCX or TXT.");
        }

        try {
            Files.createDirectories(uploadDirectory);
            String storedName = UUID.randomUUID() + "_" + fileName;
            Path target = uploadDirectory.resolve(storedName);
            Files.copy(sourceFile, target, StandardCopyOption.REPLACE_EXISTING);
            return new StoredCv(fileName, target.toString());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to store CV file.", exception);
        }
    }

    public void openCv(String storedPath) {
        try {
            Path path = Path.of(storedPath);
            if (!Files.exists(path)) {
                throw new IllegalArgumentException("The stored CV file could not be found.");
            }
            if (Desktop.isDesktopSupported()) {
                Desktop.getDesktop().open(path.toFile());
            } else {
                throw new IllegalStateException("Desktop integration is not available on this device.");
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to open the CV file.", exception);
        }
    }

    public void exportCv(String storedPath, Path targetPath) {
        try {
            Files.copy(Path.of(storedPath), targetPath, StandardCopyOption.REPLACE_EXISTING);
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to export the CV file.", exception);
        }
    }

    private String extensionOf(String fileName) {
        int separator = fileName.lastIndexOf('.');
        if (separator < 0 || separator == fileName.length() - 1) {
            return "";
        }
        return fileName.substring(separator + 1).toLowerCase(Locale.ROOT);
    }

    public record StoredCv(String originalFileName, String storedPath) {
    }
}
