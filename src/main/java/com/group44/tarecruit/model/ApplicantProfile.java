package com.group44.tarecruit.model;

public record ApplicantProfile(
        String applicantId,
        String fullName,
        String studentId,
        String programme,
        String year,
        String skills,
        String availability,
        String gpa,
        String cvOriginalFileName,
        String cvStoredPath,
        String updatedAt
) {
    public boolean isComplete() {
        return !fullName.isBlank()
                && !studentId.isBlank()
                && !programme.isBlank()
                && !year.isBlank()
                && !skills.isBlank()
                && !availability.isBlank()
                && !gpa.isBlank();
    }
}
