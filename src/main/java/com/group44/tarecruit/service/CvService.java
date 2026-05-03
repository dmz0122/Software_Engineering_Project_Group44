package com.group44.tarecruit.service;

import com.group44.tarecruit.model.ApplicantProfile;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;

import java.awt.Desktop;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import java.util.UUID;

public class CvService {
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("pdf", "doc", "docx", "txt");
    private static final Set<String> SUPPORTED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png");

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

    public StoredCv storeAvatar(Path sourceFile) {
        String fileName = sourceFile.getFileName().toString();
        String extension = extensionOf(fileName);
        if (!SUPPORTED_IMAGE_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Unsupported photo format. Please upload JPG or PNG.");
        }

        try {
            Files.createDirectories(uploadDirectory);
            String storedName = UUID.randomUUID() + "_avatar_" + fileName;
            Path target = uploadDirectory.resolve(storedName);
            Files.copy(sourceFile, target, StandardCopyOption.REPLACE_EXISTING);
            return new StoredCv(fileName, target.toString());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to store profile photo.", exception);
        }
    }

    public StoredCv generateResumePdf(ApplicantProfile profile) {
        String fileName = safeFileName(profile.fullName().isBlank() ? "resume" : profile.fullName()) + "_resume.pdf";
        try {
            Files.createDirectories(uploadDirectory);
            Path target = uploadDirectory.resolve(UUID.randomUUID() + "_" + fileName);
            writeResumePdf(profile, target);
            return new StoredCv(fileName, target.toString());
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to generate resume PDF.", exception);
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

    private void writeResumePdf(ApplicantProfile profile, Path target) throws IOException {
        try (PDDocument document = new PDDocument()) {
            PDPage page = new PDPage(PDRectangle.A4);
            document.addPage(page);
            try (PDPageContentStream content = new PDPageContentStream(document, page)) {
                float margin = 56;
                float y = 760;

                content.setNonStrokingColor(31, 65, 114);
                content.addRect(0, 720, PDRectangle.A4.getWidth(), 122);
                content.fill();

                drawLine(content, PDType1Font.HELVETICA_BOLD, 26, safeText(profile.fullName()), margin, 785, true);
                drawLine(content, PDType1Font.HELVETICA, 12,
                        "Student ID: " + profile.studentId() + "   |   " + profile.programme() + "   |   " + profile.year(),
                        margin,
                        758,
                        true
                );

                drawAvatar(document, content, profile.avatarStoredPath());

                y = drawSection(content, "Profile", "A teaching assistant candidate profile generated by TA Recruit on " + LocalDate.now() + ".", margin, y);
                y = drawSection(content, "Skills", profile.skills(), margin, y);
                y = drawSection(content, "Availability", profile.availability(), margin, y);
                y = drawSection(content, "Academic Record", "GPA: " + profile.gpa(), margin, y);
                y = drawSection(content, "Notes", "This resume was generated from the applicant profile and can be replaced by uploading a custom PDF CV.", margin, y);

                content.setStrokingColor(219, 228, 242);
                content.moveTo(margin, Math.max(70, y - 10));
                content.lineTo(PDRectangle.A4.getWidth() - margin, Math.max(70, y - 10));
                content.stroke();
            }
            document.save(target.toFile());
        }
    }

    private void drawAvatar(PDDocument document, PDPageContentStream content, String avatarPath) {
        if (avatarPath == null || avatarPath.isBlank()) {
            return;
        }
        try {
            Path path = Path.of(avatarPath);
            if (Files.exists(path)) {
                PDImageXObject image = PDImageXObject.createFromFile(path.toString(), document);
                content.drawImage(image, 462, 742, 78, 78);
            }
        } catch (IOException | RuntimeException ignored) {
            // The resume still remains useful if the optional photo cannot be embedded.
        }
    }

    private float drawSection(PDPageContentStream content, String title, String body, float x, float y) throws IOException {
        drawLine(content, PDType1Font.HELVETICA_BOLD, 15, title, x, y, false);
        y -= 20;
        content.setStrokingColor(219, 228, 242);
        content.moveTo(x, y + 8);
        content.lineTo(PDRectangle.A4.getWidth() - x, y + 8);
        content.stroke();
        return drawTextBlock(content, PDType1Font.HELVETICA, 11, body, x, y, 470, 15) - 18;
    }

    private float drawTextBlock(PDPageContentStream content, PDFont font, int fontSize, String text, float x, float y, float maxWidth, float leading) throws IOException {
        for (String paragraph : safeText(text).split("\\R")) {
            List<String> lines = wrap(paragraph, font, fontSize, maxWidth);
            if (lines.isEmpty()) {
                y -= leading;
                continue;
            }
            for (String line : lines) {
                drawLine(content, font, fontSize, line, x, y, false);
                y -= leading;
            }
            y -= 4;
        }
        return y;
    }

    private void drawLine(PDPageContentStream content, PDFont font, int fontSize, String text, float x, float y, boolean lightText) throws IOException {
        content.beginText();
        content.setFont(font, fontSize);
        content.setNonStrokingColor(lightText ? java.awt.Color.WHITE : new java.awt.Color(38, 52, 78));
        content.newLineAtOffset(x, y);
        content.showText(safePdfText(text));
        content.endText();
    }

    private List<String> wrap(String text, PDFont font, int fontSize, float maxWidth) throws IOException {
        List<String> lines = new ArrayList<>();
        StringBuilder current = new StringBuilder();
        for (String word : safePdfText(text).split("\\s+")) {
            String candidate = current.isEmpty() ? word : current + " " + word;
            float width = font.getStringWidth(candidate) / 1000 * fontSize;
            if (width > maxWidth && !current.isEmpty()) {
                lines.add(current.toString());
                current = new StringBuilder(word);
            } else {
                current = new StringBuilder(candidate);
            }
        }
        if (!current.isEmpty()) {
            lines.add(current.toString());
        }
        return lines;
    }

    private String safeText(String value) {
        return value == null || value.isBlank() ? "Not provided" : value.trim();
    }

    private String safePdfText(String value) {
        return safeText(value)
                .replace('\u2022', '-')
                .replace('\u2014', '-')
                .replaceAll("[^\\x20-\\x7E]", "?");
    }

    private String safeFileName(String value) {
        return value.trim().replaceAll("[^A-Za-z0-9._-]+", "_");
    }

    public record StoredCv(String originalFileName, String storedPath) {
    }
}
