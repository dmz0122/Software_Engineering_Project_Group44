package com.group44.tarecruit.service;

import com.group44.tarecruit.data.JobRepository;
import com.group44.tarecruit.model.JobPosting;

import java.util.Comparator;
import java.util.List;
import java.util.Optional;

public class JobService {
    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public List<JobPosting> getAllJobs() {
        return jobRepository.findAll().stream()
                .sorted(Comparator.comparing(JobPosting::title))
                .toList();
    }

    public Optional<JobPosting> findById(String jobId) {
        return jobRepository.findById(jobId);
    }
}
