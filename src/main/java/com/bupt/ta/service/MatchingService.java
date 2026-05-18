package com.bupt.ta.service;

import com.bupt.ta.db.core.ConstraintViolationException;
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
import com.bupt.ta.domain.value.WorkloadAggregate;
import com.bupt.ta.domain.enums.AuditAction;
import com.bupt.ta.domain.enums.EntityType;
import com.bupt.ta.domain.enums.NotificationType;
import com.bupt.ta.db.core.JsonMapperFactory;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.Instant;
import java.time.ZoneId;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

public class MatchingService {
    private final TaDatabase db;
    private final ObjectMapper mapper = JsonMapperFactory.create();

    public MatchingService(TaDatabase db) {
        this.db = db;
    }

    public MatchScore runAnalysis(UUID operatorId, UUID applicationId) {
        Application application = db.applications().findById(applicationId)
                .orElseThrow(() -> new ConstraintViolationException("Application not found: " + applicationId));
        Resume resume = db.resumes().findById(application.getResumeId())
                .orElseThrow(() -> new ConstraintViolationException("Resume not found: " + application.getResumeId()));
        Job job = db.jobs().findById(application.getJobId())
                .orElseThrow(() -> new ConstraintViolationException("Job not found: " + application.getJobId()));

        List<JobRequirement> requirements = db.jobRequirements().listByJobId(job.getId());
        List<ResumeSkill> resumeSkills = db.resumeSkills().listByResumeId(resume.getId());
        Set<UUID> resumeSkillIds = resumeSkills.stream().map(ResumeSkill::getSkillId).collect(Collectors.toSet());
        List<JobRequirement> requiredOnly = requirements.stream().filter(JobRequirement::isRequired).toList();
        List<JobRequirement> missingRequired = requiredOnly.stream()
                .filter(requirement -> !resumeSkillIds.contains(requirement.getSkillId()))
                .toList();
        int coveragePct = requiredOnly.isEmpty()
                ? 100
                : (requiredOnly.size() - missingRequired.size()) * 100 / requiredOnly.size();

        BigDecimal ruleScore = computeRuleScore(applicationId);
        BigDecimal aiScore = ruleScore;
        String aiExplanation = "AI scoring unavailable; rule-based fallback applied.";
        if (QwenAiService.resolveApiKey() != null) {
            aiExplanation = "Persistent analysis is currently using the rule-based score as the AI fallback value.";
        }

        MatchScore score = db.matchScores().findByApplicationId(applicationId).orElseGet(MatchScore::new);
        score.setApplicationId(applicationId);
        score.setRuleScore(ruleScore);
        score.setAiScore(aiScore);
        score.setFinalScore(ruleScore.add(aiScore).divide(BigDecimal.valueOf(2), 2, RoundingMode.HALF_UP));
        score.setSkillCoveragePct(coveragePct);
        score.setMissingRequiredCount(missingRequired.size());
        score.setWorkloadRemainingHours(remainingHours(resume.getUserId(), job));
        score.setMissingSkillSuggestions(computeMissingSkills(applicationId));
        score.setAiExplanation(aiExplanation);
        score.setAiRecommend(score.getFinalScore().compareTo(BigDecimal.valueOf(70)) >= 0 && missingRequired.isEmpty());
        score.setComputedAt(Instant.now());

        db.executeAtomically(() -> {
            MatchScore saved = db.matchScores().save(score);
            Notification notification = new Notification();
            notification.setUserId(job.getPostedBy());
            notification.setNotifType(NotificationType.SYSTEM);
            notification.setTitle("Match analysis ready");
            notification.setMessage("The application match analysis has been refreshed.");
            notification.setEntityType(EntityType.MATCH_SCORE);
            notification.setEntityId(saved.getId());
            db.notifications().save(notification);
            AuditLog log = new AuditLog();
            log.setOperatorId(operatorId);
            log.setAction(AuditAction.UPDATE);
            log.setEntityType(EntityType.MATCH_SCORE);
            log.setEntityId(saved.getId());
            log.setNewValue(mapper.valueToTree(saved));
            log.setOperatedAt(Instant.now());
            db.auditLogs().append(log);
        });
        return db.matchScores().findByApplicationId(applicationId).orElseThrow();
    }

    public BigDecimal computeRuleScore(UUID applicationId) {
        Application application = db.applications().findById(applicationId)
                .orElseThrow(() -> new ConstraintViolationException("Application not found: " + applicationId));
        Resume resume = db.resumes().findById(application.getResumeId())
                .orElseThrow(() -> new ConstraintViolationException("Resume not found: " + application.getResumeId()));
        Job job = db.jobs().findById(application.getJobId())
                .orElseThrow(() -> new ConstraintViolationException("Job not found: " + application.getJobId()));
        List<JobRequirement> requirements = db.jobRequirements().listByJobId(job.getId());
        List<ResumeSkill> resumeSkills = db.resumeSkills().listByResumeId(resume.getId());
        Set<UUID> resumeSkillIds = resumeSkills.stream().map(ResumeSkill::getSkillId).collect(Collectors.toSet());

        List<JobRequirement> required = requirements.stream().filter(JobRequirement::isRequired).toList();
        List<JobRequirement> optional = requirements.stream().filter(requirement -> !requirement.isRequired()).toList();
        long requiredMatched = required.stream().filter(requirement -> resumeSkillIds.contains(requirement.getSkillId())).count();
        long optionalMatched = optional.stream().filter(requirement -> resumeSkillIds.contains(requirement.getSkillId())).count();

        double requiredScore = required.isEmpty() ? 1.0 : (double) requiredMatched / required.size();
        double optionalScore = optional.isEmpty() ? 1.0 : (double) optionalMatched / optional.size();
        int remainingHours = remainingHours(resume.getUserId(), job);
        double workloadFactor = remainingHours >= 0 ? 1.0 : Math.max(0.2, 1.0 + (remainingHours / 20.0));
        double score = ((requiredScore * 0.75) + (optionalScore * 0.25)) * 100.0 * workloadFactor;
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    public List<String> computeMissingSkills(UUID applicationId) {
        Application application = db.applications().findById(applicationId)
                .orElseThrow(() -> new ConstraintViolationException("Application not found: " + applicationId));
        Resume resume = db.resumes().findById(application.getResumeId())
                .orElseThrow(() -> new ConstraintViolationException("Resume not found: " + application.getResumeId()));
        Set<UUID> ownedSkillIds = db.resumeSkills().listByResumeId(resume.getId()).stream()
                .map(ResumeSkill::getSkillId)
                .collect(Collectors.toSet());
        return db.jobRequirements().listRequiredByJobId(application.getJobId()).stream()
                .filter(requirement -> !ownedSkillIds.contains(requirement.getSkillId()))
                .map(requirement -> db.skills().findById(requirement.getSkillId()).map(Skill::getName).orElse("Unknown skill"))
                .toList();
    }

    private int remainingHours(UUID taUserId, Job job) {
        String semester = job.getStartDate() != null
                ? ((job.getStartDate().getMonthValue() >= 8 ? "Fall " : "Spring ") + job.getStartDate().getYear())
                : ((job.getDeadline().atZone(ZoneId.systemDefault()).getMonthValue() >= 8 ? "Fall " : "Spring ")
                + job.getDeadline().atZone(ZoneId.systemDefault()).getYear());
        WorkloadAggregate aggregate = db.workloadRecords().aggregateBySemester(semester).stream()
                .filter(item -> taUserId.equals(item.getTaId()))
                .findFirst()
                .orElse(null);
        return aggregate == null ? 20 : aggregate.getRemainingHours();
    }
}
