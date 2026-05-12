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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

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

    private User user(String email, UserRole role, String name) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setRole(role);
        user.setFullName(name);
        return user;
    }
}
