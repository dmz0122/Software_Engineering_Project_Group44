package com.group44.tarecruit.service;

import com.group44.tarecruit.data.ProfileRepository;
import com.group44.tarecruit.model.ApplicantProfile;

import java.time.LocalDateTime;
import java.util.Optional;

public class ProfileService {
    private final ProfileRepository profileRepository;

    public ProfileService(ProfileRepository profileRepository) {
        this.profileRepository = profileRepository;
    }

    public Optional<ApplicantProfile> findProfile(String applicantId) {
        return profileRepository.findByApplicantId(applicantId);
    }

    public ApplicantProfile saveProfile(ApplicantProfile draftProfile) {
        validate(draftProfile);
        ApplicantProfile savedProfile = new ApplicantProfile(
                draftProfile.applicantId(),
                draftProfile.fullName().trim(),
                draftProfile.studentId().trim(),
                draftProfile.programme().trim(),
                draftProfile.year().trim(),
                draftProfile.skills().trim(),
                draftProfile.availability().trim(),
                draftProfile.gpa().trim(),
                draftProfile.cvOriginalFileName().trim(),
                draftProfile.cvStoredPath().trim(),
                draftProfile.avatarOriginalFileName().trim(),
                draftProfile.avatarStoredPath().trim(),
                LocalDateTime.now().toString()
        );
        profileRepository.upsert(savedProfile);
        return savedProfile;
    }

    public ApplicantProfile saveCvReference(String applicantId, String originalFileName, String storedPath) {
        ApplicantProfile existingProfile = profileRepository.findByApplicantId(applicantId)
                .orElse(new ApplicantProfile(
                        applicantId,
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                ));

        ApplicantProfile savedProfile = new ApplicantProfile(
                applicantId,
                existingProfile.fullName(),
                existingProfile.studentId(),
                existingProfile.programme(),
                existingProfile.year(),
                existingProfile.skills(),
                existingProfile.availability(),
                existingProfile.gpa(),
                originalFileName == null ? "" : originalFileName.trim(),
                storedPath == null ? "" : storedPath.trim(),
                existingProfile.avatarOriginalFileName(),
                existingProfile.avatarStoredPath(),
                LocalDateTime.now().toString()
        );
        profileRepository.upsert(savedProfile);
        return savedProfile;
    }

    public ApplicantProfile saveAvatarReference(String applicantId, String originalFileName, String storedPath) {
        ApplicantProfile existingProfile = profileRepository.findByApplicantId(applicantId)
                .orElse(new ApplicantProfile(
                        applicantId,
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        "",
                        ""
                ));

        ApplicantProfile savedProfile = new ApplicantProfile(
                applicantId,
                existingProfile.fullName(),
                existingProfile.studentId(),
                existingProfile.programme(),
                existingProfile.year(),
                existingProfile.skills(),
                existingProfile.availability(),
                existingProfile.gpa(),
                existingProfile.cvOriginalFileName(),
                existingProfile.cvStoredPath(),
                originalFileName == null ? "" : originalFileName.trim(),
                storedPath == null ? "" : storedPath.trim(),
                LocalDateTime.now().toString()
        );
        profileRepository.upsert(savedProfile);
        return savedProfile;
    }

    private void validate(ApplicantProfile profile) {
        if (profile.fullName().isBlank()
                || profile.studentId().isBlank()
                || profile.programme().isBlank()
                || profile.year().isBlank()
                || profile.skills().isBlank()
                || profile.availability().isBlank()
                || profile.gpa().isBlank()) {
            throw new IllegalArgumentException("Please complete every required profile field before saving.");
        }
        if (!profile.studentId().trim().matches("\\d{6,12}")) {
            throw new IllegalArgumentException("Student ID must contain 6 to 12 digits only.");
        }
        if (!profile.year().trim().matches("(?i)(year\\s*)?[1-6]")) {
            throw new IllegalArgumentException("Year must be between 1 and 6, for example Year 2.");
        }
        try {
            double gpa = Double.parseDouble(profile.gpa().trim());
            if (gpa < 0.0 || gpa > 4.3) {
                throw new IllegalArgumentException("GPA must be between 0.0 and 4.3.");
            }
        } catch (NumberFormatException exception) {
            throw new IllegalArgumentException("GPA must be a numeric value, for example 3.7.");
        }
    }
}
