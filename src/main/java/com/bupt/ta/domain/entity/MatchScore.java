package com.bupt.ta.domain.entity;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@JsonIgnoreProperties(ignoreUnknown = true)
public class MatchScore extends AbstractEntity {
    private UUID applicationId;
    private BigDecimal ruleScore;
    private BigDecimal aiScore;
    private BigDecimal finalScore;
    private int skillCoveragePct;
    private int missingRequiredCount;
    private int workloadRemainingHours;
    private String aiExplanation;
    private List<String> missingSkillSuggestions = new ArrayList<>();
    private boolean aiRecommend;
    private Instant computedAt;

    public UUID getApplicationId() {
        return applicationId;
    }

    public void setApplicationId(UUID applicationId) {
        this.applicationId = applicationId;
    }

    public BigDecimal getRuleScore() {
        return ruleScore;
    }

    public void setRuleScore(BigDecimal ruleScore) {
        this.ruleScore = ruleScore;
    }

    public BigDecimal getAiScore() {
        return aiScore;
    }

    public void setAiScore(BigDecimal aiScore) {
        this.aiScore = aiScore;
    }

    public BigDecimal getFinalScore() {
        return finalScore;
    }

    public void setFinalScore(BigDecimal finalScore) {
        this.finalScore = finalScore;
    }

    public int getSkillCoveragePct() {
        return skillCoveragePct;
    }

    public void setSkillCoveragePct(int skillCoveragePct) {
        this.skillCoveragePct = skillCoveragePct;
    }

    public int getMissingRequiredCount() {
        return missingRequiredCount;
    }

    public void setMissingRequiredCount(int missingRequiredCount) {
        this.missingRequiredCount = missingRequiredCount;
    }

    public int getWorkloadRemainingHours() {
        return workloadRemainingHours;
    }

    public void setWorkloadRemainingHours(int workloadRemainingHours) {
        this.workloadRemainingHours = workloadRemainingHours;
    }

    public String getAiExplanation() {
        return aiExplanation;
    }

    public void setAiExplanation(String aiExplanation) {
        this.aiExplanation = aiExplanation;
    }

    public List<String> getMissingSkillSuggestions() {
        return missingSkillSuggestions == null ? new ArrayList<>() : new ArrayList<>(missingSkillSuggestions);
    }

    public void setMissingSkillSuggestions(List<String> missingSkillSuggestions) {
        this.missingSkillSuggestions = missingSkillSuggestions == null ? new ArrayList<>() : new ArrayList<>(missingSkillSuggestions);
    }

    public boolean isAiRecommend() {
        return aiRecommend;
    }

    public void setAiRecommend(boolean aiRecommend) {
        this.aiRecommend = aiRecommend;
    }

    public Instant getComputedAt() {
        return computedAt;
    }

    public void setComputedAt(Instant computedAt) {
        this.computedAt = computedAt;
    }
}
