package com.group44.tarecruit.data;

import com.group44.tarecruit.model.ApplicantProfile;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class ProfileRepository {
    private static final List<String> HEADER = List.of(
            "applicantId",
            "fullName",
            "studentId",
            "programme",
            "year",
            "skills",
            "availability",
            "gpa",
            "cvOriginalFileName",
            "cvStoredPath",
            "updatedAt"
    );

    private final Path filePath;

    public ProfileRepository(Path filePath) {
        this.filePath = filePath;
    }

    public List<ApplicantProfile> findAll() {
        List<List<String>> rows = CsvUtils.read(filePath);
        if (rows.isEmpty()) {
            return List.of();
        }
        List<ApplicantProfile> profiles = new ArrayList<>();
        for (int index = 1; index < rows.size(); index++) {
            List<String> row = rows.get(index);
            profiles.add(new ApplicantProfile(
                    valueAt(row, 0),
                    valueAt(row, 1),
                    valueAt(row, 2),
                    valueAt(row, 3),
                    valueAt(row, 4),
                    valueAt(row, 5),
                    valueAt(row, 6),
                    valueAt(row, 7),
                    valueAt(row, 8),
                    valueAt(row, 9),
                    valueAt(row, 10)
            ));
        }
        return profiles;
    }

    public Optional<ApplicantProfile> findByApplicantId(String applicantId) {
        return findAll().stream()
                .filter(profile -> profile.applicantId().equals(applicantId))
                .findFirst();
    }

    public void upsert(ApplicantProfile profile) {
        List<ApplicantProfile> updated = new ArrayList<>(findAll());
        updated.removeIf(existing -> existing.applicantId().equals(profile.applicantId()));
        updated.add(profile);
        saveAll(updated);
    }

    public void saveAll(List<ApplicantProfile> profiles) {
        List<List<String>> rows = profiles.stream()
                .map(profile -> List.of(
                        profile.applicantId(),
                        profile.fullName(),
                        profile.studentId(),
                        profile.programme(),
                        profile.year(),
                        profile.skills(),
                        profile.availability(),
                        profile.gpa(),
                        profile.cvOriginalFileName(),
                        profile.cvStoredPath(),
                        profile.updatedAt()
                ))
                .toList();
        CsvUtils.write(filePath, HEADER, rows);
    }

    private String valueAt(List<String> row, int index) {
        return index < row.size() ? row.get(index) : "";
    }
}
