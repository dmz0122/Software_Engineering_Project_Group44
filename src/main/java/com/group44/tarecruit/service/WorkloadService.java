package com.group44.tarecruit.service;

import com.group44.tarecruit.data.ApplicationRepository;
import com.group44.tarecruit.data.JobRepository;
import com.group44.tarecruit.data.UserRepository;
import com.group44.tarecruit.model.ApplicationStatus;
import com.group44.tarecruit.model.JobApplication;
import com.group44.tarecruit.model.JobPosting;
import com.group44.tarecruit.model.UserAccount;

import java.time.Clock;
import java.time.LocalDate;
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
    private final Clock clock;

    public WorkloadService(
            ApplicationRepository applicationRepository,
            JobRepository jobRepository,
            UserRepository userRepository
    ) {
        this(applicationRepository, jobRepository, userRepository, Clock.systemDefaultZone());
    }

    WorkloadService(
            ApplicationRepository applicationRepository,
            JobRepository jobRepository,
            UserRepository userRepository,
            Clock clock
    ) {
        this.applicationRepository = applicationRepository;
        this.jobRepository = jobRepository;
        this.userRepository = userRepository;
        this.clock = clock;
    }

    public String resolveSemester(String filter) {
        if (filter == null || filter.isBlank() || CURRENT_SEMESTER_FILTER.equalsIgnoreCase(filter)) {
            return inferCurrentSemester(availableSemesters());
        }
        if (ALL_SEMESTERS_FILTER.equalsIgnoreCase(filter)) {
            return "";
        }
        return filter.trim();
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
        String resolvedSemester = resolveSemester(filter);
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

    private String inferCurrentSemester(List<String> semesters) {
        if (semesters.isEmpty()) {
            return "";
        }

        int month = LocalDate.now(clock).getMonthValue();
        String preferredToken = month <= 6 ? "a" : "b";
        for (String semester : semesters) {
            if (matchesHalfYearSemester(semester, preferredToken)) {
                return semester;
            }
        }

        String seasonalToken = month <= 6 ? "spring" : "autumn";
        for (String semester : semesters) {
            String normalized = normalizeSemester(semester);
            if (normalized.contains(seasonalToken)
                    || (seasonalToken.equals("autumn") && normalized.contains("fall"))) {
                return semester;
            }
        }

        return semesters.getFirst();
    }

    private boolean matchesHalfYearSemester(String semester, String preferredToken) {
        String normalized = normalizeSemester(semester);
        return normalized.equals("semester " + preferredToken)
                || normalized.equals("semester " + ("a".equals(preferredToken) ? "1" : "2"))
                || normalized.equals("term " + ("a".equals(preferredToken) ? "1" : "2"));
    }

    private String normalizeSemester(String semester) {
        return semester == null ? "" : semester.trim().toLowerCase().replaceAll("\\s+", " ");
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
