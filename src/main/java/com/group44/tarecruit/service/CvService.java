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
    private static final Set<String> SUPPORTED_EXTENSIONS = Set.of("pdf");
    private static final Set<String> SUPPORTED_IMAGE_EXTENSIONS = Set.of("jpg", "jpeg", "png");
    private static final float PAGE_WIDTH = PDRectangle.A4.getWidth();
    private static final float PAGE_HEIGHT = PDRectangle.A4.getHeight();
    private static final float MARGIN = 56;
    private static final float BOTTOM_MARGIN = 72;

    private final Path uploadDirectory;

    public CvService(Path uploadDirectory) {
        this.uploadDirectory = uploadDirectory;
    }

    public StoredCv storeCv(Path sourceFile) {
        String fileName = sourceFile.getFileName().toString();
        String extension = extensionOf(fileName);
        if (!SUPPORTED_EXTENSIONS.contains(extension)) {
            throw new IllegalArgumentException("Unsupported CV format. Please upload a PDF file.");
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
            PDPageContentStream content = new PDPageContentStream(document, page);
            try {
                drawHeader(document, content, profile);

                PdfCursor cursor = new PdfCursor(page, content, 690);
                cursor = drawSection(document, cursor, "Profile",
                        "A teaching assistant candidate profile generated by TA Recruit on " + LocalDate.now() + ".");
                cursor = drawSection(document, cursor, "Skills", profile.skills());
                cursor = drawSection(document, cursor, "Availability", profile.availability());
                cursor = drawSection(document, cursor, "Academic Record", "GPA: " + profile.gpa());
                cursor = drawSection(document, cursor, "Notes",
                        "This resume was generated from the applicant profile and can be replaced by uploading a custom PDF CV.");

                cursor.content().setStrokingColor(219, 228, 242);
                cursor.content().moveTo(MARGIN, Math.max(BOTTOM_MARGIN, cursor.y() - 10));
                cursor.content().lineTo(PAGE_WIDTH - MARGIN, Math.max(BOTTOM_MARGIN, cursor.y() - 10));
                cursor.content().stroke();
                cursor.content().close();
            } catch (IOException | RuntimeException exception) {
                try {
                    content.close();
                } catch (IOException ignored) {
                    // Preserve the original PDF generation failure.
                }
                throw exception;
            }
            document.save(target.toFile());
        }
    }

    private void drawHeader(PDDocument document, PDPageContentStream content, ApplicantProfile profile) throws IOException {
        content.setNonStrokingColor(31, 65, 114);
        content.addRect(0, 720, PAGE_WIDTH, 122);
        content.fill();

        drawLine(content, PDType1Font.HELVETICA_BOLD, 26, safeText(profile.fullName()), MARGIN, 785, true);
        drawLine(content, PDType1Font.HELVETICA, 12,
                "Student ID: " + profile.studentId() + "   |   " + profile.programme() + "   |   " + profile.year(),
                MARGIN,
                758,
                true
        );
        drawAvatar(document, content, profile.avatarStoredPath());
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

    private PdfCursor drawSection(PDDocument document, PdfCursor cursor, String title, String body) throws IOException {
        cursor = ensureSpace(document, cursor, 52);
        drawLine(cursor.content(), PDType1Font.HELVETICA_BOLD, 15, title, MARGIN, cursor.y(), false);
        float y = cursor.y() - 20;
        cursor.content().setStrokingColor(219, 228, 242);
        cursor.content().moveTo(MARGIN, y + 8);
        cursor.content().lineTo(PAGE_WIDTH - MARGIN, y + 8);
        cursor.content().stroke();
        cursor = new PdfCursor(cursor.page(), cursor.content(), y);
        cursor = drawTextBlock(document, cursor, PDType1Font.HELVETICA, 11, body, MARGIN, 470, 15);
        return new PdfCursor(cursor.page(), cursor.content(), cursor.y() - 18);
    }

    private PdfCursor drawTextBlock(PDDocument document, PdfCursor cursor, PDFont font, int fontSize, String text, float x, float maxWidth, float leading) throws IOException {
        float y = cursor.y();
        for (String paragraph : safeText(text).split("\\R")) {
            List<String> lines = wrap(paragraph, font, fontSize, maxWidth);
            if (lines.isEmpty()) {
                y -= leading;
                continue;
            }
            for (String line : lines) {
                cursor = ensureSpace(document, new PdfCursor(cursor.page(), cursor.content(), y), leading + 4);
                y = cursor.y();
                drawLine(cursor.content(), font, fontSize, line, x, y, false);
                y -= leading;
            }
            y -= 4;
        }
        return new PdfCursor(cursor.page(), cursor.content(), y);
    }

    private PdfCursor ensureSpace(PDDocument document, PdfCursor cursor, float requiredHeight) throws IOException {
        if (cursor.y() - requiredHeight >= BOTTOM_MARGIN) {
            return cursor;
        }
        cursor.content().close();
        PDPage page = new PDPage(PDRectangle.A4);
        document.addPage(page);
        return new PdfCursor(page, new PDPageContentStream(document, page), PAGE_HEIGHT - MARGIN);
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

    private record PdfCursor(PDPage page, PDPageContentStream content, float y) {
    }
}
