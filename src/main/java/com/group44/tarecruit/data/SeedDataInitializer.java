package com.group44.tarecruit.data;

import com.group44.tarecruit.model.ApplicantProfile;
import com.group44.tarecruit.model.ApplicationStatus;
import com.group44.tarecruit.model.JobApplication;
import com.group44.tarecruit.model.JobPosting;
import com.group44.tarecruit.model.NotificationItem;
import com.group44.tarecruit.model.Role;
import com.group44.tarecruit.model.UserAccount;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class SeedDataInitializer {
    private final Path dataDirectory;

    public SeedDataInitializer(Path dataDirectory) {
        this.dataDirectory = dataDirectory;
    }

    public void ensureSeedData() {
        UserRepository userRepository = new UserRepository(dataDirectory.resolve("users.csv"));
        ProfileRepository profileRepository = new ProfileRepository(dataDirectory.resolve("profiles.csv"));
        JobRepository jobRepository = new JobRepository(dataDirectory.resolve("jobs.csv"));
        ApplicationRepository applicationRepository = new ApplicationRepository(dataDirectory.resolve("applications.csv"));
        NotificationRepository notificationRepository = new NotificationRepository(dataDirectory.resolve("notifications.csv"));

        ensureSeedUsers(userRepository);

        ensureSampleCv("amy_cv.txt", """
                Amy Parker
                Skills: Java, Python, tutoring
                Availability: Mon/Wed PM
                Experience: peer mentoring, lab support
                """);
        ensureSampleCv("bob_cv.txt", """
                Bob Chen
                Skills: Java, algorithms, lab support
                Availability: Tue/Thu
                Experience: coding clinic assistant
                """);

        if (profileRepository.findAll().isEmpty()) {
            String now = LocalDateTime.now().toString();
            profileRepository.saveAll(List.of(
                    new ApplicantProfile(
                            "ta-amy",
                            "Amy Parker",
                            "20240001",
                            "BSc Computer Science",
                            "Year 2",
                            "Java, Python, tutoring",
                            "Mon / Wed PM",
                            "3.8",
                            "amy_cv.txt",
                            dataDirectory.resolve("uploads").resolve("amy_cv.txt").toString(),
                            now
                    ),
                    new ApplicantProfile(
                            "ta-bob",
                            "Bob Chen",
                            "20240002",
                            "BSc Data Science",
                            "Year 3",
                            "Java, algorithms, lab support",
                            "Tue / Thu",
                            "3.7",
                            "bob_cv.txt",
                            dataDirectory.resolve("uploads").resolve("bob_cv.txt").toString(),
                            now
                    )
            ));
        }

        if (jobRepository.findAll().isEmpty()) {
            jobRepository.saveAll(List.of(
                    new JobPosting(
                            "job-programming",
                            "Programming TA",
                            "CS101",
                            "Introduction to Programming",
                            "Semester A",
                            "8",
                            "Java basics; can support lab sessions; available 8 hrs per week",
                            "Java|High demand",
                            "Support weekly labs, answer debugging questions and hold office hours for first-year students.",
                            2
                    ),
                    new JobPosting(
                            "job-maths",
                            "Maths Support TA",
                            "MA102",
                            "Foundations of Mathematics",
                            "Semester A",
                            "6",
                            "Excel basics; patient communication; workshop support",
                            "Excel|Core",
                            "Assist with maths workshops, help students practice problem sheets and prepare revision sessions.",
                            1
                    ),
                    new JobPosting(
                            "job-writing",
                            "Academic Writing TA",
                            "IS201",
                            "English for Academic Purposes",
                            "Semester A",
                            "4",
                            "Writing support; feedback literacy; communication",
                            "Writing|Support",
                            "Help students with referencing, essay structure and formative writing feedback.",
                            1
                    )
            ));
        }

        if (applicationRepository.findAll().isEmpty()) {
            applicationRepository.saveAll(List.of(
                    new JobApplication("app-amy-programming", "job-programming", "ta-amy", ApplicationStatus.UNDER_REVIEW, "2026-04-01T10:00:00", "Strong Java background"),
                    new JobApplication("app-bob-programming", "job-programming", "ta-bob", ApplicationStatus.UNDER_REVIEW, "2026-04-01T11:00:00", "Good lab support experience"),
                    new JobApplication("app-bob-maths", "job-maths", "ta-bob", ApplicationStatus.APPLIED, "2026-04-02T09:30:00", "Interested in workshop support")
            ));
        }

        if (notificationRepository.findAll().isEmpty()) {
            notificationRepository.saveAll(List.of(
                    new NotificationItem("note-amy-1", "ta-amy", "Application update", "Your Programming TA application is under review.", "2026-04-01T10:05:00"),
                    new NotificationItem("note-bob-1", "ta-bob", "Application update", "Your Programming TA application is under review.", "2026-04-01T11:05:00"),
                    new NotificationItem("note-new-1", "ta-new", "Welcome", "Create your applicant profile to start applying for TA roles.", "2026-04-03T09:00:00")
            ));
        }
    }

    private void ensureSeedUsers(UserRepository userRepository) {
        List<UserAccount> users = new ArrayList<>(userRepository.findAll());
        boolean changed = false;
        changed |= ensureUser(users, new UserAccount("ta-new", Role.APPLICANT, "New Student", "newta@school.edu", "password123"));
        changed |= ensureUser(users, new UserAccount("ta-amy", Role.APPLICANT, "Amy Parker", "amy@school.edu", "password123"));
        changed |= ensureUser(users, new UserAccount("ta-bob", Role.APPLICANT, "Bob Chen", "bob@school.edu", "password123"));
        changed |= ensureUser(users, new UserAccount("mo-olivia", Role.ORGANISER, "Olivia Moore", "mo@school.edu", "password123"));
        if (changed) {
            userRepository.saveAll(users);
        }
    }

    private boolean ensureUser(List<UserAccount> users, UserAccount userAccount) {
        boolean exists = users.stream().anyMatch(existing -> existing.email().equalsIgnoreCase(userAccount.email()));
        if (exists) {
            return false;
        }
        users.add(userAccount);
        return true;
    }

    private void ensureSampleCv(String fileName, String content) {
        try {
            Path uploadPath = dataDirectory.resolve("uploads").resolve(fileName);
            Files.createDirectories(uploadPath.getParent());
            if (!Files.exists(uploadPath)) {
                Files.writeString(uploadPath, content, StandardCharsets.UTF_8);
            }
        } catch (IOException exception) {
            throw new IllegalStateException("Failed to create sample CV: " + fileName, exception);
        }
    }
}
