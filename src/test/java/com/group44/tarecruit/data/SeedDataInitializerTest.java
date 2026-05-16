package com.group44.tarecruit.data;

import com.group44.tarecruit.model.ApplicationStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.Set;
import java.util.stream.Collectors;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SeedDataInitializerTest {
    @TempDir
    Path tempDir;

    @Test
    void demoApplicantDataIncludesAllVisibleStatuses() {
        new SeedDataInitializer(tempDir).ensureSeedData();

        Set<ApplicationStatus> amyStatuses = new ApplicationRepository(tempDir.resolve("applications.csv"))
                .findAll()
                .stream()
                .filter(application -> application.applicantId().equals("ta-amy"))
                .map(application -> application.status())
                .collect(Collectors.toSet());

        assertTrue(amyStatuses.contains(ApplicationStatus.UNDER_REVIEW));
        assertTrue(amyStatuses.contains(ApplicationStatus.SHORTLISTED));
        assertTrue(amyStatuses.contains(ApplicationStatus.INTERVIEW_SCHEDULED));
        assertTrue(amyStatuses.contains(ApplicationStatus.SELECTED));
        assertTrue(amyStatuses.contains(ApplicationStatus.REJECTED));
    }
}
