package com.bupt.ta.db.repository;

import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.value.JobQuery;

import java.util.List;
import java.util.UUID;

/**
 * Repository contract for vacancy listing, ownership, and accepted-slot counts.
 */
public interface JobRepository extends CrudRepository<Job> {
    List<Job> listOpen(JobQuery query);

    List<Job> listByPoster(UUID posterId);

    long countAccepted(UUID jobId);
}
