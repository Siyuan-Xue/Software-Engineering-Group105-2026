package com.bupt.ta.service;

import com.bupt.ta.model.Job;
import com.bupt.ta.repository.JobRepository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public class JobService {
    private final JobRepository jobRepository;

    public JobService(JobRepository jobRepository) {
        this.jobRepository = jobRepository;
    }

    public List<Job> listOpen(Instant now) {
        return jobRepository.listOpen(now);
    }

    public List<Job> listByPoster(UUID posterId) {
        return jobRepository.listByPoster(posterId);
    }

    public Job save(Job job) {
        return jobRepository.save(job);
    }
}
