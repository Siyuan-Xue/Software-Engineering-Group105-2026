package com.bupt.ta.service;

import com.bupt.ta.db.core.ConstraintViolationException;
import com.bupt.ta.db.core.JsonMapperFactory;
import com.bupt.ta.db.facade.TaDatabase;
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
import com.bupt.ta.domain.enums.JobStatus;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.domain.value.AvailabilitySlot;
import com.bupt.ta.util.PasswordUtil;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

/**
 * Facade powering the database demo tooling and integration scenarios that exercise cohesive workflows.
 *
 * <p>{@link TaDatabase} remains reachable for raw JSON-table inspection, while higher-level helpers delegate to
 * {@link ResumeService}, {@link JobService}, {@link ApplicationService}, and {@link MatchingService} to keep business
 * rules aligned with production paths.</p>
 */
public class DbDemoService {
    private static final TypeReference<List<AvailabilitySlot>> AVAILABILITY_TYPE = new TypeReference<>() { };

    private final TaDatabase db;
    private final ResumeService resumeService;
    private final JobService jobService;
    private final ApplicationService applicationService;
    private final MatchingService matchingService;
    private final ObjectMapper mapper;

    /** @param db shared persistence facade powering every table walkthrough */
    public DbDemoService(TaDatabase db) {
        this.db = db;
        this.resumeService = new ResumeService(db);
        this.jobService = new JobService(db);
        this.applicationService = new ApplicationService(db);
        this.matchingService = new MatchingService(db);
        this.mapper = JsonMapperFactory.create();
    }

