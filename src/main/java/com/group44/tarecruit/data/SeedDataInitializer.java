package com.group44.tarecruit.data;

import com.group44.tarecruit.model.ActivityLogItem;
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
        ActivityLogRepository activityLogRepository = new ActivityLogRepository(dataDirectory.resolve("activity_logs.csv"));

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
                            "",
                            "",
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
                            "",
                            "",
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
        ensureDemoApplicationStates(applicationRepository, jobRepository, profileRepository, notificationRepository);

        if (activityLogRepository.findAll().isEmpty()) {
            activityLogRepository.saveAll(List.of(
                    new ActivityLogItem("log-1", "Job", "mo-olivia", "", "Job posted", "Programming TA (CS101) was published for Semester A.", "2026-03-28T14:00:00"),
                    new ActivityLogItem("log-2", "Notification", "", "ta-amy", "Application update", "Your Programming TA application is under review.", "2026-04-01T10:05:00"),
                    new ActivityLogItem("log-3", "Notification", "", "ta-bob", "Application update", "Your Programming TA application is under review.", "2026-04-01T11:05:00"),
                    new ActivityLogItem("log-4", "Profile", "ta-amy", "ta-amy", "Profile saved", "Applicant profile details were created or updated.", "2026-04-02T09:30:00"),
                    new ActivityLogItem("log-5", "Notification", "", "ta-new", "Welcome", "Create your applicant profile to start applying for TA roles.", "2026-04-03T09:00:00")
            ));
        }
    }

    private void ensureDemoApplicationStates(
            ApplicationRepository applicationRepository,
            JobRepository jobRepository,
            ProfileRepository profileRepository,
            NotificationRepository notificationRepository
    ) {
        List<JobPosting> jobs = new ArrayList<>(jobRepository.findAll());
        List<ApplicantProfile> profiles = new ArrayList<>(profileRepository.findAll());
        List<JobApplication> applications = new ArrayList<>(applicationRepository.findAll());
        List<NotificationItem> notifications = new ArrayList<>(notificationRepository.findAll());
        boolean changedJobs = false;
        boolean changedProfiles = false;
        boolean changedApplications = false;
        boolean changedNotifications = false;

        changedJobs |= ensureJob(jobs, new JobPosting(
                "job-data",
                "Data Analytics TA",
                "DS205",
                "Applied Data Analytics",
                "Semester B",
                "5",
                "Python data analysis; dashboard support; clear communication",
                "Python|Analytics",
                "Support data labs, answer analysis questions and help students interpret dashboard outputs.",
                1
        ));
        changedJobs |= ensureJob(jobs, new JobPosting(
                "job-research",
                "Research Methods TA",
                "RM301",
                "Research Methods",
                "Semester B",
                "6",
                "Research design; feedback literacy; interview facilitation",
                "Research|Methods",
                "Help run seminars, support research planning clinics and coordinate small-group feedback.",
                1
        ));
        changedJobs |= ensureJob(jobs, new JobPosting(
                "job-systems",
                "Systems Lab TA",
                "CS220",
                "Computer Systems",
                "Semester B",
                "7",
                "Linux basics; lab troubleshooting; patient communication",
                "Linux|Labs",
                "Support systems labs and guide students through debugging practical exercises.",
                1
        ));
        changedJobs |= ensureJob(jobs, new JobPosting(
                "job-ai",
                "AI Studio TA",
                "AI310",
                "Applied AI Studio",
                "Semester B",
                "4",
                "Python; prompt evaluation; project mentoring",
                "AI|Python",
                "Support project studios, review prototypes and guide responsible AI evaluation.",
                1
        ));

        changedProfiles |= ensureProfile(profiles, new ApplicantProfile(
                "ta-new",
                "New Student",
                "20240003",
                "BSc Software Engineering",
                "Year 2",
                "Python, dashboards, communication",
                "Fri AM",
                "3.6",
                "",
                "",
                "",
                "",
                "2026-04-04T10:00:00"
        ));

        changedApplications |= ensureApplication(applications, new JobApplication(
                "app-amy-data",
                "job-data",
                "ta-amy",
                ApplicationStatus.SHORTLISTED,
                "2026-04-03T10:00:00",
                "Shortlisted for further review. Please wait for an interview invitation or next-step update.",
                ""
        ));
        changedApplications |= ensureApplication(applications, new JobApplication(
                "app-amy-research",
                "job-research",
                "ta-amy",
                ApplicationStatus.INTERVIEW_SCHEDULED,
                "2026-04-04T10:00:00",
                "Interview scheduled. Please prepare one tutoring example.",
                "2026-05-20T09:00"
        ));
        changedApplications |= ensureApplication(applications, new JobApplication(
                "app-amy-systems",
                "job-systems",
                "ta-amy",
                ApplicationStatus.SELECTED,
                "2026-04-05T10:00:00",
                "You have been selected for this role. Await onboarding details.",
                ""
        ));
        changedApplications |= ensureApplication(applications, new JobApplication(
                "app-amy-ai",
                "job-ai",
                "ta-amy",
                ApplicationStatus.REJECTED,
                "2026-04-06T10:00:00",
                "Thank you for applying. This application was not selected.",
                ""
        ));

        changedNotifications |= ensureNotification(notifications, new NotificationItem(
                "note-amy-shortlisted",
                "ta-amy",
                "Application shortlisted",
                "You have been shortlisted for Data Analytics TA. Please wait for an interview invitation or further review.",
                "2026-04-03T10:05:00"
        ));
        changedNotifications |= ensureNotification(notifications, new NotificationItem(
                "note-amy-interview",
                "ta-amy",
                "Interview scheduled",
                "Your interview for Research Methods TA is scheduled for 2026-05-20 09:00.",
                "2026-04-04T10:05:00"
        ));

        if (changedJobs) {
            jobRepository.saveAll(jobs);
        }
        if (changedProfiles) {
            profileRepository.saveAll(profiles);
        }
        if (changedApplications) {
            applicationRepository.saveAll(applications);
        }
        if (changedNotifications) {
            notificationRepository.saveAll(notifications);
        }
    }

    private boolean ensureJob(List<JobPosting> jobs, JobPosting job) {
        if (jobs.stream().anyMatch(existing -> existing.id().equals(job.id()))) {
            return false;
        }
        jobs.add(job);
        return true;
    }

    private boolean ensureProfile(List<ApplicantProfile> profiles, ApplicantProfile profile) {
        if (profiles.stream().anyMatch(existing -> existing.applicantId().equals(profile.applicantId()))) {
            return false;
        }
        profiles.add(profile);
        return true;
    }

    private boolean ensureApplication(List<JobApplication> applications, JobApplication application) {
        if (applications.stream().anyMatch(existing -> existing.id().equals(application.id()))) {
            return false;
        }
        applications.add(application);
        return true;
    }

    private boolean ensureNotification(List<NotificationItem> notifications, NotificationItem notification) {
        if (notifications.stream().anyMatch(existing -> existing.id().equals(notification.id()))) {
            return false;
        }
        notifications.add(notification);
        return true;
    }

    private void ensureSeedUsers(UserRepository userRepository) {
        List<UserAccount> users = new ArrayList<>(userRepository.findAll());
        boolean changed = false;
        changed |= ensureUser(users, new UserAccount("ta-new", Role.APPLICANT, "New Student", "newta@school.edu", "password123"));
        changed |= ensureUser(users, new UserAccount("ta-amy", Role.APPLICANT, "Amy Parker", "amy@school.edu", "password123"));
        changed |= ensureUser(users, new UserAccount("ta-bob", Role.APPLICANT, "Bob Chen", "bob@school.edu", "password123"));
        changed |= ensureUser(users, new UserAccount("mo-olivia", Role.ORGANISER, "Olivia Moore", "mo@school.edu", "password123"));
        changed |= ensureUser(users, new UserAccount("admin-cindy", Role.ADMIN, "Cindy Admin", "admin@school.edu", "password123"));
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
