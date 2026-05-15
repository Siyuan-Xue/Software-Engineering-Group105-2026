package com.bupt.ta.service;

import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.*;
import com.bupt.ta.domain.enums.ApplicationStatus;
import com.bupt.ta.domain.value.AuditLogQuery;
import com.bupt.ta.domain.value.WorkloadAggregate;
import java.math.BigDecimal;
import java.util.*;
import java.util.stream.Collectors;

public class AdminService {
    private final TaDatabase db;

    public AdminService(TaDatabase db) {
        this.db = db;
    }

    public List<Map<String, Object>> calculateTAWorkloads() {
        List<Application> allApplications = db.applications().findAll();
        List<Application> acceptedApps = allApplications.stream()
                .filter(app -> app.getStatus() == ApplicationStatus.ACCEPTED)
                .collect(Collectors.toList());

        // Group by resumeId (which maps to TA user)
        Map<UUID, List<Application>> appsByResume = acceptedApps.stream()
                .collect(Collectors.groupingBy(Application::getResumeId));

        List<Map<String, Object>> result = new ArrayList<>();

        for (Map.Entry<UUID, List<Application>> entry : appsByResume.entrySet()) {
            Optional<Resume> resumeOpt = db.resumes().findById(entry.getKey());
            if (resumeOpt.isEmpty()) continue;
            
            Resume resume = resumeOpt.get();
            Optional<User> taUserOpt = db.users().findById(resume.getUserId());
            if (taUserOpt.isEmpty()) continue;

            User taUser = taUserOpt.get();

            Map<String, Object> view = new HashMap<>();
            view.put("taId", taUser.getId());
            view.put("taName", taUser.getFullName());
            view.put("taEmail", taUser.getEmail());
            view.put("studentId", taUser.getStudentId());

            List<Map<String, Object>> vacancyViews = new ArrayList<>();
            int totalWeeklyHours = 0;
            BigDecimal totalEstimatedIncome = BigDecimal.ZERO;

            for (Application app : entry.getValue()) {
                Optional<Job> jobOpt = db.jobs().findById(app.getJobId());
                if (jobOpt.isEmpty()) continue;
                Job job = jobOpt.get();
                int requiredHours = Math.max(0, job.getRequiredHours());

                Map<String, Object> vView = new HashMap<>();
                vView.put("vacancyId", job.getId());
                vView.put("title", job.getTitle());
                vView.put("courseCode", job.getModuleCode());
                vView.put("weeklyHours", requiredHours);

                BigDecimal hourlyRate = job.getHourlyRate() != null ? job.getHourlyRate() : BigDecimal.ZERO;
                vView.put("hourlyRate", hourlyRate);

                // Assuming 8 weeks
                int estimatedWorkload = requiredHours * 8;
                vView.put("estimatedWorkloadHours", estimatedWorkload);
                
                BigDecimal estimatedIncomeForJob = hourlyRate.multiply(BigDecimal.valueOf(estimatedWorkload));
                vView.put("estimatedIncome", estimatedIncomeForJob);

                // Fetch MO user
                Optional<User> moUserOpt = db.users().findById(job.getPostedBy());
                if (moUserOpt.isPresent()) {
                    vView.put("moduleOwner", moUserOpt.get().getFullName());
                    vView.put("department", moUserOpt.get().getDepartment());
                } else {
                    vView.put("moduleOwner", "Unknown");
                    vView.put("department", "Unknown");
                }

                vacancyViews.add(vView);
                totalWeeklyHours += requiredHours;
                totalEstimatedIncome = totalEstimatedIncome.add(estimatedIncomeForJob);
            }

            view.put("assignedVacancies", vacancyViews);
            view.put("acceptedVacancyCount", vacancyViews.size());
            view.put("totalWeeklyHours", totalWeeklyHours);
            view.put("totalWorkloadHours", totalWeeklyHours * 8);
            view.put("totalEstimatedIncome", totalEstimatedIncome);

            if (totalWeeklyHours <= 10) {
                view.put("workloadStatus", "Normal");
            } else if (totalWeeklyHours <= 15) {
                view.put("workloadStatus", "Busy");
            } else {
                view.put("workloadStatus", "Overloaded");
            }

            result.add(view);
        }

        return result;
    }

    public List<WorkloadAggregate> workloadDashboard(String semester) {
        return db.workloadRecords().aggregateBySemester(semester);
    }

    public List<AuditLog> searchAuditLogs(AuditLogQuery query) {
        return db.auditLogs().search(query);
    }

    public User updateUser(UUID operatorId, User user) {
        return db.users().save(user);
    }
}
