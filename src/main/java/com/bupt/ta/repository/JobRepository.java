package com.bupt.ta.repository;

import com.bupt.ta.model.Job;

import java.time.Instant;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface JobRepository {
    Optional<Job> findById(UUID id);

    List<Job> listOpen(Instant now);

    List<Job> listByPoster(UUID posterId);

    Job save(Job job);
}
