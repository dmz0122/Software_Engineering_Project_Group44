package com.group44.tarecruit.service;

import com.group44.tarecruit.data.JobRepository;
import com.group44.tarecruit.data.SavedJobRepository;
import com.group44.tarecruit.model.JobPosting;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SavedJobServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void savesAndRemovesJobsForApplicant() {
        JobRepository jobRepository = new JobRepository(tempDir.resolve("jobs.csv"));
        jobRepository.saveAll(List.of(new JobPosting(
                "job-1",
                "Programming TA",
                "CS101",
                "Programming",
                "Semester A",
                "8",
                "Java",
                "Java",
                "Support labs",
                1
        )));
        SavedJobService service = new SavedJobService(new SavedJobRepository(tempDir.resolve("saved_jobs.csv")), jobRepository);

        service.saveJob("ta-1", "job-1");

        assertTrue(service.isSaved("ta-1", "job-1"));
        assertEquals("Programming TA", service.findSavedJobPostings("ta-1").getFirst().title());

        service.removeSavedJob("ta-1", "job-1");

        assertFalse(service.isSaved("ta-1", "job-1"));
    }

    @Test
    void rejectsSavingUnknownJob() {
        SavedJobService service = new SavedJobService(
                new SavedJobRepository(tempDir.resolve("saved_jobs.csv")),
                new JobRepository(tempDir.resolve("jobs.csv"))
        );

        assertThrows(IllegalArgumentException.class, () -> service.saveJob("ta-1", "missing"));
    }
}
