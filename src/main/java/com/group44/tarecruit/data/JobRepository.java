package com.group44.tarecruit.data;

import com.group44.tarecruit.model.JobPosting;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class JobRepository {
    private static final List<String> HEADER = List.of(
            "id",
            "title",
            "moduleCode",
            "moduleName",
            "semester",
            "hoursPerWeek",
            "requiredSkills",
            "tags",
            "description",
            "openings"
    );

    private final Path filePath;

    public JobRepository(Path filePath) {
        this.filePath = filePath;
    }

    public List<JobPosting> findAll() {
        List<List<String>> rows = CsvUtils.read(filePath);
        if (rows.isEmpty()) {
            return List.of();
        }
        List<JobPosting> jobs = new ArrayList<>();
        for (int index = 1; index < rows.size(); index++) {
            List<String> row = rows.get(index);
            jobs.add(new JobPosting(
                    valueAt(row, 0),
                    valueAt(row, 1),
                    valueAt(row, 2),
                    valueAt(row, 3),
                    valueAt(row, 4),
                    valueAt(row, 5),
                    valueAt(row, 6),
                    valueAt(row, 7),
                    valueAt(row, 8),
                    Integer.parseInt(valueAt(row, 9))
            ));
        }
        return jobs;
    }

    public Optional<JobPosting> findById(String id) {
        return findAll().stream()
                .filter(job -> job.id().equals(id))
                .findFirst();
    }

    public void saveAll(List<JobPosting> jobs) {
        List<List<String>> rows = jobs.stream()
                .map(job -> List.of(
                        job.id(),
                        job.title(),
                        job.moduleCode(),
                        job.moduleName(),
                        job.semester(),
                        job.hoursPerWeek(),
                        job.requiredSkills(),
                        job.tags(),
                        job.description(),
                        String.valueOf(job.openings())
                ))
                .toList();
        CsvUtils.write(filePath, HEADER, rows);
    }

    private String valueAt(List<String> row, int index) {
        return index < row.size() ? row.get(index) : "";
    }
}
