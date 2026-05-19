package com.bupt.ta.service;

import com.bupt.ta.db.core.ConstraintViolationException;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Application;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.entity.ResumeSkill;
import com.bupt.ta.domain.entity.Skill;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.ApplicationStatus;
import com.bupt.ta.domain.enums.ProficiencyLevel;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.support.TestData;
import com.bupt.ta.support.TestDatabases;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ResumeServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void saveShouldValidateOwnerAndNormalizeTitle() {
        TaDatabase db = TestDatabases.open(tempDir);
        ResumeService service = new ResumeService(db);
        User user = db.users().save(TestData.user(TestData.uniqueEmail("resume-owner"), UserRole.TA, "Resume Owner"));

        Resume resume = TestData.resume(user.getId(), "  Primary Resume  ");
        Resume saved = service.create(resume);

        assertEquals("Primary Resume", saved.getTitle());
        assertEquals(1, service.listByUserId(user.getId()).stream()
                .filter(item -> saved.getId().equals(item.getId()))
                .count());

        Resume missingOwner = TestData.resume(UUID.randomUUID(), "Missing Owner");
        assertThrows(ConstraintViolationException.class, () -> service.save(missingOwner));

        user.setActive(false);
        db.users().save(user);
        Resume inactiveOwnerResume = TestData.resume(user.getId(), "Inactive Owner");
        assertThrows(ConstraintViolationException.class, () -> service.save(inactiveOwnerResume));
    }

    @Test
    void deleteShouldEnforceOwnershipAndRemoveSkillsWhenApplicationIsTerminal() {
        TaDatabase db = TestDatabases.open(tempDir);
        ResumeService service = new ResumeService(db);
        User owner = db.users().save(TestData.user(TestData.uniqueEmail("resume-delete"), UserRole.TA, "Resume Delete"));
        User other = db.users().save(TestData.user(TestData.uniqueEmail("resume-other"), UserRole.TA, "Other TA"));
        User mo = db.users().save(TestData.user(TestData.uniqueEmail("resume-mo"), UserRole.MO, "Resume MO"));
        Resume resume = db.resumes().save(TestData.resume(owner.getId(), "Delete Candidate"));
        Skill skill = db.skills().save(TestData.skill("Delete Skill " + UUID.randomUUID()));
        db.resumeSkills().save(TestData.resumeSkill(resume.getId(), skill.getId(), ProficiencyLevel.ADVANCED));
        Job job = db.jobs().save(TestData.openJob(mo.getId(), "Terminal Application Job"));
        db.applications().save(TestData.application(resume.getId(), job.getId(), ApplicationStatus.REJECTED));

        assertThrows(ConstraintViolationException.class, () -> service.delete(other.getId(), resume.getId()));

        service.delete(owner.getId(), resume.getId());

        assertTrue(db.resumes().findById(resume.getId()).isEmpty());
        assertTrue(db.resumeSkills().listByResumeId(resume.getId()).isEmpty());
    }

    @Test
    void deleteShouldRejectActiveApplicationReferences() {
        TaDatabase db = TestDatabases.open(tempDir);
        ResumeService service = new ResumeService(db);
        User owner = db.users().save(TestData.user(TestData.uniqueEmail("resume-active"), UserRole.TA, "Resume Active"));
        User mo = db.users().save(TestData.user(TestData.uniqueEmail("resume-active-mo"), UserRole.MO, "Resume Active MO"));
        Resume resume = db.resumes().save(TestData.resume(owner.getId(), "Active Candidate"));
        Job job = db.jobs().save(TestData.openJob(mo.getId(), "Active Application Job"));
        Application application = TestData.application(resume.getId(), job.getId(), ApplicationStatus.OFFER_PENDING);
        db.applications().save(application);

        assertThrows(ConstraintViolationException.class, () -> service.delete(owner.getId(), resume.getId()));
    }

    @Test
    void replaceSkillsShouldEnforceOwnershipAndBindAllRowsToResume() {
        TaDatabase db = TestDatabases.open(tempDir);
        ResumeService service = new ResumeService(db);
        User owner = db.users().save(TestData.user(TestData.uniqueEmail("replace-skills"), UserRole.TA, "Skill Owner"));
        User other = db.users().save(TestData.user(TestData.uniqueEmail("replace-other"), UserRole.TA, "Other Owner"));
        Resume resume = db.resumes().save(TestData.resume(owner.getId(), "Skill Resume"));
        Skill oldSkill = db.skills().save(TestData.skill("Old Skill " + UUID.randomUUID()));
        Skill newSkill = db.skills().save(TestData.skill("New Skill " + UUID.randomUUID()));
        db.resumeSkills().save(TestData.resumeSkill(resume.getId(), oldSkill.getId(), ProficiencyLevel.BEGINNER));

        ResumeSkill replacement = TestData.resumeSkill(UUID.randomUUID(), newSkill.getId(), ProficiencyLevel.EXPERT);
        assertThrows(ConstraintViolationException.class,
                () -> service.replaceSkills(other.getId(), resume.getId(), List.of(replacement)));

        service.replaceSkills(owner.getId(), resume.getId(), List.of(replacement));

        List<ResumeSkill> rows = db.resumeSkills().listByResumeId(resume.getId());
        assertEquals(1, rows.size());
        assertEquals(newSkill.getId(), rows.get(0).getSkillId());
        assertEquals(resume.getId(), rows.get(0).getResumeId());
    }
}
