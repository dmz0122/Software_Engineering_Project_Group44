package com.group44.tarecruit.data;

import com.group44.tarecruit.model.SavedJob;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class SavedJobRepository {
    private static final List<String> HEADER = List.of("id", "applicantId", "jobId", "savedAt");

    private final Path filePath;

    public SavedJobRepository(Path filePath) {
        this.filePath = filePath;
    }

    public List<SavedJob> findAll() {
        List<List<String>> rows = CsvUtils.read(filePath);
        if (rows.isEmpty()) {
            return List.of();
        }
        List<SavedJob> savedJobs = new ArrayList<>();
        for (int index = 1; index < rows.size(); index++) {
            List<String> row = rows.get(index);
            savedJobs.add(new SavedJob(
                    valueAt(row, 0),
                    valueAt(row, 1),
                    valueAt(row, 2),
                    valueAt(row, 3)
            ));
        }
        return savedJobs;
    }

    public List<SavedJob> findByApplicantId(String applicantId) {
        return findAll().stream()
                .filter(savedJob -> savedJob.applicantId().equals(applicantId))
                .toList();
    }

    public Optional<SavedJob> find(String applicantId, String jobId) {
        return findAll().stream()
                .filter(savedJob -> savedJob.applicantId().equals(applicantId))
                .filter(savedJob -> savedJob.jobId().equals(jobId))
                .findFirst();
    }

    public void upsert(SavedJob savedJob) {
        List<SavedJob> updated = new ArrayList<>(findAll());
        updated.removeIf(existing -> existing.applicantId().equals(savedJob.applicantId())
                && existing.jobId().equals(savedJob.jobId()));
        updated.add(savedJob);
        saveAll(updated);
    }

    public void delete(String applicantId, String jobId) {
        List<SavedJob> updated = new ArrayList<>(findAll());
        updated.removeIf(existing -> existing.applicantId().equals(applicantId)
                && existing.jobId().equals(jobId));
        saveAll(updated);
    }

    public void saveAll(List<SavedJob> savedJobs) {
        List<List<String>> rows = savedJobs.stream()
                .map(savedJob -> List.of(
                        savedJob.id(),
                        savedJob.applicantId(),
                        savedJob.jobId(),
                        savedJob.savedAt()
                ))
                .toList();
        CsvUtils.write(filePath, HEADER, rows);
    }

    private String valueAt(List<String> row, int index) {
        return index < row.size() ? row.get(index) : "";
    }
}
