package com.bupt.ta.db.facade;

import com.bupt.ta.db.repository.ApplicationRepository;
import com.bupt.ta.db.repository.AuditLogRepository;
import com.bupt.ta.db.repository.JobRepository;
import com.bupt.ta.db.repository.JobRequirementRepository;
import com.bupt.ta.db.repository.MatchScoreRepository;
import com.bupt.ta.db.repository.NotificationRepository;
import com.bupt.ta.db.repository.ResumeRepository;
import com.bupt.ta.db.repository.ResumeSkillRepository;
import com.bupt.ta.db.repository.SkillRepository;
import com.bupt.ta.db.repository.UserRepository;
import com.bupt.ta.db.repository.WorkloadRecordRepository;

/**
 * Facade that exposes all repositories used by services and servlets.
 */
public interface TaDatabase {
    UserRepository users();

    ResumeRepository resumes();

    JobRepository jobs();

    ApplicationRepository applications();

    SkillRepository skills();

    ResumeSkillRepository resumeSkills();

    JobRequirementRepository jobRequirements();

    WorkloadRecordRepository workloadRecords();

    MatchScoreRepository matchScores();

    NotificationRepository notifications();

    AuditLogRepository auditLogs();

    void executeAtomically(Runnable action);
}
