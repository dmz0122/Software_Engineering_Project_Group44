package com.group44.tarecruit.service;

import com.group44.tarecruit.data.ApplicationRepository;
import com.group44.tarecruit.data.JobRepository;
import com.group44.tarecruit.data.UserRepository;
import com.group44.tarecruit.model.ApplicationStatus;
import com.group44.tarecruit.model.JobApplication;
import com.group44.tarecruit.model.JobPosting;
import com.group44.tarecruit.model.UserAccount;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class WorkloadService {
    public static final String CURRENT_SEMESTER_FILTER = "Current semester";
    public static final String ALL_SEMESTERS_FILTER = "All semesters";

    private final ApplicationRepository applicationRepository;
    private final JobRepository jobRepository;
    private final UserRepository userRepository;

    public WorkloadService(
            ApplicationRepository applicationRepository,
            JobRepository jobRepository,
            UserRepository userRepository
    ) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
    }

    public List<String> availableSemesters() {
        return jobRepository.findAll().stream()
                .map(JobPosting::semester)
                .filter(semester -> !semester.isBlank())
                .distinct()
                .sorted()
                .toList();
    }

    public List<WorkloadSummary> getWorkload(String filter) {
        String resolvedSemester = filter == null || filter.isBlank() || ALL_SEMESTERS_FILTER.equalsIgnoreCase(filter)
                ? ""
                : filter.trim();
        Map<String, JobPosting> jobsById = jobRepository.findAll().stream()
                .collect(LinkedHashMap::new, (map, job) -> map.put(job.id(), job), LinkedHashMap::putAll);
        Map<String, UserAccount> usersById = userRepository.findAll().stream()
                .collect(LinkedHashMap::new, (map, user) -> map.put(user.id(), user), LinkedHashMap::putAll);

        Map<String, List<WorkloadAssignment>> assignmentsByApplicant = new LinkedHashMap<>();
        for (JobApplication application : applicationRepository.findAll()) {
            if (application.status() != ApplicationStatus.SELECTED) {
                continue;
            }
            JobPosting job = jobsById.get(application.jobId());
            if (job == null || !matchesSemester(job.semester(), resolvedSemester)) {
                continue;
            }
            assignmentsByApplicant
                    .computeIfAbsent(application.applicantId(), ignored -> new ArrayList<>())
                    .add(new WorkloadAssignment(
                            application.id(),
                            job.id(),
                            job.title(),
                            job.moduleCode(),
                            job.moduleName(),
                            job.semester(),
                            parseHours(job.hoursPerWeek())
                    ));
        }

        return assignmentsByApplicant.entrySet().stream()
                .map(entry -> toSummary(entry.getKey(), entry.getValue(), usersById, resolvedSemester))
                .sorted(Comparator.comparingInt(WorkloadSummary::totalHoursPerWeek).reversed()
                        .thenComparing(WorkloadSummary::displayName))
                .toList();
    }

    private WorkloadSummary toSummary(
            String applicantId,
            List<WorkloadAssignment> assignments,
            Map<String, UserAccount> usersById,
            String resolvedSemester
    ) {
        UserAccount user = usersById.get(applicantId);
        int totalHours = assignments.stream()
                .mapToInt(WorkloadAssignment::hoursPerWeek)
                .sum();
        return new WorkloadSummary(
                applicantId,
                user == null ? applicantId : user.displayName(),
                resolvedSemester.isBlank() ? ALL_SEMESTERS_FILTER : resolvedSemester,
                totalHours,
                assignments.size(),
                assignments.stream()
                        .sorted(Comparator.comparing(WorkloadAssignment::jobTitle))
                        .toList()
        );
    }

    private boolean matchesSemester(String semester, String resolvedSemester) {
        return resolvedSemester.isBlank() || semester.equalsIgnoreCase(resolvedSemester);
    }

    private int parseHours(String value) {
        try {
            return Integer.parseInt(value.trim());
        } catch (NumberFormatException exception) {
            return 0;
        }
    }

    public record WorkloadSummary(
            String applicantId,
            String displayName,
            String semester,
            int totalHoursPerWeek,
            int selectedRoles,
            List<WorkloadAssignment> assignments
    ) {
        public String summaryLine() {
            return displayName + ": " + totalHoursPerWeek + " hrs/week";
        }
    }

    public record WorkloadAssignment(
            String applicationId,
            String jobId,
            String jobTitle,
            String moduleCode,
            String moduleName,
            String semester,
            int hoursPerWeek
    ) {
    }
}
