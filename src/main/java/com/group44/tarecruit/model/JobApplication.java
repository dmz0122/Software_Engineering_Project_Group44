package com.group44.tarecruit.model;

public record JobApplication(
        String id,
        String jobId,
        String applicantId,
        ApplicationStatus status,
        String appliedAt,
        String note
) {
}