    /** @return every user row ordered by {@link User#getUpdatedAt()} descending */
    public List<User> listUsers() {
        return db.users().findAll().stream()
                .sorted(Comparator.comparing(User::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    /** @return MO and ADMIN accounts only, newest activity first */
    public List<User> listRecruiters() {
        return listUsers().stream()
                .filter(user -> user.getRole() == UserRole.MO || user.getRole() == UserRole.ADMIN)
                .toList();
    }

    /** @return resumes ordered by {@link Resume#getUpdatedAt()} descending */
    public List<Resume> listResumes() {
        return db.resumes().findAll().stream()
                .sorted(Comparator.comparing(Resume::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    /** @return vacancies ordered by {@link Job#getUpdatedAt()} descending */
    public List<Job> listJobs() {
        return db.jobs().findAll().stream()
                .sorted(Comparator.comparing(Job::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    /** @return applications ordered by {@link Application#getUpdatedAt()} descending */
    public List<Application> listApplications() {
        return db.applications().findAll().stream()
                .sorted(Comparator.comparing(Application::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    /** @return global skill catalogue ordered by {@link Skill#getUpdatedAt()} descending */
    public List<Skill> listSkills() {
        return db.skills().findAll().stream()
                .sorted(Comparator.comparing(Skill::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    /** @return resume-skill junction rows ordered by {@link ResumeSkill#getUpdatedAt()} descending */
    public List<ResumeSkill> listResumeSkills() {
        return db.resumeSkills().findAll().stream()
                .sorted(Comparator.comparing(ResumeSkill::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    /** @return structured requirements ordered by {@link JobRequirement#getUpdatedAt()} descending */
    public List<JobRequirement> listJobRequirements() {
        return db.jobRequirements().findAll().stream()
                .sorted(Comparator.comparing(JobRequirement::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    /** @return persisted analytics rows ordered primarily by {@link MatchScore#getComputedAt()} */
    public List<MatchScore> listMatchScores() {
        return db.matchScores().findAll().stream()
                .sorted(Comparator.comparing(MatchScore::getComputedAt, Comparator.nullsLast(Comparator.reverseOrder()))
                        .thenComparing(MatchScore::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .toList();
    }

    /** @param limit maximum rows to include from the notification stream */
    public List<Notification> listRecentNotifications(int limit) {
        return db.notifications().findAll().stream()
                .sorted(Comparator.comparing(Notification::getCreatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .toList();
    }

    /** @param limit maximum audit events to return */
    public List<AuditLog> listRecentAuditLogs(int limit) {
        return db.auditLogs().findAll().stream()
                .sorted(Comparator.comparing(AuditLog::getOperatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .toList();
    }

    /** @param limit maximum workload ledger entries to return */
    public List<WorkloadRecord> listRecentWorkloadRecords(int limit) {
        return db.workloadRecords().findAll().stream()
                .sorted(Comparator.comparing(WorkloadRecord::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .limit(limit)
                .toList();
    }

    /** @return row counts aligned with JSON fixture filenames for the dashboard summary cards */
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

    /** @return direct lookup helper for editable {@code User} tuples */
    public Optional<User> findUser(UUID userId) {
        return db.users().findById(userId);
    }

    /** @return resume snapshot keyed by identifier */
    public Optional<Resume> findResume(UUID resumeId) {
        return db.resumes().findById(resumeId);
    }

    /** @return vacancy tuple for binding demo forms */
    public Optional<Job> findJob(UUID jobId) {
        return db.jobs().findById(jobId);
    }

    /** @return application linkage row for downstream workflow mutations */
    public Optional<Application> findApplication(UUID applicationId) {
        return db.applications().findById(applicationId);
    }

    /** @return master skill definition keyed by catalogue id */
    public Optional<Skill> findSkill(UUID skillId) {
        return db.skills().findById(skillId);
    }

    /** @return bridge row between resume drafts and proficiency metadata */
    public Optional<ResumeSkill> findResumeSkill(UUID resumeSkillId) {
        return db.resumeSkills().findById(resumeSkillId);
    }

    /** @return structured vacancy requirement keyed by surrogate id */
    public Optional<JobRequirement> findJobRequirement(UUID requirementId) {
        return db.jobRequirements().findById(requirementId);
    }

    /**
     * Creates or mutates demo accounts with password hygiene mirroring interactive registration.
     *
     * @param plainPassword hashed when supplied; omitted updates keep the prior hash
     */
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

    /**
     * Persists resumes while parsing textual availability payloads into structured slots.
     *
     * @param availabilitySlotsJson JSON array serialisation conforming to {@link AvailabilitySlot}
     */
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

    /**
     * Saves vacancies through {@link JobService} defaults (status {@link JobStatus#OPEN}, hourly rate stub).
     */
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

    /** Validates catalogue constraints before inserting or patching global skills. */
    public Skill saveSkill(Skill submitted) {
        if (submitted == null) {
            throw new ConstraintViolationException("Skill submission is required");
        }
        requireNonBlank(submitted.getName(), "Skill name must not be blank");
        if (submitted.getCategory() == null) {
            throw new ConstraintViolationException("Skill category must not be null");
        }

        Skill toSave = submitted.getId() == null
                ? new Skill()
                : db.skills().findById(submitted.getId())
                        .map(existing -> mergeSkill(existing, submitted))
                        .orElseThrow(() -> new ConstraintViolationException("Skill not found: " + submitted.getId()));

        toSave.setName(submitted.getName().trim());
        toSave.setCategory(submitted.getCategory());
        toSave.setDescription(trimToNull(submitted.getDescription()));
        return db.skills().save(toSave);
    }

    /** Enforces uniqueness between resume drafts and enumerated skills prior to persistence. */
    public ResumeSkill saveResumeSkill(ResumeSkill submitted) {
        if (submitted == null) {
            throw new ConstraintViolationException("Resume skill submission is required");
        }
        requireResume(submitted.getResumeId());
        requireSkill(submitted.getSkillId());
        if (submitted.getProficiency() == null) {
            throw new ConstraintViolationException("Resume skill proficiency must not be null");
        }
        if (submitted.getYearsExp() < 0) {
            throw new ConstraintViolationException("Resume skill yearsExp must not be negative");
        }
        ensureResumeSkillUnique(submitted);

        ResumeSkill toSave = submitted.getId() == null
                ? new ResumeSkill()
                : db.resumeSkills().findById(submitted.getId())
                        .map(existing -> mergeResumeSkill(existing, submitted))
                        .orElseThrow(() -> new ConstraintViolationException("Resume skill not found: " + submitted.getId()));

        toSave.setResumeId(submitted.getResumeId());
        toSave.setSkillId(submitted.getSkillId());
        toSave.setProficiency(submitted.getProficiency());
        toSave.setYearsExp(submitted.getYearsExp());
        return db.resumeSkills().save(toSave);
    }

    /** Mirrors {@link ResumeSkill} validation for prerequisite rows on vacancy postings. */
    public JobRequirement saveJobRequirement(JobRequirement submitted) {
        if (submitted == null) {
            throw new ConstraintViolationException("Job requirement submission is required");
        }
        requireJob(submitted.getJobId());
        requireSkill(submitted.getSkillId());
        if (submitted.getMinProficiency() == null) {
            throw new ConstraintViolationException("Job requirement proficiency must not be null");
        }
        ensureJobRequirementUnique(submitted);

        JobRequirement toSave = submitted.getId() == null
                ? new JobRequirement()
                : db.jobRequirements().findById(submitted.getId())
                        .map(existing -> mergeJobRequirement(existing, submitted))
                        .orElseThrow(() -> new ConstraintViolationException("Job requirement not found: " + submitted.getId()));

        toSave.setJobId(submitted.getJobId());
        toSave.setSkillId(submitted.getSkillId());
        toSave.setRequired(submitted.isRequired());
        toSave.setMinProficiency(submitted.getMinProficiency());
        return db.jobRequirements().save(toSave);
    }

    /** Recomputes analytic payloads under the recruiter who originated the vacancy. */
    public MatchScore refreshMatchScore(UUID applicationId) {
        Application application = requireApplication(applicationId);
        Job job = requireJob(application.getJobId());
        return matchingService.runAnalysis(job.getPostedBy(), applicationId);
    }

    /** Convenience wrapper around {@link ApplicationService#submit(UUID, UUID, UUID, String)} for demos. */
    public Application submitApplication(UUID resumeId, UUID jobId, String coverLetter) {
        Resume resume = requireResume(resumeId);
        requireJob(jobId);
        return applicationService.submit(resume.getUserId(), resumeId, jobId, trimToNull(coverLetter));
    }

    /** @see ApplicationService#startReview(UUID, UUID) */
    public Application startReview(UUID applicationId) {
        Application application = requireApplication(applicationId);
        Job job = requireJob(application.getJobId());
        return applicationService.startReview(job.getPostedBy(), applicationId);
    }

    /** @see ApplicationService#sendOffer(UUID, UUID) */
    public Application sendOffer(UUID applicationId) {
        Application application = requireApplication(applicationId);
        Job job = requireJob(application.getJobId());
        return applicationService.sendOffer(job.getPostedBy(), applicationId);
    }

    /** @see ApplicationService#reject(UUID, UUID, String) */
    public Application reject(UUID applicationId, String notes) {
        Application application = requireApplication(applicationId);
        Job job = requireJob(application.getJobId());
        return applicationService.reject(job.getPostedBy(), applicationId, trimToNull(notes));
    }

    /** @see ApplicationService#acceptOffer(UUID, UUID) */
    public Application acceptOffer(UUID applicationId) {
        Application application = requireApplication(applicationId);
        Resume resume = requireResume(application.getResumeId());
        return applicationService.acceptOffer(resume.getUserId(), applicationId);
    }

    /** @see ApplicationService#declineOffer(UUID, UUID) */
    public Application declineOffer(UUID applicationId) {
        Application application = requireApplication(applicationId);
        Resume resume = requireResume(application.getResumeId());
        return applicationService.declineOffer(resume.getUserId(), applicationId);
    }

    /** @see ApplicationService#withdraw(UUID, UUID) */
    public Application withdraw(UUID applicationId) {
        Application application = requireApplication(applicationId);
        Resume resume = requireResume(application.getResumeId());
        return applicationService.withdraw(resume.getUserId(), applicationId);
    }

    /** @see JobService#changeStatus(UUID, UUID, JobStatus) */
    public Job cancelJob(UUID jobId) {
        Job job = requireJob(jobId);
        return jobService.changeStatus(job.getPostedBy(), jobId, JobStatus.CANCELLED);
    }

    /** Pretty-prints persisted availability tuples for textual editors embedded in demo forms. */
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

    /** @return display labels {@code Full Name (email)} keyed by stable user ids */
    public Map<UUID, String> buildUserLabels() {
        return listUsers().stream().collect(java.util.stream.Collectors.toMap(
                User::getId,
                user -> user.getFullName() + " (" + user.getEmail() + ")",
                (left, right) -> left,
                LinkedHashMap::new
        ));
    }

    /** @return resume titles suffixed by owning {@code UUID} keys */
    public Map<UUID, String> buildResumeLabels() {
        return listResumes().stream().collect(java.util.stream.Collectors.toMap(
                Resume::getId,
                resume -> resume.getTitle() + " [" + resume.getUserId() + "]",
                (left, right) -> left,
                LinkedHashMap::new
        ));
    }

    /** @return vacancy titles annotated with enumerated {@link JobStatus} values */
    public Map<UUID, String> buildJobLabels() {
        return listJobs().stream().collect(java.util.stream.Collectors.toMap(
                Job::getId,
                job -> job.getTitle() + " [" + job.getStatus() + "]",
                (left, right) -> left,
                LinkedHashMap::new
        ));
    }

    /** @return skill names annotated with categorical metadata */
    public Map<UUID, String> buildSkillLabels() {
        return listSkills().stream().collect(java.util.stream.Collectors.toMap(
                Skill::getId,
                skill -> skill.getName() + " (" + skill.getCategory() + ")",
                (left, right) -> left,
                LinkedHashMap::new
        ));
    }

    /** @return composite labels pairing surrogate ids with {@link com.bupt.ta.domain.enums.ApplicationStatus} markers */
    public Map<UUID, String> buildApplicationLabels() {
        return listApplications().stream().collect(java.util.stream.Collectors.toMap(
                Application::getId,
                application -> application.getId() + " [" + application.getStatus() + "]",
                (left, right) -> left,
                LinkedHashMap::new
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

    private Skill mergeSkill(Skill existing, Skill submitted) {
        Skill merged = mapper.convertValue(existing, Skill.class);
        merged.setId(existing.getId());
        merged.setName(submitted.getName());
        merged.setCategory(submitted.getCategory());
        merged.setDescription(submitted.getDescription());
        return merged;
    }

    private ResumeSkill mergeResumeSkill(ResumeSkill existing, ResumeSkill submitted) {
        ResumeSkill merged = mapper.convertValue(existing, ResumeSkill.class);
        merged.setId(existing.getId());
        merged.setResumeId(submitted.getResumeId());
        merged.setSkillId(submitted.getSkillId());
        merged.setProficiency(submitted.getProficiency());
        merged.setYearsExp(submitted.getYearsExp());
        return merged;
    }

    private JobRequirement mergeJobRequirement(JobRequirement existing, JobRequirement submitted) {
        JobRequirement merged = mapper.convertValue(existing, JobRequirement.class);
        merged.setId(existing.getId());
        merged.setJobId(submitted.getJobId());
        merged.setSkillId(submitted.getSkillId());
        merged.setRequired(submitted.isRequired());
        merged.setMinProficiency(submitted.getMinProficiency());
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

    private void ensureResumeSkillUnique(ResumeSkill submitted) {
        boolean duplicate = db.resumeSkills().listByResumeId(submitted.getResumeId()).stream()
                .anyMatch(existing -> existing.getSkillId().equals(submitted.getSkillId())
                        && !existing.getId().equals(submitted.getId()));
        if (duplicate) {
            throw new ConstraintViolationException("This resume already references the selected skill");
        }
    }

    private void ensureJobRequirementUnique(JobRequirement submitted) {
        boolean duplicate = db.jobRequirements().listByJobId(submitted.getJobId()).stream()
                .anyMatch(existing -> existing.getSkillId().equals(submitted.getSkillId())
                        && !existing.getId().equals(submitted.getId()));
        if (duplicate) {
            throw new ConstraintViolationException("This job already references the selected skill");
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

    private Skill requireSkill(UUID skillId) {
        return db.skills().findById(skillId)
                .orElseThrow(() -> new ConstraintViolationException("Skill not found: " + skillId));
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

    /** Tuple rendered by the dashboard summarising persisted JSON artefacts. */
    public static final class TableCount {
        private final String tableName;
        private final int rowCount;
        private final String fileName;

        /**
         * Binds relation metadata for template rendering.
         *
         * @param tableName canonical relation label for UI columns
         * @param rowCount  live count pulled from persistence
         * @param fileName  backing fixture filename for hyperlinking exports
         */
        public TableCount(String tableName, int rowCount, String fileName) {
            this.tableName = tableName;
            this.rowCount = rowCount;
            this.fileName = fileName;
        }

        /** @return logical table or collection name */
        public String getTableName() {
            return tableName;
        }

        /** @return number of rows observed while building the summary */
        public int getRowCount() {
            return rowCount;
        }

        /** @return JSON filename presented beside the counter */
        public String getFileName() {
            return fileName;
        }
    }
}
