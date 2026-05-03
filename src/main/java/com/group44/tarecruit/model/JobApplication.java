package com.group44.tarecruit.model;

public record JobApplication(
        String id,
        String jobId,
        String applicantId,
        ApplicationStatus status,
        String appliedAt,
        String note,
        String interviewAt
) {
    public JobApplication(String id, String jobId, String applicantId, ApplicationStatus status, String appliedAt, String note) {
        this(id, jobId, applicantId, status, appliedAt, note, "");
    }

    public boolean hasInterviewScheduled() {
        return interviewAt != null && !interviewAt.isBlank();
    }
}
