package com.group44.tarecruit.data;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CsvUtilsTest {
    @TempDir
    Path tempDir;

    @Test
    void preservesCommaQuoteAndNewlineFields() {
        Path csvPath = tempDir.resolve("edge-cases.csv");
        List<String> header = List.of("id", "content");
        List<List<String>> rows = List.of(
                List.of("comma", "TA role, lab support"),
                List.of("quote", "Applicant said \"ready\""),
                List.of("newline", "First line\nSecond line")
        );

        CsvUtils.write(csvPath, header, rows);

        assertEquals(List.of(header, rows.get(0), rows.get(1), rows.get(2)), CsvUtils.read(csvPath));
    }
}
