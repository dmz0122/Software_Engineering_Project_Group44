package com.group44.tarecruit.service;

import com.group44.tarecruit.data.ProfileRepository;
import com.group44.tarecruit.model.ApplicantProfile;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProfileServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void savesProfileToCsv() {
        ProfileService service = new ProfileService(new ProfileRepository(tempDir.resolve("profiles.csv")));

        ApplicantProfile saved = service.saveProfile(new ApplicantProfile(
                "ta-new",
                "New Student",
                "20240003",
                "BSc Software Engineering",
                "Year 2",
                "Java, teamwork",
                "Mon / Fri",
                "3.6",
                "",
                "",
                ""
        ));

        assertEquals("New Student", saved.fullName());
        assertEquals("20240003", service.findProfile("ta-new").orElseThrow().studentId());
    }

    @Test
    void rejectsMissingFields() {
        ProfileService service = new ProfileService(new ProfileRepository(tempDir.resolve("profiles.csv")));

        assertThrows(IllegalArgumentException.class, () -> service.saveProfile(new ApplicantProfile(
                "ta-new",
                "",
                "20240003",
                "BSc Software Engineering",
                "Year 2",
                "Java, teamwork",
                "Mon / Fri",
                "3.6",
                "",
                "",
                ""
        )));
    }
}
