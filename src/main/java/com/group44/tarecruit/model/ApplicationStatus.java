package com.group44.tarecruit.model;

public enum ApplicationStatus {
    APPLIED("Applied"),
    UNDER_REVIEW("Under Review"),
    SELECTED("Selected");

    private final String label;

    ApplicationStatus(String label) {
        this.label = label;
    }

    public String label() {
        return label;
    }

    public static ApplicationStatus fromLabel(String value) {
        for (ApplicationStatus status : values()) {
            if (status.label.equalsIgnoreCase(value) || status.name().equalsIgnoreCase(value)) {
                return status;
            }
        }
        return UNDER_REVIEW;
    }
}
