package com.group44.tarecruit.service;

import com.group44.tarecruit.data.ApplicationRepository;
import com.group44.tarecruit.data.JobRepository;
import com.group44.tarecruit.data.NotificationRepository;
import com.group44.tarecruit.data.ProfileRepository;
import com.group44.tarecruit.data.UserRepository;
import com.group44.tarecruit.model.ApplicantProfile;
import com.group44.tarecruit.model.ApplicationStatus;
import com.group44.tarecruit.model.JobApplication;
import com.group44.tarecruit.model.JobPosting;
import com.group44.tarecruit.model.NotificationItem;
import com.group44.tarecruit.model.Role;
import com.group44.tarecruit.model.UserAccount;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void selectingApplicantUpdatesStatusAndCreatesNotification() {
        ApplicationRepository applicationRepository = new ApplicationRepository(tempDir.resolve("applications.csv"));
        JobRepository jobRepository = new JobRepository(tempDir.resolve("jobs.csv"));
        ProfileRepository profileRepository = new ProfileRepository(tempDir.resolve("profiles.csv"));
        UserRepository userRepository = new UserRepository(tempDir.resolve("users.csv"));
        NotificationRepository notificationRepository = new NotificationRepository(tempDir.resolve("notifications.csv"));

        jobRepository.saveAll(List.of(new JobPosting(
                "job-1",
                "Programming TA",
                "CS101",
                "Introduction to Programming",
                "Semester A",
                "8",
                "Java basics, lab support",
                "Java|High demand",
                "Help in labs",
                2
        )));
        userRepository.saveAll(List.of(new UserAccount("ta-1", Role.APPLICANT, "Amy Parker", "amy@school.edu", "password123")));
        profileRepository.saveAll(List.of(new ApplicantProfile(
                "ta-1",
                "Amy Parker",
                "20240001",
                "BSc Computer Science",
                "Year 2",
                "Java, tutoring",
                "Mon/Wed",
                "3.8",
                "amy_cv.txt",
                "cv.txt",
                "2026-04-01T10:00:00"
        )));
        applicationRepository.saveAll(List.of(new JobApplication(
                "app-1",
                "job-1",
                "ta-1",
                ApplicationStatus.UNDER_REVIEW,
                "2026-04-01T10:00:00",
                ""
        )));

        ApplicationService service = new ApplicationService(
                applicationRepository,
                jobRepository,
                profileRepository,
                userRepository,
                new NotificationService(notificationRepository)
        );

        service.selectApplicant("app-1");

        assertEquals(ApplicationStatus.SELECTED, applicationRepository.findById("app-1").orElseThrow().status());
        NotificationItem notification = notificationRepository.findAll().getFirst();
        assertEquals("ta-1", notification.userId());
    }

    @Test
    void applyingForJobCreatesApplicationAndNotification() {
        ApplicationRepository applicationRepository = new ApplicationRepository(tempDir.resolve("applications-apply.csv"));
        JobRepository jobRepository = new JobRepository(tempDir.resolve("jobs-apply.csv"));
        ProfileRepository profileRepository = new ProfileRepository(tempDir.resolve("profiles-apply.csv"));
        UserRepository userRepository = new UserRepository(tempDir.resolve("users-apply.csv"));
        NotificationRepository notificationRepository = new NotificationRepository(tempDir.resolve("notifications-apply.csv"));

        jobRepository.saveAll(List.of(new JobPosting(
                "job-1",
                "Programming TA",
                "CS101",
                "Introduction to Programming",
                "Semester A",
                "8",
                "Java basics; lab support",
                "Java|Support",
                "Help in labs",
                2
        )));
        userRepository.saveAll(List.of(new UserAccount("ta-1", Role.APPLICANT, "Amy Parker", "amy@school.edu", "password123")));
        profileRepository.saveAll(List.of(new ApplicantProfile(
                "ta-1",
                "Amy Parker",
                "20240001",
                "BSc Computer Science",
                "Year 2",
                "Java, tutoring",
                "Mon/Wed",
                "3.8",
                "amy_cv.txt",
                "cv.txt",
                "2026-04-01T10:00:00"
        )));

        ApplicationService service = new ApplicationService(
                applicationRepository,
                jobRepository,
                profileRepository,
                userRepository,
                new NotificationService(notificationRepository)
        );

        JobApplication application = service.applyForJob("job-1", "ta-1");

        assertEquals(ApplicationStatus.APPLIED, application.status());
        assertTrue(service.hasApplicantApplied("ta-1", "job-1"));
        assertEquals(1, notificationRepository.findAll().size());
    }

    @Test
    void preventsSelectingMoreApplicantsThanOpenings() {
        ApplicationRepository applicationRepository = new ApplicationRepository(tempDir.resolve("applications-2.csv"));
        JobRepository jobRepository = new JobRepository(tempDir.resolve("jobs-2.csv"));
        ProfileRepository profileRepository = new ProfileRepository(tempDir.resolve("profiles-2.csv"));
        UserRepository userRepository = new UserRepository(tempDir.resolve("users-2.csv"));
        NotificationRepository notificationRepository = new NotificationRepository(tempDir.resolve("notifications-2.csv"));

        jobRepository.saveAll(List.of(new JobPosting(
                "job-1",
                "Programming TA",
                "CS101",
                "Introduction to Programming",
                "Semester A",
                "8",
                "Java basics, lab support",
                "Java|High demand",
                "Help in labs",
                1
        )));
        userRepository.saveAll(List.of(
                new UserAccount("ta-1", Role.APPLICANT, "Amy Parker", "amy@school.edu", "password123"),
                new UserAccount("ta-2", Role.APPLICANT, "Bob Chen", "bob@school.edu", "password123")
        ));
        profileRepository.saveAll(List.of(
                new ApplicantProfile("ta-1", "Amy Parker", "20240001", "CS", "Year 2", "Java", "Mon", "3.8", "", "", ""),
                new ApplicantProfile("ta-2", "Bob Chen", "20240002", "CS", "Year 2", "Java", "Tue", "3.7", "", "", "")
        ));
        applicationRepository.saveAll(List.of(
                new JobApplication("app-1", "job-1", "ta-1", ApplicationStatus.SELECTED, "2026-04-01T10:00:00", ""),
                new JobApplication("app-2", "job-1", "ta-2", ApplicationStatus.UNDER_REVIEW, "2026-04-01T11:00:00", "")
        ));

        ApplicationService service = new ApplicationService(
                applicationRepository,
                jobRepository,
                profileRepository,
                userRepository,
                new NotificationService(notificationRepository)
        );

        assertThrows(IllegalArgumentException.class, () -> service.selectApplicant("app-2"));
    }

    @Test
    void preventsDuplicateApplication() {
        ApplicationRepository applicationRepository = new ApplicationRepository(tempDir.resolve("applications-duplicate.csv"));
        JobRepository jobRepository = new JobRepository(tempDir.resolve("jobs-duplicate.csv"));
        ProfileRepository profileRepository = new ProfileRepository(tempDir.resolve("profiles-duplicate.csv"));
        UserRepository userRepository = new UserRepository(tempDir.resolve("users-duplicate.csv"));
        NotificationRepository notificationRepository = new NotificationRepository(tempDir.resolve("notifications-duplicate.csv"));

        jobRepository.saveAll(List.of(new JobPosting(
                "job-1",
                "Programming TA",
                "CS101",
                "Introduction to Programming",
                "Semester A",
                "8",
                "Java basics; lab support",
                "Java|Support",
                "Help in labs",
                2
        )));
        userRepository.saveAll(List.of(new UserAccount("ta-1", Role.APPLICANT, "Amy Parker", "amy@school.edu", "password123")));
        profileRepository.saveAll(List.of(new ApplicantProfile(
                "ta-1",
                "Amy Parker",
                "20240001",
                "BSc Computer Science",
                "Year 2",
                "Java, tutoring",
                "Mon/Wed",
                "3.8",
                "",
                "",
                "2026-04-01T10:00:00"
        )));
        applicationRepository.saveAll(List.of(new JobApplication(
                "app-1",
                "job-1",
                "ta-1",
                ApplicationStatus.APPLIED,
                "2026-04-01T10:00:00",
                ""
        )));

        ApplicationService service = new ApplicationService(
                applicationRepository,
                jobRepository,
                profileRepository,
                userRepository,
                new NotificationService(notificationRepository)
        );

        assertThrows(IllegalArgumentException.class, () -> service.applyForJob("job-1", "ta-1"));
    }
}
