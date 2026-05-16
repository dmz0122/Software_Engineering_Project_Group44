package com.group44.tarecruit.model;

public record SavedJob(
        String id,
        String applicantId,
        String jobId,
        String savedAt
) {
}
