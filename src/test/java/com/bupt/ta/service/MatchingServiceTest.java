package com.bupt.ta.service;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.db.core.ConstraintViolationException;
import com.bupt.ta.db.core.JsonStoreConfig;
import com.bupt.ta.db.facade.FileTaDatabase;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Application;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.JobRequirement;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.entity.ResumeSkill;
import com.bupt.ta.domain.entity.Skill;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.entity.WorkloadRecord;
import com.bupt.ta.domain.enums.ApplicationStatus;
import com.bupt.ta.domain.enums.DegreeLevel;
import com.bupt.ta.domain.enums.JobStatus;
import com.bupt.ta.domain.enums.JobType;
import com.bupt.ta.domain.enums.ProficiencyLevel;
import com.bupt.ta.domain.enums.SkillCategory;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.domain.enums.WorkloadStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MatchingServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void runAnalysisShouldPersistMatchScoreUsingFallbackCapablePipeline() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        MatchingService service = new MatchingService(db);
        int matchScoreCount = db.matchScores().findAll().size();
        int notificationCount = db.notifications().findAll().size();
        int auditLogCount = db.auditLogs().findAll().size();

        User ta = db.users().save(user("ta@example.com", UserRole.TA, "TA User"));
        User mo = db.users().save(user("matching-mo@example.com", UserRole.MO, "MO User"));

        Resume resume = new Resume();
        resume.setUserId(ta.getId());
        resume.setTitle("Resume");
        resume.setDegreeLevel(DegreeLevel.MASTER);
        resume = db.resumes().save(resume);

        Job job = new Job();
        job.setPostedBy(mo.getId());
        job.setTitle("Java TA");
        job.setType(JobType.MODULE_SUPPORT);
        job.setStatus(JobStatus.OPEN);
        job.setRequiredHours(8);
        job.setDeadline(Instant.now().plusSeconds(3600));
        job = db.jobs().save(job);

        Skill java = new Skill();
        java.setName("Kotlin");
        java.setCategory(SkillCategory.PROGRAMMING);
        java = db.skills().save(java);

        ResumeSkill resumeSkill = new ResumeSkill();
        resumeSkill.setResumeId(resume.getId());
        resumeSkill.setSkillId(java.getId());
        resumeSkill.setProficiency(ProficiencyLevel.ADVANCED);
        db.resumeSkills().save(resumeSkill);

        JobRequirement requirement = new JobRequirement();
        requirement.setJobId(job.getId());
        requirement.setSkillId(java.getId());
        requirement.setRequired(true);
        requirement.setMinProficiency(ProficiencyLevel.INTERMEDIATE);
        db.jobRequirements().save(requirement);

        Application application = new Application();
        application.setResumeId(resume.getId());
        application.setJobId(job.getId());
        application.setStatus(ApplicationStatus.PENDING);
        application = db.applications().save(application);

        var score = service.runAnalysis(mo.getId(), application.getId());

        assertNotNull(score.getRuleScore());
        assertNotNull(score.getFinalScore());
        assertEquals(matchScoreCount + 1, db.matchScores().findAll().size());
        assertEquals(notificationCount + 1, db.notifications().findAll().size());
        assertEquals(auditLogCount + 1, db.auditLogs().findAll().size());
    }

    @Test
    void computeCoverageShouldRespectRequiredSkillsAndMinimumProficiency() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        MatchingService service = new MatchingService(db);

        User ta = db.users().save(user("coverage-ta@example.com", UserRole.TA, "Coverage TA"));
        User mo = db.users().save(user("coverage-mo@example.com", UserRole.MO, "Coverage MO"));

        Resume resume = new Resume();
        resume.setUserId(ta.getId());
        resume.setTitle("Coverage Resume");
        resume.setDegreeLevel(DegreeLevel.MASTER);
        resume = db.resumes().save(resume);

        Job job = new Job();
        job.setPostedBy(mo.getId());
        job.setTitle("Coverage Job");
        job.setType(JobType.MODULE_SUPPORT);
        job.setStatus(JobStatus.OPEN);
        job.setRequiredHours(8);
        job.setDeadline(Instant.now().plusSeconds(3600));
        job = db.jobs().save(job);

        Skill java = skill("Coverage Java " + UUID.randomUUID());
        java = db.skills().save(java);
        Skill sql = skill("Coverage SQL " + UUID.randomUUID());
        sql = db.skills().save(sql);

        ResumeSkill javaSkill = new ResumeSkill();
        javaSkill.setResumeId(resume.getId());
        javaSkill.setSkillId(java.getId());
        javaSkill.setProficiency(ProficiencyLevel.BEGINNER);
        db.resumeSkills().save(javaSkill);

        db.jobRequirements().save(requirement(job.getId(), java.getId(), true, ProficiencyLevel.INTERMEDIATE));
        db.jobRequirements().save(requirement(job.getId(), sql.getId(), true, ProficiencyLevel.BEGINNER));

        MatchingService.SkillCoverageView coverage = service.computeCoverage(resume.getId(), job.getId());

        assertEquals(0, coverage.getRequiredMatched());
        assertEquals(2, coverage.getRequiredTotal());
        assertEquals(0, coverage.getRequiredCoveragePct());
        assertEquals(2, coverage.getMissingRequiredCount());
        assertTrue(coverage.isLowCoverageWarning());
    }

    @Test
    void computeCoverageShouldTrackOptionalSkillsSeparatelyAndTreatNoRequirementsAsFullCoverage() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        MatchingService service = new MatchingService(db);
        User ta = db.users().save(user("optional-ta-" + UUID.randomUUID() + "@example.com", UserRole.TA, "Optional TA"));
        User mo = db.users().save(user("optional-mo-" + UUID.randomUUID() + "@example.com", UserRole.MO, "Optional MO"));
        Resume resume = resume(ta.getId(), "Optional Resume");
        resume = db.resumes().save(resume);
        Job job = job(mo.getId(), "Optional Job");
        job = db.jobs().save(job);
        Skill matched = db.skills().save(skill("Optional Matched " + UUID.randomUUID()));
        Skill missing = db.skills().save(skill("Optional Missing " + UUID.randomUUID()));
        db.resumeSkills().save(resumeSkill(resume.getId(), matched.getId(), ProficiencyLevel.ADVANCED));
        db.jobRequirements().save(requirement(job.getId(), matched.getId(), false, ProficiencyLevel.INTERMEDIATE));
        db.jobRequirements().save(requirement(job.getId(), missing.getId(), false, ProficiencyLevel.BEGINNER));

        MatchingService.SkillCoverageView coverage = service.computeCoverage(resume.getId(), job.getId());

        assertEquals(0, coverage.getRequiredTotal());
        assertEquals(100, coverage.getRequiredCoveragePct());
        assertEquals(1, coverage.getOptionalMatched());
        assertEquals(2, coverage.getOptionalTotal());
        assertEquals(50, coverage.getOverallCoveragePct());
        assertEquals(1, coverage.getMissingOptionalSkills().size());
    }

    @Test
    void computeRuleScoreShouldApplyWorkloadPenaltyAndThrowForMissingApplication() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        MatchingService service = new MatchingService(db);
        User ta = db.users().save(user("penalty-ta-" + UUID.randomUUID() + "@example.com", UserRole.TA, "Penalty TA"));
        User mo = db.users().save(user("penalty-mo-" + UUID.randomUUID() + "@example.com", UserRole.MO, "Penalty MO"));
        Resume resume = resume(ta.getId(), "Penalty Resume");
        resume.setMaxWeeklyHours(10);
        resume = db.resumes().save(resume);
        Job job = job(mo.getId(), "Penalty Job");
        job.setStartDate(java.time.LocalDate.of(2026, 2, 1));
        job = db.jobs().save(job);
        Application application = new Application();
        application.setResumeId(resume.getId());
        application.setJobId(job.getId());
        application.setStatus(ApplicationStatus.PENDING);
        application = db.applications().save(application);
        WorkloadRecord workload = new WorkloadRecord();
        workload.setTaId(ta.getId());
        workload.setJobId(job.getId());
        workload.setApplicationId(application.getId());
        workload.setSemester("Spring 2026");
        workload.setAssignedHours(20);
        workload.setStatus(WorkloadStatus.ACTIVE);
        db.workloadRecords().save(workload);

        assertEquals("50.00", service.computeRuleScore(application.getId()).toPlainString());
        assertThrows(ConstraintViolationException.class, () -> service.computeRuleScore(UUID.randomUUID()));
    }

    private Skill skill(String name) {
        Skill skill = new Skill();
        skill.setName(name);
        skill.setCategory(SkillCategory.PROGRAMMING);
        return skill;
    }

    private JobRequirement requirement(java.util.UUID jobId, java.util.UUID skillId, boolean required,
                                       ProficiencyLevel proficiency) {
        JobRequirement requirement = new JobRequirement();
        requirement.setJobId(jobId);
        requirement.setSkillId(skillId);
        requirement.setRequired(required);
        requirement.setMinProficiency(proficiency);
        return requirement;
    }

    private Resume resume(java.util.UUID userId, String title) {
        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setTitle(title);
        resume.setDegreeLevel(DegreeLevel.MASTER);
        resume.setMaxWeeklyHours(20);
        return resume;
    }

    private Job job(java.util.UUID posterId, String title) {
        Job job = new Job();
        job.setPostedBy(posterId);
        job.setTitle(title);
        job.setType(JobType.MODULE_SUPPORT);
        job.setStatus(JobStatus.OPEN);
        job.setRequiredHours(8);
        job.setDeadline(Instant.now().plusSeconds(3600));
        return job;
    }

    private ResumeSkill resumeSkill(java.util.UUID resumeId, java.util.UUID skillId, ProficiencyLevel proficiency) {
        ResumeSkill resumeSkill = new ResumeSkill();
        resumeSkill.setResumeId(resumeId);
        resumeSkill.setSkillId(skillId);
        resumeSkill.setProficiency(proficiency);
        return resumeSkill;
    }

    private User user(String email, UserRole role, String name) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setRole(role);
        user.setFullName(name);
        return user;
    }
}
