package com.bupt.ta.service;

import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.*;
import com.bupt.ta.domain.enums.ApplicationStatus;
import com.bupt.ta.domain.enums.WorkloadStatus;
import com.bupt.ta.domain.value.AuditLogQuery;
import com.bupt.ta.domain.value.WorkloadAggregate;
import java.math.RoundingMode;
import java.math.BigDecimal;
import java.util.*;
import java.util.Locale;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Administrator widgets that expose TA utilization, departmental drill-down helpers, aggregated workload dashboards,
 * and lightweight user maintenance hooks for the `/admin/**` controllers.
 *
 * <p>{@link #buildTAWorkloads(String)} derives each TA row from ACTIVE {@link WorkloadRecord} rows grouped by tutor id.
 * Per-assignment monetisation multiples weekly hours by a fixed demo factor of eight to approximate one semester workload.</p>
 */
public class AdminService {
    private final TaDatabase db;

    /**
     * @param db façade initialised alongside portal filters
     */
    public AdminService(TaDatabase db) {
        this.db = db;
    }

    /**
     * Equivalent to calling {@link #calculateTAWorkloads(String, String, String)} with unrestricted semester/keyword/dept triple.
     *
     * @return view projection maps described in {@link #buildTAWorkloads(String)}
     */
    public List<Map<String, Object>> calculateTAWorkloads() {
        return calculateTAWorkloads(null, null, null);
    }

    /**
     * Applies keyword and departmental filters regardless of semester.
     */
    public List<Map<String, Object>> calculateTAWorkloads(String keyword, String departmentFilter) {
        return calculateTAWorkloads(null, keyword, departmentFilter);
    }

    /**
     * Primary workload grid builder respecting optional semester narrowing plus UI filters supplied from JSP widgets.
     *
     * @param semester          optional textual semester filter (ignored when {@code null})
     * @param keyword           case-insensitive match against TA name, student id or email substring
     * @param departmentFilter  exact departmental label match against TA or vacancy metadata
     * @return immutable table rows keyed by descriptive strings ({@code taId}, {@code assignedVacancies}, {@code utilizationPct}, etc.)
     */
    public List<Map<String, Object>> calculateTAWorkloads(String semester, String keyword, String departmentFilter) {
        return filterTAWorkloads(buildTAWorkloads(semester), keyword, departmentFilter);
    }

    /**
     * Convenience wrapper listing distinct department labels across all semesters.
     */
    public List<String> listWorkloadDepartmentOptions() {
        return listWorkloadDepartmentOptions(null);
    }

    /**
     * Computes filter chips for departmental drill-down respecting optional semester narrowing.
     */
    public List<String> listWorkloadDepartmentOptions(String semester) {
        return buildTAWorkloads(semester).stream()
                .flatMap(this::departmentsForWorkload)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    /** Surfaces distinct semesters present on ACTIVE workload rows for dropdown population. */
    public List<String> listWorkloadSemesterOptions() {
        return db.workloadRecords().findAll().stream()
                .filter(record -> record.getStatus() == WorkloadStatus.ACTIVE)
                .map(WorkloadRecord::getSemester)
                .filter(value -> value != null && !value.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    /**
     * Applies UI filters atop an eagerly built workload collection (typically originating from {@link #calculateTAWorkloads} overloads).
     */
    public List<Map<String, Object>> filterTAWorkloads(List<Map<String, Object>> workloads,
                                                       String keyword,
                                                       String departmentFilter) {
        return workloads.stream()
                .filter(row -> matchesWorkloadKeyword(row, keyword))
                .filter(row -> matchesWorkloadDepartment(row, departmentFilter))
                .toList();
    }

    private List<Map<String, Object>> buildTAWorkloads(String semesterFilter) {
        List<WorkloadRecord> activeRecords = db.workloadRecords().findAll().stream()
                .filter(record -> record.getStatus() == WorkloadStatus.ACTIVE)
                .filter(record -> semesterFilter == null || semesterFilter.equalsIgnoreCase(record.getSemester()))
                .collect(Collectors.toList());

        Map<UUID, List<WorkloadRecord>> recordsByTa = activeRecords.stream()
                .collect(Collectors.groupingBy(WorkloadRecord::getTaId));

        List<Map<String, Object>> result = new ArrayList<>();

        for (Map.Entry<UUID, List<WorkloadRecord>> entry : recordsByTa.entrySet()) {
            Optional<User> taUserOpt = db.users().findById(entry.getKey());
            if (taUserOpt.isEmpty()) continue;

            User taUser = taUserOpt.get();
            int capacityHours = resolveCapacityHours(taUser.getId());

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

            for (WorkloadRecord record : entry.getValue()) {
                Optional<Application> appOpt = db.applications().findById(record.getApplicationId());
                Optional<Job> jobOpt = db.jobs().findById(record.getJobId());
                if (jobOpt.isEmpty()) continue;
                Job job = jobOpt.get();
                int requiredHours = Math.max(0, record.getAssignedHours());

                Map<String, Object> vView = new HashMap<>();
                vView.put("vacancyId", job.getId());
                vView.put("applicationId", appOpt.map(Application::getId).orElse(null));
                vView.put("semester", record.getSemester());
                vView.put("title", job.getTitle());
                vView.put("courseCode", job.getModuleCode());
                vView.put("weeklyHours", requiredHours);

                BigDecimal hourlyRate = job.getHourlyRate() != null ? job.getHourlyRate() : BigDecimal.ZERO;
                vView.put("hourlyRate", hourlyRate);

                // Demo assumption: multiply weekly hours by eight to approximate workload for one semester.
                int estimatedWorkload = requiredHours * 8;
                vView.put("estimatedWorkloadHours", estimatedWorkload);
                
                BigDecimal estimatedIncomeForJob = hourlyRate.multiply(BigDecimal.valueOf(estimatedWorkload));
                vView.put("estimatedIncome", estimatedIncomeForJob);

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
            view.put("capacityHours", capacityHours);
            view.put("remainingHours", capacityHours - totalWeeklyHours);
            int utilizationPct = capacityHours <= 0 ? 0 : BigDecimal.valueOf(totalWeeklyHours)
                    .multiply(BigDecimal.valueOf(100))
                    .divide(BigDecimal.valueOf(capacityHours), 0, RoundingMode.HALF_UP)
                    .intValue();
            view.put("utilizationPct", utilizationPct);

            if (utilizationPct < 80) {
                view.put("workloadStatus", "Normal");
            } else if (utilizationPct < 100) {
                view.put("workloadStatus", "Busy");
            } else {
                view.put("workloadStatus", "Overloaded");
            }

            result.add(view);
        }

        return result;
    }

    private int resolveCapacityHours(UUID taId) {
        return db.resumes().listByUserId(taId).stream()
                .sorted(Comparator.comparing(Resume::getUpdatedAt, Comparator.nullsLast(Comparator.reverseOrder())))
                .findFirst()
                .map(Resume::getMaxWeeklyHours)
                .filter(hours -> hours > 0)
                .orElse(20);
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

    /**
     * Pass-through to repository aggregates used by workloads dashboard charts.
     */
    public List<WorkloadAggregate> workloadDashboard(String semester) {
        return db.workloadRecords().aggregateBySemester(semester);
    }

    /**
     * Administrative audit explorer delegating verbatim to persistence search indices.
     */
    public List<AuditLog> searchAuditLogs(AuditLogQuery query) {
        return db.auditLogs().search(query);
    }

    /**
     * Saves arbitrary {@link User} mutations performed from admin tooling; caller enforces uniqueness and role sanity.
     */
    public User updateUser(UUID operatorId, User user) {
        return db.users().save(user);
    }
}
