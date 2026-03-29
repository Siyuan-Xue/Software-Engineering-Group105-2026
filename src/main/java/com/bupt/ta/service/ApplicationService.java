package com.bupt.ta.service;

import com.bupt.ta.model.Application;
import com.bupt.ta.repository.ApplicationRepository;

import java.util.List;
import java.util.UUID;

public class ApplicationService {
    private final ApplicationRepository applicationRepository;

    public ApplicationService(ApplicationRepository applicationRepository) {
        this.applicationRepository = applicationRepository;
    }

    public List<Application> listByJobId(UUID jobId) {
        return applicationRepository.listByJobId(jobId);
    }

    public List<Application> listByResumeId(UUID resumeId) {
        return applicationRepository.listByResumeId(resumeId);
    }

    public Application save(Application application) {
        return applicationRepository.save(application);
    }
}
