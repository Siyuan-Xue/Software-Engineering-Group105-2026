package com.bupt.ta.db.repository;

import com.bupt.ta.domain.entity.Application;
import com.bupt.ta.domain.enums.ApplicationStatus;

import java.util.List;
import java.util.UUID;

public interface ApplicationRepository extends CrudRepository<Application> {
    List<Application> listByJobId(UUID jobId);

    List<Application> listByResumeId(UUID resumeId);

    List<Application> listByStatuses(UUID jobId, List<ApplicationStatus> statuses);

    boolean existsByTaAndJob(UUID taUserId, UUID jobId);
}
