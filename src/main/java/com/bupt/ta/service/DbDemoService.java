package com.bupt.ta.service;

import com.bupt.ta.db.core.ConstraintViolationException;
import com.bupt.ta.db.core.JsonMapperFactory;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Application;
import com.bupt.ta.domain.entity.AuditLog;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.Notification;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.entity.WorkloadRecord;
import com.bupt.ta.domain.enums.JobStatus;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.domain.value.AvailabilitySlot;
import com.bupt.ta.util.PasswordUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class DbDemoService {
    private static final TypeReference<List<AvailabilitySlot>> AVAILABILITY_TYPE = new TypeReference<>() { };

    private final TaDatabase db;
    private final ResumeService resumeService;
    private final JobService jobService;
    private final ApplicationService applicationService;
    private final ObjectMapper mapper;

    public DbDemoService(TaDatabase db) {
        this.db = db;
        this.resumeService = new ResumeService(db);
        this.jobService = new JobService(db);
        this.applicationService = new ApplicationService(db);
        this.mapper = JsonMapperFactory.create();
    }

    public List<User> listUsers() {
        return db.users().findAll().stream()
                .sorted(Comparator.comparing(User::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public List<User> listRecruiters() {
        return listUsers().stream()
                .filter(user -> user.getRole() == UserRole.MO || user.getRole() == UserRole.ADMIN)
                .toList();
    }

    public List<Resume> listResumes() {
        return db.resumes().findAll().stream()
                .sorted(Comparator.comparing(Resume::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public List<Job> listJobs() {
        return db.jobs().findAll().stream()
                .sorted(Comparator.comparing(Job::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public List<Application> listApplications() {
        return db.applications().findAll().stream()
                .sorted(Comparator.comparing(Application::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    public List<Notification> listRecentNotifications(int limit) {
        return db.notifications().findAll().stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .toList();
    }

    public List<AuditLog> listRecentAuditLogs(int limit) {
        return db.auditLogs().findAll().stream()
                .sorted(Comparator.comparing(AuditLog::getOperatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .toList();
    }

    public List<WorkloadRecord> listRecentWorkloadRecords(int limit) {
        return db.workloadRecords().findAll().stream()
                .sorted(Comparator.comparing(WorkloadRecord::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .toList();
    }

    public List<TableCount> listTableCounts() {
        return List.of(
                new TableCount("users", db.users().findAll().size(), "users.json"),
                new TableCount("resumes", db.resumes().findAll().size(), "resumes.json"),
                new TableCount("jobs", db.jobs().findAll().size(), "jobs.json"),
                new TableCount("applications", db.applications().findAll().size(), "applications.json"),
                new TableCount("skills", db.skills().findAll().size(), "skills.json"),
                new TableCount("resume_skills", db.resumeSkills().findAll().size(), "resume_skills.json"),
                new TableCount("job_requirements", db.jobRequirements().findAll().size(), "job_requirements.json"),
                new TableCount("workload_records", db.workloadRecords().findAll().size(), "workload_records.json"),
                new TableCount("match_scores", db.matchScores().findAll().size(), "match_scores.json"),
                new TableCount("notifications", db.notifications().findAll().size(), "notifications.json"),
                new TableCount("audit_logs", db.auditLogs().findAll().size(), "audit_logs.json")
        );
    }

    public Optional<User> findUser(UUID userId) {
        return db.users().findById(userId);
    }

    public Optional<Resume> findResume(UUID resumeId) {
        return db.resumes().findById(resumeId);
    }

    public Optional<Job> findJob(UUID jobId) {
        return db.jobs().findById(jobId);
    }

    public Optional<Application> findApplication(UUID applicationId) {
        return db.applications().findById(applicationId);
    }

    public User saveUser(User submitted, String plainPassword) {
        if (submitted == null) {
            throw new ConstraintViolationException("User submission is required");
        }
        requireNonBlank(submitted.getEmail(), "User email must not be blank");
        requireNonBlank(submitted.getFullName(), "User full name must not be blank");
        if (submitted.getRole() == null) {
            throw new ConstraintViolationException("User role must not be null");
        }

        User toSave = submitted.getId() == null
                ? new User()
                : db.users().findById(submitted.getId()).map(existing -> mergeUser(existing, submitted)).orElseThrow(
                        () -> new ConstraintViolationException("User not found: " + submitted.getId()));

        toSave.setEmail(submitted.getEmail().trim());
        toSave.setFullName(submitted.getFullName().trim());
        toSave.setRole(submitted.getRole());
        toSave.setPhone(trimToNull(submitted.getPhone()));
        toSave.setDepartment(trimToNull(submitted.getDepartment()));
        toSave.setStudentId(trimToNull(submitted.getStudentId()));
        toSave.setBio(trimToNull(submitted.getBio()));
        toSave.setActive(submitted.isActive());

        if (plainPassword != null && !plainPassword.isBlank()) {
            toSave.setPasswordHash(PasswordUtil.hashPassword(plainPassword.trim()));
        } else if (toSave.getPasswordHash() == null || toSave.getPasswordHash().isBlank()) {
            throw new ConstraintViolationException("A password is required when creating a user");
        }

        return db.users().save(toSave);
    }

    public Resume saveResume(Resume submitted, String availabilitySlotsJson) {
        if (submitted == null) {
            throw new ConstraintViolationException("Resume submission is required");
        }
        Resume toSave = submitted.getId() == null
                ? submitted
                : db.resumes().findById(submitted.getId())
                        .map(existing -> mergeResume(existing, submitted))
                        .orElseThrow(() -> new ConstraintViolationException("Resume not found: " + submitted.getId()));

        toSave.setDepartment(trimToNull(submitted.getDepartment()));
        toSave.setBio(trimToNull(submitted.getBio()));
        toSave.setAvailabilitySlots(parseAvailabilitySlots(availabilitySlotsJson));
        return resumeService.save(toSave);
    }

    public Job saveJob(Job submitted) {
        if (submitted == null) {
            throw new ConstraintViolationException("Job submission is required");
        }
        Job toSave = submitted.getId() == null
                ? submitted
                : db.jobs().findById(submitted.getId())
                        .map(existing -> mergeJob(existing, submitted))
                        .orElseThrow(() -> new ConstraintViolationException("Job not found: " + submitted.getId()));

        if (toSave.getStatus() == null) {
            toSave.setStatus(JobStatus.OPEN);
        }
        if (toSave.getHourlyRate() == null) {
            toSave.setHourlyRate(new BigDecimal("20.00"));
        }
        return jobService.save(toSave);
    }

    public Application submitApplication(UUID resumeId, UUID jobId, String coverLetter) {
        Resume resume = requireResume(resumeId);
        requireJob(jobId);
        return applicationService.submit(resume.getUserId(), resumeId, jobId, trimToNull(coverLetter));
    }

    public Application startReview(UUID applicationId) {
        Application application = requireApplication(applicationId);
        Job job = requireJob(application.getJobId());
        return applicationService.startReview(job.getPostedBy(), applicationId);
    }

    public Application sendOffer(UUID applicationId) {
        Application application = requireApplication(applicationId);
        Job job = requireJob(application.getJobId());
        return applicationService.sendOffer(job.getPostedBy(), applicationId);
    }

    public Application reject(UUID applicationId, String notes) {
        Application application = requireApplication(applicationId);
        Job job = requireJob(application.getJobId());
        return applicationService.reject(job.getPostedBy(), applicationId, trimToNull(notes));
    }

    public Application acceptOffer(UUID applicationId) {
        Application application = requireApplication(applicationId);
        Resume resume = requireResume(application.getResumeId());
        return applicationService.acceptOffer(resume.getUserId(), applicationId);
    }

    public Application declineOffer(UUID applicationId) {
        Application application = requireApplication(applicationId);
        Resume resume = requireResume(application.getResumeId());
        return applicationService.declineOffer(resume.getUserId(), applicationId);
    }

    public Application withdraw(UUID applicationId) {
        Application application = requireApplication(applicationId);
        Resume resume = requireResume(application.getResumeId());
        return applicationService.withdraw(resume.getUserId(), applicationId);
    }

    public Job cancelJob(UUID jobId) {
        Job job = requireJob(jobId);
        return jobService.changeStatus(job.getPostedBy(), jobId, JobStatus.CANCELLED);
    }

    public String availabilitySlotsJson(Resume resume) {
        if (resume == null || resume.getAvailabilitySlots().isEmpty()) {
            return "";
        }
        try {
            return mapper.writerWithDefaultPrettyPrinter().writeValueAsString(resume.getAvailabilitySlots());
        } catch (IOException e) {
            throw new ConstraintViolationException("Unable to render availability slots as JSON");
        }
    }

    public Map<UUID, String> buildUserLabels() {
        return listUsers().stream().collect(java.util.stream.Collectors.toMap(
                User::getId,
                user -> user.getFullName() + " (" + user.getEmail() + ")",
                (left, right) -> left,
                java.util.LinkedHashMap::new
        ));
    }

    public Map<UUID, String> buildResumeLabels() {
        return listResumes().stream().collect(java.util.stream.Collectors.toMap(
                Resume::getId,
                resume -> resume.getTitle() + " [" + resume.getUserId() + "]",
                (left, right) -> left,
                java.util.LinkedHashMap::new
        ));
    }

    public Map<UUID, String> buildJobLabels() {
        return listJobs().stream().collect(java.util.stream.Collectors.toMap(
                Job::getId,
                job -> job.getTitle() + " [" + job.getStatus() + "]",
                (left, right) -> left,
                java.util.LinkedHashMap::new
        ));
    }

    private User mergeUser(User existing, User submitted) {
        User merged = mapper.convertValue(existing, User.class);
        merged.setId(existing.getId());
        merged.setEmail(submitted.getEmail());
        merged.setFullName(submitted.getFullName());
        merged.setRole(submitted.getRole());
        merged.setPhone(submitted.getPhone());
        merged.setDepartment(submitted.getDepartment());
        merged.setStudentId(submitted.getStudentId());
        merged.setBio(submitted.getBio());
        merged.setActive(submitted.isActive());
        return merged;
    }

    private Resume mergeResume(Resume existing, Resume submitted) {
        Resume merged = mapper.convertValue(existing, Resume.class);
        merged.setId(existing.getId());
        merged.setUserId(submitted.getUserId());
        merged.setTitle(submitted.getTitle());
        merged.setDepartment(submitted.getDepartment());
        merged.setDegreeLevel(submitted.getDegreeLevel());
        merged.setGpa(submitted.getGpa());
        merged.setBio(submitted.getBio());
        merged.setMaxWeeklyHours(submitted.getMaxWeeklyHours());
        return merged;
    }

    private Job mergeJob(Job existing, Job submitted) {
        Job merged = mapper.convertValue(existing, Job.class);
        merged.setId(existing.getId());
        merged.setPostedBy(submitted.getPostedBy());
        merged.setTitle(submitted.getTitle());
        merged.setType(submitted.getType());
        merged.setModuleCode(submitted.getModuleCode());
        merged.setDescription(submitted.getDescription());
        merged.setRequiredHours(submitted.getRequiredHours());
        merged.setSlots(submitted.getSlots());
        merged.setStatus(submitted.getStatus());
        merged.setStartDate(submitted.getStartDate());
        merged.setEndDate(submitted.getEndDate());
        merged.setDeadline(submitted.getDeadline());
        merged.setHourlyRate(submitted.getHourlyRate());
        return merged;
    }

    private List<AvailabilitySlot> parseAvailabilitySlots(String value) {
        if (value == null || value.isBlank()) {
            return List.of();
        }
        try {
            return mapper.readValue(value, AVAILABILITY_TYPE);
        } catch (IOException e) {
            throw new ConstraintViolationException("Availability slots must be a valid JSON array");
        }
    }

    private Resume requireResume(UUID resumeId) {
        return db.resumes().findById(resumeId)
                .orElseThrow(() -> new ConstraintViolationException("Resume not found: " + resumeId));
    }

    private Job requireJob(UUID jobId) {
        return db.jobs().findById(jobId)
                .orElseThrow(() -> new ConstraintViolationException("Job not found: " + jobId));
    }

    private Application requireApplication(UUID applicationId) {
        return db.applications().findById(applicationId)
                .orElseThrow(() -> new ConstraintViolationException("Application not found: " + applicationId));
    }

    private void requireNonBlank(String value, String message) {
        if (value == null || value.isBlank()) {
            throw new ConstraintViolationException(message);
        }
    }

    private String trimToNull(String value) {
        if (value == null) {
            return null;
        }
        String trimmed = value.trim();
        return trimmed.isEmpty() ? null : trimmed;
    }

    public static final class TableCount {
        private final String tableName;
        private final int rowCount;
        private final String fileName;

        public TableCount(String tableName, int rowCount, String fileName) {
            this.tableName = tableName;
            this.rowCount = rowCount;
            this.fileName = fileName;
        }

        public String getTableName() {
            return tableName;
        }

        public int getRowCount() {
            return rowCount;
        }

        public String getFileName() {
            return fileName;
        }
    }
}
