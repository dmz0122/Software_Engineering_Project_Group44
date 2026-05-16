package com.group44.tarecruit.service;

import com.group44.tarecruit.data.JobRepository;
import com.group44.tarecruit.data.SavedJobRepository;
import com.group44.tarecruit.model.JobPosting;
import com.group44.tarecruit.model.SavedJob;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.UUID;

public class SavedJobService {
    private final SavedJobRepository savedJobRepository;
    private final JobRepository jobRepository;

    public SavedJobService(SavedJobRepository savedJobRepository, JobRepository jobRepository) {
        this.savedJobRepository = savedJobRepository;
        this.jobRepository = jobRepository;
    }

    public List<SavedJob> findSavedJobs(String applicantId) {
        return savedJobRepository.findByApplicantId(applicantId).stream()
                .sorted(Comparator.comparing(SavedJob::savedAt).reversed())
                .toList();
    }

    public List<JobPosting> findSavedJobPostings(String applicantId) {
        return findSavedJobs(applicantId).stream()
                .map(savedJob -> jobRepository.findById(savedJob.jobId()).orElse(null))
                .filter(job -> job != null)
                .toList();
    }

    public boolean isSaved(String applicantId, String jobId) {
        return savedJobRepository.find(applicantId, jobId).isPresent();
    }

    public SavedJob saveJob(String applicantId, String jobId) {
        jobRepository.findById(jobId)
                .orElseThrow(() -> new IllegalArgumentException("Job not found."));
        return savedJobRepository.find(applicantId, jobId)
                .orElseGet(() -> {
                    SavedJob savedJob = new SavedJob(
                            UUID.randomUUID().toString(),
                            applicantId,
                            jobId,
                            LocalDateTime.now().toString()
                    );
                    savedJobRepository.upsert(savedJob);
                    return savedJob;
                });
    }

    public void removeSavedJob(String applicantId, String jobId) {
        savedJobRepository.delete(applicantId, jobId);
    }
}
