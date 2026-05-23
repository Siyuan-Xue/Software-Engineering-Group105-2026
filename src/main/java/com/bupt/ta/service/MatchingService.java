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
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;

/**
 * Computes persisted match analysis from structured skills and workload data.
 *
 * <p>The rule score is always computed locally. When external AI is unavailable,
 * the AI score intentionally falls back to the rule score so the workflow remains
 * demonstrable and explainable.</p>
 */
public class MatchingService {
    private final TaDatabase db;
    private final ObjectMapper mapper = JsonMapperFactory.create();

    /** @param db canonical persistence facade sourcing applications, workloads, notifications, and audit logs */
    public MatchingService(TaDatabase db) {
        this.db = db;
    }

    /**
     * Computes and persists the match analysis for one application.
     *
     * <p>The stored record includes rule score, AI/fallback score, skill coverage,
     * missing skill suggestions, workload remaining hours, audit evidence, and a
     * completion notification for the MO.</p>
     */
    public MatchScore runAnalysis(UUID operatorId, UUID applicationId) {
        Application application = db.applications().findById(applicationId)
                .orElseThrow(() -> new ConstraintViolationException("Application not found: " + applicationId));
        Resume resume = db.resumes().findById(application.getResumeId())
                .orElseThrow(() -> new ConstraintViolationException("Resume not found: " + application.getResumeId()));
        Job job = db.jobs().findById(application.getJobId())
                .orElseThrow(() -> new ConstraintViolationException("Job not found: " + application.getJobId()));

        SkillCoverageView coverage = computeCoverage(resume.getId(), job.getId());

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
        score.setSkillCoveragePct(coverage.getRequiredCoveragePct());
        score.setMissingRequiredCount(coverage.getMissingRequiredCount());
        score.setWorkloadRemainingHours(remainingHours(resume.getUserId(), job));
        score.setMissingSkillSuggestions(coverage.getMissingRequiredSkills());
        score.setAiExplanation(aiExplanation);
        score.setAiRecommend(score.getFinalScore().compareTo(BigDecimal.valueOf(70)) >= 0
                && coverage.getMissingRequiredCount() == 0);
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

    /**
     * Calculates the weighted rule score from skill coverage ratios and inferred workload slack.
     *
     * @return blended coverage score scaled to two decimal places; workload headroom adjusts the multiplier when semester aggregates show deficits
     */
    public BigDecimal computeRuleScore(UUID applicationId) {
        Application application = db.applications().findById(applicationId)
                .orElseThrow(() -> new ConstraintViolationException("Application not found: " + applicationId));
        Resume resume = db.resumes().findById(application.getResumeId())
                .orElseThrow(() -> new ConstraintViolationException("Resume not found: " + application.getResumeId()));
        Job job = db.jobs().findById(application.getJobId())
                .orElseThrow(() -> new ConstraintViolationException("Job not found: " + application.getJobId()));
        SkillCoverageView coverage = computeCoverage(resume.getId(), job.getId());
        double requiredScore = coverage.getRequiredTotal() == 0 ? 1.0
                : (double) coverage.getRequiredMatched() / coverage.getRequiredTotal();
        double optionalScore = coverage.getOptionalTotal() == 0 ? 1.0
                : (double) coverage.getOptionalMatched() / coverage.getOptionalTotal();
        int remainingHours = remainingHours(resume.getUserId(), job);
        double workloadFactor = remainingHours >= 0 ? 1.0 : Math.max(0.2, 1.0 + (remainingHours / 20.0));
        double score = ((requiredScore * 0.75) + (optionalScore * 0.25)) * 100.0 * workloadFactor;
        return BigDecimal.valueOf(score).setScale(2, RoundingMode.HALF_UP);
    }

    /**
     * @return human-readable descriptions of mandatory skills the resume still lacks versus the vacancy
     */
    public List<String> computeMissingSkills(UUID applicationId) {
        Application application = db.applications().findById(applicationId)
                .orElseThrow(() -> new ConstraintViolationException("Application not found: " + applicationId));
        return computeCoverage(application.getResumeId(), application.getJobId()).getMissingRequiredSkills();
    }

    /**
     * Evaluates structured {@link JobRequirement} rows against the resume-linked {@link ResumeSkill} set.
     *
     * @return immutable tallies describing required/optional fulfilment plus missing descriptors
     */
    public SkillCoverageView computeCoverage(UUID resumeId, UUID jobId) {
        Resume resume = db.resumes().findById(resumeId)
                .orElseThrow(() -> new ConstraintViolationException("Resume not found: " + resumeId));
        db.jobs().findById(jobId)
                .orElseThrow(() -> new ConstraintViolationException("Job not found: " + jobId));

        Map<UUID, ResumeSkill> resumeSkills = db.resumeSkills().listByResumeId(resume.getId()).stream()
                .collect(Collectors.toMap(ResumeSkill::getSkillId, skill -> skill, (left, right) -> left));

        int requiredTotal = 0;
        int requiredMatched = 0;
        int optionalTotal = 0;
        int optionalMatched = 0;
        List<String> missingRequired = new java.util.ArrayList<>();
        List<String> missingOptional = new java.util.ArrayList<>();

        for (JobRequirement requirement : db.jobRequirements().listByJobId(jobId)) {
            boolean required = requirement.isRequired();
            if (required) {
                requiredTotal++;
            } else {
                optionalTotal++;
            }

            ResumeSkill resumeSkill = resumeSkills.get(requirement.getSkillId());
            boolean matched = resumeSkill != null
                    && meetsProficiency(resumeSkill.getProficiency(), requirement.getMinProficiency());
            if (matched) {
                if (required) {
                    requiredMatched++;
                } else {
                    optionalMatched++;
                }
            } else if (required) {
                missingRequired.add(requirementLabel(requirement));
            } else {
                missingOptional.add(requirementLabel(requirement));
            }
        }

        return new SkillCoverageView(requiredMatched, requiredTotal, optionalMatched, optionalTotal,
                missingRequired, missingOptional);
    }

    private String requirementLabel(JobRequirement requirement) {
        String skillName = db.skills().findById(requirement.getSkillId()).map(Skill::getName).orElse("Unknown skill");
        return requirement.getMinProficiency() == null
                ? skillName
                : skillName + " (" + requirement.getMinProficiency() + "+)";
    }

    private static boolean meetsProficiency(com.bupt.ta.domain.enums.ProficiencyLevel actual,
                                            com.bupt.ta.domain.enums.ProficiencyLevel minimum) {
        if (minimum == null) {
            return true;
        }
        return actual != null && actual.ordinal() >= minimum.ordinal();
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

    /** Immutable tally of structured skill coverage between one resume snapshot and vacancy requirements. */
    public static final class SkillCoverageView {
        private final int requiredMatched;
        private final int requiredTotal;
        private final int optionalMatched;
        private final int optionalTotal;
        private final List<String> missingRequiredSkills;
        private final List<String> missingOptionalSkills;

        SkillCoverageView(int requiredMatched, int requiredTotal, int optionalMatched, int optionalTotal,
                          List<String> missingRequiredSkills, List<String> missingOptionalSkills) {
            this.requiredMatched = requiredMatched;
            this.requiredTotal = requiredTotal;
            this.optionalMatched = optionalMatched;
            this.optionalTotal = optionalTotal;
            this.missingRequiredSkills = List.copyOf(missingRequiredSkills);
            this.missingOptionalSkills = List.copyOf(missingOptionalSkills);
        }

        /** @return fulfilled mandatory requirement rows detected on the resume */
        public int getRequiredMatched() {
            return requiredMatched;
        }

        /** @return total mandatory requirement rows declared on the job */
        public int getRequiredTotal() {
            return requiredTotal;
        }

        /** @return fulfilled optional requirement rows detected on the resume */
        public int getOptionalMatched() {
            return optionalMatched;
        }

        /** @return total optional requirement rows declared on the job */
        public int getOptionalTotal() {
            return optionalTotal;
        }

        /** @return percentage covering mandatory skill rows only ({@code 100} when no requirements exist) */
        public int getRequiredCoveragePct() {
            return percent(requiredMatched, requiredTotal);
        }

        /** @return percentage covering mandatory and optional requirement rows jointly */
        public int getOverallCoveragePct() {
            return percent(requiredMatched + optionalMatched, requiredTotal + optionalTotal);
        }

        /** @return count of mandatory requirements still unmatched */
        public int getMissingRequiredCount() {
            return missingRequiredSkills.size();
        }

        /** @return {@code true} when mandatory coverage sinks below fifty percent */
        public boolean isLowCoverageWarning() {
            return getRequiredCoveragePct() < 50;
        }

        /** @return defensive copy backing list of textual gap descriptors for mandatory skills */
        public List<String> getMissingRequiredSkills() {
            return missingRequiredSkills;
        }

        /** @return defensive copy backing list of textual gap descriptors for optional skills */
        public List<String> getMissingOptionalSkills() {
            return missingOptionalSkills;
        }

        private static int percent(int matched, int total) {
            return total == 0 ? 100 : matched * 100 / total;
        }
    }
}
