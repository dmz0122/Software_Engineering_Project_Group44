package com.group44.tarecruit.data;

import com.group44.tarecruit.model.ApplicationStatus;
import com.group44.tarecruit.model.JobApplication;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ApplicationRepository {
    private static final List<String> HEADER = List.of(
            "id",
            "jobId",
            "applicantId",
            "status",
            "appliedAt",
            "note"
    );

    private final Path filePath;

    public ApplicationRepository(Path filePath) {
        this.filePath = filePath;
    }

    public List<JobApplication> findAll() {
        List<List<String>> rows = CsvUtils.read(filePath);
        if (rows.isEmpty()) {
            return List.of();
        }
        List<JobApplication> applications = new ArrayList<>();
        for (int index = 1; index < rows.size(); index++) {
            List<String> row = rows.get(index);
            applications.add(new JobApplication(
                    valueAt(row, 0),
                    valueAt(row, 1),
                    valueAt(row, 2),
                    ApplicationStatus.fromLabel(valueAt(row, 3)),
                    valueAt(row, 4),
                    valueAt(row, 5)
            ));
        }
        return applications;
    }

    public Optional<JobApplication> findById(String id) {
        return findAll().stream()
                .filter(application -> application.id().equals(id))
                .findFirst();
    }

    public void upsert(JobApplication application) {
        List<JobApplication> updated = new ArrayList<>(findAll());
        updated.removeIf(existing -> existing.id().equals(application.id()));
        updated.add(application);
        saveAll(updated);
    }

    public void saveAll(List<JobApplication> applications) {
        List<List<String>> rows = applications.stream()
                .map(application -> List.of(
                        application.id(),
                        application.jobId(),
                        application.applicantId(),
                        application.status().label(),
                        application.appliedAt(),
                        application.note()
                ))
                .toList();
        CsvUtils.write(filePath, HEADER, rows);
    }

    private String valueAt(List<String> row, int index) {
        return index < row.size() ? row.get(index) : "";
    }
}
