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
    }
}
