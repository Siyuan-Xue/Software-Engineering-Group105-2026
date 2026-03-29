package com.bupt.ta.persistence;

import com.bupt.ta.config.DatabaseConfig;
import com.bupt.ta.persistence.json.JsonApplicationRepository;
import com.bupt.ta.persistence.json.JsonJobRepository;
import com.bupt.ta.persistence.json.JsonResumeRepository;
import com.bupt.ta.persistence.json.JsonUserRepository;
import com.bupt.ta.repository.ApplicationRepository;
import com.bupt.ta.repository.JobRepository;
import com.bupt.ta.repository.ResumeRepository;
import com.bupt.ta.repository.UserRepository;

public final class TaDatabase {
    private final UserRepository users;
    private final ResumeRepository resumes;
    private final JobRepository jobs;
    private final ApplicationRepository applications;

    private TaDatabase(UserRepository users,
                       ResumeRepository resumes,
                       JobRepository jobs,
                       ApplicationRepository applications) {
        this.users = users;
        this.resumes = resumes;
        this.jobs = jobs;
        this.applications = applications;
    }

    public static TaDatabase open(DatabaseConfig config) {
        return new TaDatabase(
                new JsonUserRepository(config),
                new JsonResumeRepository(config),
                new JsonJobRepository(config),
                new JsonApplicationRepository(config)
        );
    }

    public UserRepository users() {
        return users;
    }

    public ResumeRepository resumes() {
        return resumes;
    }

    public JobRepository jobs() {
        return jobs;
    }

    public ApplicationRepository applications() {
        return applications;
    }
}
