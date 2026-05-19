package com.bupt.ta.support;

import com.bupt.ta.domain.entity.Application;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.JobRequirement;
import com.bupt.ta.domain.entity.Notification;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.entity.ResumeSkill;
import com.bupt.ta.domain.entity.Skill;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.entity.WorkloadRecord;
import com.bupt.ta.domain.enums.ApplicationStatus;
import com.bupt.ta.domain.enums.DegreeLevel;
import com.bupt.ta.domain.enums.JobStatus;
import com.bupt.ta.domain.enums.JobType;
import com.bupt.ta.domain.enums.NotificationType;
import com.bupt.ta.domain.enums.ProficiencyLevel;
import com.bupt.ta.domain.enums.SkillCategory;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.domain.enums.WorkloadStatus;
import com.bupt.ta.util.PasswordUtil;

import java.math.BigDecimal;
import java.time.Instant;
import java.time.LocalDate;
import java.util.UUID;

public final class TestData {
    private TestData() {
    }

    public static String uniqueEmail(String prefix) {
        return prefix + "-" + UUID.randomUUID() + "@example.test";
    }

    public static User user(String email, UserRole role, String fullName) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash(PasswordUtil.hashPassword("password123"));
        user.setRole(role);
        user.setFullName(fullName);
        user.setActive(true);
        return user;
    }

    public static Resume resume(UUID userId, String title) {
        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setTitle(title);
        resume.setDepartment("Computer Science");
        resume.setDegreeLevel(DegreeLevel.MASTER);
        resume.setGpa(new BigDecimal("3.70"));
        resume.setMaxWeeklyHours(20);
        resume.setBio("Experienced teaching assistant.");
        return resume;
    }

    public static Job openJob(UUID posterId, String title) {
        Job job = new Job();
        job.setPostedBy(posterId);
        job.setTitle(title);
        job.setModuleCode("CS" + Math.abs(UUID.randomUUID().hashCode() % 1000));
        job.setType(JobType.MODULE_SUPPORT);
        job.setStatus(JobStatus.OPEN);
        job.setRequiredHours(8);
        job.setSlots(2);
        job.setStartDate(LocalDate.of(2026, 2, 1));
        job.setEndDate(LocalDate.of(2026, 5, 1));
        job.setDeadline(Instant.now().plusSeconds(86_400));
        job.setHourlyRate(new BigDecimal("22.00"));
        job.setDescription("Support labs and tutorials.");
        return job;
    }

    public static Application application(UUID resumeId, UUID jobId, ApplicationStatus status) {
        Application application = new Application();
        application.setResumeId(resumeId);
        application.setJobId(jobId);
        application.setStatus(status);
        return application;
    }

    public static Skill skill(String name) {
        Skill skill = new Skill();
        skill.setName(name);
        skill.setCategory(SkillCategory.PROGRAMMING);
        skill.setDescription(name + " skill");
        return skill;
    }

    public static ResumeSkill resumeSkill(UUID resumeId, UUID skillId, ProficiencyLevel proficiency) {
        ResumeSkill resumeSkill = new ResumeSkill();
        resumeSkill.setResumeId(resumeId);
        resumeSkill.setSkillId(skillId);
        resumeSkill.setProficiency(proficiency);
        resumeSkill.setYearsExp(2);
        return resumeSkill;
    }

    public static JobRequirement requirement(UUID jobId, UUID skillId, boolean required, ProficiencyLevel proficiency) {
        JobRequirement requirement = new JobRequirement();
        requirement.setJobId(jobId);
        requirement.setSkillId(skillId);
        requirement.setRequired(required);
        requirement.setMinProficiency(proficiency);
        return requirement;
    }

    public static WorkloadRecord workload(UUID taId, UUID jobId, UUID applicationId, String semester, int hours) {
        WorkloadRecord workload = new WorkloadRecord();
        workload.setTaId(taId);
        workload.setJobId(jobId);
        workload.setApplicationId(applicationId);
        workload.setSemester(semester);
        workload.setAssignedHours(hours);
        workload.setStatus(WorkloadStatus.ACTIVE);
        return workload;
    }

    public static Notification notification(UUID userId, NotificationType type, String title) {
        Notification notification = new Notification();
        notification.setUserId(userId);
        notification.setNotifType(type);
        notification.setTitle(title);
        notification.setMessage(title + " message");
        return notification;
    }
}
