package com.bupt.ta.service;

import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.*;
import com.bupt.ta.domain.enums.ApplicationStatus;
import com.bupt.ta.domain.value.AuditLogQuery;
import com.bupt.ta.domain.value.WorkloadAggregate;
import java.math.BigDecimal;
import java.util.*;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class AdminService {
    private final TaDatabase db;

    public AdminService(TaDatabase db) {
        this.db = db;
    }

    public List<Map<String, Object>> calculateTAWorkloads() {
        return calculateTAWorkloads(null, null);
    }

    public List<Map<String, Object>> calculateTAWorkloads(String keyword, String departmentFilter) {
        return filterTAWorkloads(buildTAWorkloads(), keyword, departmentFilter);
    }

    public List<String> listWorkloadDepartmentOptions() {
        return buildTAWorkloads().stream()
                .flatMap(this::departmentsForWorkload)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public List<Map<String, Object>> filterTAWorkloads(List<Map<String, Object>> workloads,
                                                       String keyword,
                                                       String departmentFilter) {
        return workloads.stream()
                .filter(row -> matchesWorkloadKeyword(row, keyword))
                .filter(row -> matchesWorkloadDepartment(row, departmentFilter))
                .toList();
    }

    private List<Map<String, Object>> buildTAWorkloads() {
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
            String taDepartment = taUser.getDepartment();
            view.put("department", taDepartment == null || taDepartment.isBlank() ? "" : taDepartment);

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

    private boolean matchesWorkloadKeyword(Map<String, Object> row, String keyword) {
        if (keyword == null || keyword.isBlank()) {
            return true;
        }
        String normalized = keyword.toLowerCase(Locale.ROOT);
        return containsIgnoreCase(row.get("taName"), normalized)
                || containsIgnoreCase(row.get("studentId"), normalized)
                || containsIgnoreCase(row.get("taEmail"), normalized);
    }

    private boolean matchesWorkloadDepartment(Map<String, Object> row, String departmentFilter) {
        if (departmentFilter == null || departmentFilter.isBlank()) {
            return true;
        }
        return departmentsForWorkload(row).anyMatch(value -> value.equalsIgnoreCase(departmentFilter));
    }

    private Stream<String> departmentsForWorkload(Map<String, Object> row) {
        List<String> values = new ArrayList<>();
        addDepartmentValue(values, row.get("department"));
        Object vacancies = row.get("assignedVacancies");
        if (vacancies instanceof List<?> list) {
            for (Object item : list) {
                if (item instanceof Map<?, ?> vacancy) {
                    addDepartmentValue(values, vacancy.get("department"));
                }
            }
        }
        return values.stream();
    }

    private static void addDepartmentValue(List<String> values, Object raw) {
        if (raw == null) {
            return;
        }
        String value = raw.toString().trim();
        if (!value.isEmpty() && !"Unknown".equalsIgnoreCase(value)) {
            values.add(value);
        }
    }

    private static boolean containsIgnoreCase(Object value, String normalizedKeyword) {
        if (value == null) {
            return false;
        }
        return value.toString().toLowerCase(Locale.ROOT).contains(normalizedKeyword);
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
