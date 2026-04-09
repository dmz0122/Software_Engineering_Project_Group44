package com.group44.tarecruit.service;

import com.group44.tarecruit.data.ApplicationRepository;
import com.group44.tarecruit.data.JobRepository;
import com.group44.tarecruit.data.ProfileRepository;
import com.group44.tarecruit.data.UserRepository;
import com.group44.tarecruit.model.ApplicantProfile;
import com.group44.tarecruit.model.ApplicationStatus;
import com.group44.tarecruit.model.JobApplication;
import com.group44.tarecruit.model.JobPosting;
import com.group44.tarecruit.model.UserAccount;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class ApplicationService {
    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final ProfileRepository profileRepository;
    private final UserRepository userRepository;
    private final NotificationService notificationService;

    public ApplicationService(
            ApplicationRepository applicationRepository,
            JobRepository jobRepository,
            ProfileRepository profileRepository,
            UserRepository userRepository,
            NotificationService notificationService
    ) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.profileRepository = profileRepository;
        this.userRepository = userRepository;
        this.notificationService = notificationService;
    }

    public List<JobApplication> findApplicationsForApplicant(String applicantId) {
        return applicationRepository.findAll().stream()
                .filter(application -> application.applicantId().equals(applicantId))
                .sorted(Comparator.comparing(JobApplication::appliedAt).reversed())
                .toList();
    }

    public List<ApplicantReviewItem> findApplicantsForJob(String jobId) {
        JobPosting job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found."));
        return applicationRepository.findAll().stream()
                .filter(application -> application.jobId().equals(jobId))
                .map(application -> buildReviewItem(job, application))
                .sorted(Comparator.comparingInt(ApplicantReviewItem::fitScore).reversed())
                .toList();
    }

    public JobApplication applyForJob(String jobId, String applicantId) {
        JobPosting job = jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found."));
        ApplicantProfile profile = profileRepository.findByApplicantId(applicantId)
                .orElseThrow(() -> new IllegalArgumentException("Please complete your profile before applying."));
        if (!profile.isComplete()) {
            throw new IllegalArgumentException("Please complete your profile before applying.");
        }

        boolean duplicate = applicationRepository.findAll().stream()
                .anyMatch(existing -> existing.jobId().equals(jobId) && existing.applicantId().equals(applicantId));
        if (duplicate) {
            throw new IllegalArgumentException("You have already applied for this role.");
        }

        JobApplication application = new JobApplication(
                UUID.randomUUID().toString(),
                jobId,
                applicantId,
                ApplicationStatus.APPLIED,
                LocalDateTime.now().toString(),
                "Application received and queued for organiser review."
        );
        applicationRepository.upsert(application);
        notificationService.notifyUser(
                applicantId,
                "Application submitted",
                "Your application for " + job.title() + " has been recorded successfully."
        );
        return application;
    }

    public void selectApplicant(String applicationId) {
        JobApplication application = applicationRepository.findById(applicationId)
                .orElseThrow(() -> new IllegalArgumentException("Application not found."));
        if (application.status() == ApplicationStatus.SELECTED) {
            throw new IllegalArgumentException("This applicant has already been selected.");
        }

        JobPosting job = jobRepository.findById(application.jobId()).orElseThrow();
        if (selectedCountForJob(job.id()) >= job.openings()) {
            throw new IllegalArgumentException("All available openings for this job have already been filled.");
        }

        JobApplication updated = new JobApplication(
                application.id(),
                application.jobId(),
                application.applicantId(),
                ApplicationStatus.SELECTED,
                application.appliedAt(),
                application.note()
        );
        applicationRepository.upsert(updated);

        notificationService.notifyUser(
                application.applicantId(),
                "Selection result",
                "You have been selected for " + job.title() + "."
        );
    }

    public List<JobPosting> jobsWithApplicants() {
        return jobRepository.findAll().stream()
                .sorted(Comparator.comparing(JobPosting::title))
                .toList();
    }

    public Optional<JobPosting> findJobForApplication(JobApplication application) {
        return jobRepository.findById(application.jobId());
    }

    public long selectedCountForJob(String jobId) {
        return applicationRepository.findAll().stream()
                .filter(application -> application.jobId().equals(jobId))
                .filter(application -> application.status() == ApplicationStatus.SELECTED)
                .count();
    }

    private ApplicantReviewItem buildReviewItem(JobPosting job, JobApplication application) {
        UserAccount applicant = userRepository.findAll().stream()
                .filter(user -> user.id().equals(application.applicantId()))
                .findFirst()
                .orElseThrow();
        ApplicantProfile profile = profileRepository.findByApplicantId(application.applicantId())
                .orElse(new ApplicantProfile(
                        application.applicantId(),
                        applicant.displayName(),
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                ));

        int fitScore = calculateFitScore(job.requiredSkills(), profile.skills());
        return new ApplicantReviewItem(application, applicant, profile, fitScore);
    }

    private int calculateFitScore(String requiredSkills, String applicantSkills) {
        if (requiredSkills.isBlank() || applicantSkills.isBlank()) {
            return 0;
        }
        List<String> requiredTokens = splitSkills(requiredSkills);
        List<String> applicantTokens = splitSkills(applicantSkills);
        long matched = requiredTokens.stream()
                .filter(token -> applicantTokens.stream().anyMatch(skill -> skill.contains(token) || token.contains(skill)))
                .count();
        return (int) Math.round((matched * 100.0) / requiredTokens.size());
    }

    private List<String> splitSkills(String value) {
        return List.of(value.toLowerCase()
                        .replace(';', ',')
                        .split(","))
                .stream()
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .toList();
    }

    public record ApplicantReviewItem(
            JobApplication application,
            UserAccount applicant,
            ApplicantProfile profile,
            int fitScore
    ) {
    }
}
