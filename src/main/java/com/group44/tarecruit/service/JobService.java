package com.group44.tarecruit.service;

import com.group44.tarecruit.data.JobRepository;
import com.group44.tarecruit.model.JobPosting;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public class JobService {
    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public List<JobPosting> getAllJobs() {
        return jobRepository.findAll().stream()
                .sorted(Comparator.comparing(JobPosting::title))
                .toList();
    }

    public List<JobPosting> filterJobs(String searchQuery, String tagFilter, String semesterFilter) {
        String normalizedQuery = normalize(searchQuery);
        String normalizedTag = normalize(tagFilter);
        String normalizedSemester = normalize(semesterFilter);

        return getAllJobs().stream()
                .filter(job -> matchesQuery(job, normalizedQuery))
                .filter(job -> matchesTag(job, normalizedTag))
                .filter(job -> matchesSemester(job, normalizedSemester))
                .toList();
    }

    public List<String> availableTags() {
        return getAllJobs().stream()
                .flatMap(job -> List.of(job.tags().split("\\|")).stream())
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .toList();
    }

    public List<String> availableSemesters() {
        return getAllJobs().stream()
                .map(JobPosting::semester)
                .filter(semester -> !semester.isBlank())
                .distinct()
                .sorted(String::compareToIgnoreCase)
                .toList();
    }

    public Optional<JobPosting> findById(String jobId) {
        return jobRepository.findById(jobId);
    }

    public JobPosting createJob(JobPosting draftJob) {
        int openings = validatePositiveNumber(draftJob.openings(), "Openings");
        String hoursPerWeek = String.valueOf(validatePositiveNumber(draftJob.hoursPerWeek(), "Hours per week"));
        String requiredSkills = normalizeSkills(draftJob.requiredSkills());

        JobPosting savedJob = new JobPosting(
                draftJob.id() == null || draftJob.id().isBlank() ? UUID.randomUUID().toString() : draftJob.id().trim(),
                requireValue(draftJob.title(), "Role"),
                requireValue(draftJob.moduleCode(), "Module code").toUpperCase(),
                requireValue(draftJob.moduleName(), "Module or activity"),
                requireValue(draftJob.semester(), "Semester"),
                hoursPerWeek,
                requiredSkills,
                normalizeTags(draftJob.tags(), requiredSkills),
                requireValue(draftJob.description(), "Description"),
                openings
        );

        List<JobPosting> jobs = new ArrayList<>(jobRepository.findAll());
        jobs.add(savedJob);
        jobRepository.saveAll(jobs);
        return savedJob;
    }

    private String requireValue(String value, String fieldName) {
        if (value == null || value.isBlank()) {
            throw new IllegalArgumentException(fieldName + " is required.");
        }
        return value.trim();
    }

    private int validatePositiveNumber(int value, String fieldName) {
        if (value <= 0) {
            throw new IllegalArgumentException(fieldName + " must be greater than 0.");
        }
        return value;
    }

    private int validatePositiveNumber(String value, String fieldName) {
        String trimmed = requireValue(value, fieldName);
        try {
            return validatePositiveNumber(Integer.parseInt(trimmed), fieldName);
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException(fieldName + " must be a whole number.");
        }
    }

    private String normalizeSkills(String requiredSkills) {
        List<String> tokens = List.of(requireValue(requiredSkills, "Required skills")
                        .replace('|', ',')
                        .replace(';', ',')
                        .split(","))
                .stream()
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .distinct()
                .toList();
        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Required skills are required.");
        }
        return String.join("; ", tokens);
    }

    private String normalizeTags(String rawTags, String requiredSkills) {
        if (rawTags != null && !rawTags.isBlank()) {
            return List.of(rawTags.replace(',', '|').split("\\|"))
                    .stream()
                    .map(String::trim)
                    .filter(token -> !token.isBlank())
                    .distinct()
                    .reduce((left, right) -> left + "|" + right)
                    .orElse("");
        }

        List<String> generatedTags = List.of(requiredSkills.split(";"))
                .stream()
                .map(String::trim)
                .filter(token -> !token.isBlank())
                .limit(2)
                .toList();
        return String.join("|", generatedTags);
    }

    private boolean matchesQuery(JobPosting job, String normalizedQuery) {
        if (normalizedQuery.isBlank()) {
            return true;
        }
        String searchable = String.join(" ",
                job.title(),
                job.moduleCode(),
                job.moduleName(),
                job.semester(),
                job.requiredSkills(),
                job.tags(),
                job.description()
        ).toLowerCase();
        return searchable.contains(normalizedQuery);
    }

    private boolean matchesTag(JobPosting job, String normalizedTag) {
        if (normalizedTag.isBlank() || "all tags".equals(normalizedTag)) {
            return true;
        }
        return List.of(job.tags().split("\\|")).stream()
                .map(String::trim)
                .filter(tag -> !tag.isBlank())
                .map(String::toLowerCase)
                .anyMatch(normalizedTag::equals);
    }

    private boolean matchesSemester(JobPosting job, String normalizedSemester) {
        return normalizedSemester.isBlank()
                || "all semesters".equals(normalizedSemester)
                || job.semester().equalsIgnoreCase(normalizedSemester);
    }

    private String normalize(String value) {
        return value == null ? "" : value.trim().toLowerCase();
    }
}
