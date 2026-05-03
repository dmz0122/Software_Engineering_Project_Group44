package com.group44.tarecruit.service;

import com.group44.tarecruit.data.ApplicationRepository;
import com.group44.tarecruit.data.JobRepository;
import com.group44.tarecruit.data.UserRepository;
import com.group44.tarecruit.model.ApplicationStatus;
import com.group44.tarecruit.model.JobApplication;
import com.group44.tarecruit.model.JobPosting;
import com.group44.tarecruit.model.Role;
import com.group44.tarecruit.model.UserAccount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;

class WorkloadServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void aggregatesSelectedAssignmentsBySemester() {
        ApplicationRepository applicationRepository = new ApplicationRepository(tempDir.resolve("applications.csv"));
        JobRepository jobRepository = new JobRepository(tempDir.resolve("jobs.csv"));
        UserRepository userRepository = new UserRepository(tempDir.resolve("users.csv"));

        jobRepository.saveAll(List.of(
                new JobPosting("job-1", "Programming TA", "CS101", "Programming", "Semester A", "8", "Java", "Java", "Support labs", 2),
                new JobPosting("job-2", "Maths Support TA", "MA102", "Maths", "Semester A", "4", "Excel", "Excel", "Support workshops", 1),
                new JobPosting("job-3", "Writing TA", "IS201", "Writing", "Semester B", "6", "Writing", "Writing", "Support writing", 1)
        ));
        userRepository.saveAll(List.of(
                new UserAccount("ta-1", Role.APPLICANT, "Amy Parker", "amy@school.edu", "password123"),
                new UserAccount("ta-2", Role.APPLICANT, "Bob Chen", "bob@school.edu", "password123")
        ));
        applicationRepository.saveAll(List.of(
                new JobApplication("app-1", "job-1", "ta-1", ApplicationStatus.SELECTED, "2026-04-01T10:00:00", ""),
                new JobApplication("app-2", "job-2", "ta-1", ApplicationStatus.SELECTED, "2026-04-01T11:00:00", ""),
                new JobApplication("app-3", "job-3", "ta-2", ApplicationStatus.SELECTED, "2026-04-01T12:00:00", "")
        ));

        WorkloadService service = new WorkloadService(applicationRepository, jobRepository, userRepository);

        List<WorkloadService.WorkloadSummary> semesterAWorkload = service.getWorkload("Semester A");
        List<WorkloadService.WorkloadSummary> allWorkload = service.getWorkload(WorkloadService.ALL_SEMESTERS_FILTER);

        assertEquals(1, semesterAWorkload.size());
        assertEquals("Amy Parker", semesterAWorkload.getFirst().displayName());
        assertEquals(12, semesterAWorkload.getFirst().totalHoursPerWeek());
        assertEquals(2, allWorkload.size());
    }

    @Test
    void currentSemesterFilterUsesCurrentDateInsteadOfAlphabeticalOrder() {
        ApplicationRepository applicationRepository = new ApplicationRepository(tempDir.resolve("applications-current.csv"));
        JobRepository jobRepository = new JobRepository(tempDir.resolve("jobs-current.csv"));
        UserRepository userRepository = new UserRepository(tempDir.resolve("users-current.csv"));

        jobRepository.saveAll(List.of(
                new JobPosting("job-1", "Writing TA", "IS201", "Writing", "Semester B", "6", "Writing", "Writing", "Support writing", 1),
                new JobPosting("job-2", "Programming TA", "CS101", "Programming", "Semester A", "8", "Java", "Java", "Support labs", 2)
        ));
        userRepository.saveAll(List.of(
                new UserAccount("ta-1", Role.APPLICANT, "Amy Parker", "amy@school.edu", "password123"),
                new UserAccount("ta-2", Role.APPLICANT, "Bob Chen", "bob@school.edu", "password123")
        ));
        applicationRepository.saveAll(List.of(
                new JobApplication("app-1", "job-1", "ta-1", ApplicationStatus.SELECTED, "2026-10-01T10:00:00", ""),
                new JobApplication("app-2", "job-2", "ta-2", ApplicationStatus.SELECTED, "2026-10-01T11:00:00", "")
        ));

        Clock octoberClock = Clock.fixed(Instant.parse("2026-10-15T00:00:00Z"), ZoneId.of("UTC"));
        WorkloadService service = new WorkloadService(applicationRepository, jobRepository, userRepository, octoberClock);

        List<WorkloadService.WorkloadSummary> currentWorkload = service.getWorkload(WorkloadService.CURRENT_SEMESTER_FILTER);

        assertEquals(1, currentWorkload.size());
        assertEquals("Amy Parker", currentWorkload.getFirst().displayName());
        assertEquals("Semester B", currentWorkload.getFirst().semester());
    }
}
