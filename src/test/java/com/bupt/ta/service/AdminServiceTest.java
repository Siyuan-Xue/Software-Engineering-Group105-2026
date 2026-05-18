package com.bupt.ta.service;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.db.core.JsonStoreConfig;
import com.bupt.ta.db.facade.FileTaDatabase;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Application;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.entity.WorkloadRecord;
import com.bupt.ta.domain.enums.ApplicationStatus;
import com.bupt.ta.domain.enums.DegreeLevel;
import com.bupt.ta.domain.enums.JobStatus;
import com.bupt.ta.domain.enums.JobType;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.domain.enums.WorkloadStatus;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class AdminServiceTest {
    @TempDir
    Path tempDir;

    @Test
    void workloadFiltersShouldMatchTaNameAndDepartment() {
        TaDatabase db = FileTaDatabase.open(JsonStoreConfig.of(tempDir, AppConfig.createObjectMapper()));
        AdminService service = new AdminService(db);

        User ta = db.users().save(user("alice-ta@example.com", UserRole.TA, "Alice TA", "CS", "20210001"));
        User mo = db.users().save(user("alice-mo@example.com", UserRole.MO, "MO User", "MATH", null));
        Resume resume = db.resumes().save(resume(ta.getId()));
        Job job = db.jobs().save(job(mo.getId(), "Algorithms TA", "MATH101"));

        Application application = new Application();
        application.setResumeId(resume.getId());
        application.setJobId(job.getId());
        application.setStatus(ApplicationStatus.ACCEPTED);
        application = db.applications().save(application);

        WorkloadRecord record = new WorkloadRecord();
        record.setApplicationId(application.getId());
        record.setTaId(ta.getId());
        record.setJobId(job.getId());
        record.setSemester("Spring 2026");
        record.setAssignedHours(10);
        record.setStatus(WorkloadStatus.ACTIVE);
        db.workloadRecords().save(record);

        List<Map<String, Object>> all = service.calculateTAWorkloads();

        assertEquals(1, all.stream().filter(row -> ta.getId().equals(row.get("taId"))).count());
        assertEquals(1, service.filterTAWorkloads(all, "Alice", null).stream()
                .filter(row -> ta.getId().equals(row.get("taId"))).count());
        assertEquals(1, service.filterTAWorkloads(all, "20210001", null).stream()
                .filter(row -> ta.getId().equals(row.get("taId"))).count());
        assertEquals(0, service.filterTAWorkloads(all, "zz-no-such-ta", null).stream()
                .filter(row -> ta.getId().equals(row.get("taId"))).count());
        assertEquals(1, service.filterTAWorkloads(all, null, "CS").stream()
                .filter(row -> ta.getId().equals(row.get("taId"))).count());
        assertEquals(1, service.filterTAWorkloads(all, null, "MATH").stream()
                .filter(row -> ta.getId().equals(row.get("taId"))).count());
        assertEquals(0, service.filterTAWorkloads(all, null, "EE").stream()
                .filter(row -> ta.getId().equals(row.get("taId"))).count());

        assertTrue(service.listWorkloadDepartmentOptions().contains("CS"));
        assertTrue(service.listWorkloadDepartmentOptions().contains("MATH"));
    }

    private User user(String email, UserRole role, String name, String department, String studentId) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setRole(role);
        user.setFullName(name);
        user.setDepartment(department);
        user.setStudentId(studentId);
        return user;
    }

    private Resume resume(java.util.UUID userId) {
        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setTitle("Resume");
        resume.setDegreeLevel(DegreeLevel.MASTER);
        resume.setMaxWeeklyHours(20);
        return resume;
    }

    private Job job(java.util.UUID posterId, String title, String moduleCode) {
        Job job = new Job();
        job.setPostedBy(posterId);
        job.setTitle(title);
        job.setModuleCode(moduleCode);
        job.setType(JobType.MODULE_SUPPORT);
        job.setStatus(JobStatus.OPEN);
        job.setRequiredHours(10);
        job.setHourlyRate(new BigDecimal("15.00"));
        job.setDeadline(Instant.now().plusSeconds(3600));
        return job;
    }
}
