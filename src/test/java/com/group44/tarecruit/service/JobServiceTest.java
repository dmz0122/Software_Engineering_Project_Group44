package com.group44.tarecruit.service;

import com.group44.tarecruit.data.JobRepository;
import com.group44.tarecruit.model.JobPosting;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class JobServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void createsJobAndPersistsItToCsv() {
        JobService service = new JobService(new JobRepository(tempDir.resolve("jobs.csv")));

        JobPosting savedJob = service.createJob(new JobPosting(
                "",
                "Programming TA",
                "cs101",
                "Introduction to Programming",
                "Semester A",
                "8",
                "Java basics, lab support",
                "",
                "Support first-year programming labs.",
                2
        ));

        assertEquals("CS101", savedJob.moduleCode());
        assertEquals(1, service.getAllJobs().size());
        assertEquals("Java basics; lab support", service.getAllJobs().getFirst().requiredSkills());
    }

    @Test
    void rejectsMissingRequiredField() {
        JobService service = new JobService(new JobRepository(tempDir.resolve("jobs.csv")));

        assertThrows(IllegalArgumentException.class, () -> service.createJob(new JobPosting(
                "",
                "",
                "CS101",
                "Introduction to Programming",
                "Semester A",
                "8",
                "Java basics",
                "",
                "Support first-year programming labs.",
                2
        )));
    }
}