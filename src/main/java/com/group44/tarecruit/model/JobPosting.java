package com.group44.tarecruit.model;

public record JobPosting(
        String id,
        String title,
        String moduleCode,
        String moduleName,
        String semester,
        String hoursPerWeek,
        String requiredSkills,
        String tags,
        String description,
        int openings
) {
    public String summaryLine() {
        return moduleCode + " " + moduleName + " • " + hoursPerWeek + " hrs/week";
    }
}
