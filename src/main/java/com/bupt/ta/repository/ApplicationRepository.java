package com.bupt.ta.repository;

import com.bupt.ta.model.Application;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface ApplicationRepository {
    Optional<Application> findById(UUID id);

    List<Application> listByJobId(UUID jobId);

    List<Application> listByResumeId(UUID resumeId);

    Application save(Application application);
}
