package com.group44.tarecruit.model;

public enum ApplicationStatus {
    APPLIED("Applied"),
    UNDER_REVIEW("Under Review"),
    SHORTLISTED("Shortlisted"),
    INTERVIEW_SCHEDULED("Interview"),
    SELECTED("Selected"),
    REJECTED("Rejected"),
    WITHDRAWN("Withdrawn");

    private final String label;

    ApplicationStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public boolean isTerminal() {
        return this == SELECTED || this == REJECTED || this == WITHDRAWN;
    }

    public static ApplicationStatus fromLabel(String value) {
        if (value == null || value.isBlank()) {
            return UNDER_REVIEW;
        }
        if ("Interview Scheduled".equalsIgnoreCase(value.trim())) {
            return INTERVIEW_SCHEDULED;
        }
        for (ApplicationStatus status : values()) {
            if (status.label.equalsIgnoreCase(value) || status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        return UNDER_REVIEW;
    }
}
