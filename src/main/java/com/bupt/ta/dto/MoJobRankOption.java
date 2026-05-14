package com.bupt.ta.dto;

/**
 * MO application list: a posted job that has at least two (non-withdrawn) applicants for AI ranking.
 */
public class MoJobRankOption {
    private final String jobId;
    private final String title;
    private final int applicantCount;

    public MoJobRankOption(String jobId, String title, int applicantCount) {
        this.jobId = jobId;
        this.title = title;
        this.applicantCount = applicantCount;
    }

    public String getJobId() {
        return jobId;
    }

    public String getTitle() {
        return title;
    }

    public int getApplicantCount() {
        return applicantCount;
    }
}
