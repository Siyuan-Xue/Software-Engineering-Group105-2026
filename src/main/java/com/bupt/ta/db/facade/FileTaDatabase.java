package com.bupt.ta.db.facade;

import com.bupt.ta.db.core.DatabaseException;
import com.bupt.ta.db.core.JsonStoreConfig;
import com.bupt.ta.domain.entity.Application;
import com.bupt.ta.domain.entity.AuditLog;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.JobRequirement;
import com.bupt.ta.domain.entity.MatchScore;
import com.bupt.ta.domain.entity.Notification;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.entity.ResumeSkill;
import com.bupt.ta.domain.entity.Skill;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.entity.WorkloadRecord;
import com.bupt.ta.db.repository.ApplicationRepository;
import com.bupt.ta.db.repository.AuditLogRepository;
import com.bupt.ta.db.repository.JobRepository;
import com.bupt.ta.db.repository.JobRequirementRepository;
import com.bupt.ta.db.repository.JsonApplicationRepository;
import com.bupt.ta.db.repository.JsonAuditLogRepository;
import com.bupt.ta.db.repository.JsonJobRepository;
import com.bupt.ta.db.repository.JsonJobRequirementRepository;
import com.bupt.ta.db.repository.JsonMatchScoreRepository;
import com.bupt.ta.db.repository.JsonNotificationRepository;
import com.bupt.ta.db.repository.JsonResumeRepository;
import com.bupt.ta.db.repository.JsonResumeSkillRepository;
import com.bupt.ta.db.repository.JsonSkillRepository;
import com.bupt.ta.db.repository.JsonUserRepository;
import com.bupt.ta.db.repository.JsonWorkloadRecordRepository;
import com.bupt.ta.db.repository.MatchScoreRepository;
import com.bupt.ta.db.repository.NotificationRepository;
import com.bupt.ta.db.repository.ResumeRepository;
import com.bupt.ta.db.repository.ResumeSkillRepository;
import com.bupt.ta.db.repository.SkillRepository;
import com.bupt.ta.db.repository.UserRepository;
import com.bupt.ta.db.repository.WorkloadRecordRepository;
import com.bupt.ta.db.store.AtomicJsonFileWriter;
import com.bupt.ta.db.store.JsonTableStore;
import com.bupt.ta.db.store.TableDescriptor;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Set;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

/**
 * Production {@link TaDatabase} implementation backed by one JSON file per table.
 */
public class FileTaDatabase implements TaDatabase {
    private static final int VERSION = 1;
    private static final List<String> TABLE_FILES = List.of(
            "users.json",
            "resumes.json",
            "jobs.json",
            "applications.json",
            "skills.json",
            "resume_skills.json",
            "job_requirements.json",
            "workload_records.json",
            "match_scores.json",
            "notifications.json",
            "audit_logs.json"
    );

    private final UserRepository userRepository;
    private final ResumeRepository resumeRepository;
    private final JobRepository jobRepository;
    private final ApplicationRepository applicationRepository;
    private final SkillRepository skillRepository;
    private final ResumeSkillRepository resumeSkillRepository;
    private final JobRequirementRepository jobRequirementRepository;
    private final WorkloadRecordRepository workloadRecordRepository;
    private final MatchScoreRepository matchScoreRepository;
    private final NotificationRepository notificationRepository;
    private final AuditLogRepository auditLogRepository;
    private final Lock transactionLock = new ReentrantLock();

