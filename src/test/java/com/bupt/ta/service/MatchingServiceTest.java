package com.bupt.ta.service;

import com.bupt.ta.config.AppConfig;
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
import com.bupt.ta.domain.enums.ApplicationStatus;
import com.bupt.ta.domain.enums.DegreeLevel;
import com.bupt.ta.domain.enums.JobStatus;
import com.bupt.ta.domain.enums.JobType;
import com.bupt.ta.domain.enums.ProficiencyLevel;
import com.bupt.ta.domain.enums.SkillCategory;
import com.bupt.ta.domain.enums.UserRole;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.Instant;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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

    private User user(String email, UserRole role, String name) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setRole(role);
        user.setFullName(name);
        return user;
    }
}