    private FileTaDatabase(JsonStoreConfig config) {
        validateOrPrepareDataDir(config.getDataDir());
        AtomicJsonFileWriter writer = new AtomicJsonFileWriter();

        JsonTableStore<User> users = store(config, writer, "users", "users.json", User.class);
        JsonTableStore<Resume> resumes = store(config, writer, "resumes", "resumes.json", Resume.class);
        JsonTableStore<Job> jobs = store(config, writer, "jobs", "jobs.json", Job.class);
        JsonTableStore<Application> applications = store(config, writer, "applications", "applications.json", Application.class);
        JsonTableStore<Skill> skills = store(config, writer, "skills", "skills.json", Skill.class);
        JsonTableStore<ResumeSkill> resumeSkills = store(config, writer, "resume_skills", "resume_skills.json", ResumeSkill.class);
        JsonTableStore<JobRequirement> jobRequirements = store(config, writer, "job_requirements", "job_requirements.json", JobRequirement.class);
        JsonTableStore<WorkloadRecord> workloadRecords = store(config, writer, "workload_records", "workload_records.json", WorkloadRecord.class);
        JsonTableStore<MatchScore> matchScores = store(config, writer, "match_scores", "match_scores.json", MatchScore.class);
        JsonTableStore<Notification> notifications = store(config, writer, "notifications", "notifications.json", Notification.class);
        JsonTableStore<AuditLog> auditLogs = store(config, writer, "audit_logs", "audit_logs.json", AuditLog.class);

        List.of(users, resumes, jobs, applications, skills, resumeSkills, jobRequirements,
                workloadRecords, matchScores, notifications, auditLogs)
                .forEach(JsonTableStore::load);

        this.userRepository = new JsonUserRepository(users);
        this.resumeRepository = new JsonResumeRepository(resumes);
        this.applicationRepository = new JsonApplicationRepository(applications, resumes);
        this.jobRepository = new JsonJobRepository(jobs, applications);
        this.skillRepository = new JsonSkillRepository(skills);
        this.resumeSkillRepository = new JsonResumeSkillRepository(resumeSkills);
        this.jobRequirementRepository = new JsonJobRequirementRepository(jobRequirements);
        this.workloadRecordRepository = new JsonWorkloadRecordRepository(workloadRecords, resumes);
        this.matchScoreRepository = new JsonMatchScoreRepository(matchScores);
        this.notificationRepository = new JsonNotificationRepository(notifications);
        this.auditLogRepository = new JsonAuditLogRepository(auditLogs);
        DatabaseSeeder.seedIfNeeded(this);
    }

    public static TaDatabase open(JsonStoreConfig config) {
        return new FileTaDatabase(config);
    }

    @Override
    public UserRepository users() {
        return userRepository;
    }

    @Override
    public ResumeRepository resumes() {
        return resumeRepository;
    }

    @Override
    public JobRepository jobs() {
        return jobRepository;
    }

    @Override
    public ApplicationRepository applications() {
        return applicationRepository;
    }

    @Override
    public SkillRepository skills() {
        return skillRepository;
    }

    @Override
    public ResumeSkillRepository resumeSkills() {
        return resumeSkillRepository;
    }

    @Override
    public JobRequirementRepository jobRequirements() {
        return jobRequirementRepository;
    }

    @Override
    public WorkloadRecordRepository workloadRecords() {
        return workloadRecordRepository;
    }

    @Override
    public MatchScoreRepository matchScores() {
        return matchScoreRepository;
    }

    @Override
    public NotificationRepository notifications() {
        return notificationRepository;
    }

    @Override
    public AuditLogRepository auditLogs() {
        return auditLogRepository;
    }

    @Override
    public void executeAtomically(Runnable action) {
        transactionLock.lock();
        try {
            action.run();
        } finally {
            transactionLock.unlock();
        }
    }

    private static <T extends com.bupt.ta.domain.entity.AbstractEntity> JsonTableStore<T> store(
            JsonStoreConfig config, AtomicJsonFileWriter writer, String tableName, String fileName, Class<T> rowType) {
        return new JsonTableStore<>(
                new TableDescriptor<>(tableName, config.getDataDir().resolve(fileName), VERSION, rowType),
                config.getObjectMapper(),
                writer
        );
    }

    private static void validateOrPrepareDataDir(Path dataDir) {
        try {
            Files.createDirectories(dataDir);
            Set<String> existingJson = Files.list(dataDir)
                    .filter(path -> path.getFileName().toString().endsWith(".json"))
                    .map(path -> path.getFileName().toString())
                    .collect(Collectors.toSet());
            if (!existingJson.isEmpty() && !existingJson.containsAll(TABLE_FILES)) {
                throw new DatabaseException(
                        "Incompatible existing data directory detected at " + dataDir
                                + ". Please back up and clear the directory before starting the new database system.");
            }
        } catch (IOException e) {
            throw new DatabaseException("Failed to prepare data directory: " + dataDir, e);
        }
    }
}
