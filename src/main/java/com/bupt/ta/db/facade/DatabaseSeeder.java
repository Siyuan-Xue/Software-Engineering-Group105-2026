package com.bupt.ta.db.facade;

import com.bupt.ta.db.core.JsonMapperFactory;
import com.bupt.ta.domain.entity.Application;
import com.bupt.ta.domain.entity.AuditLog;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.JobRequirement;
import com.bupt.ta.domain.entity.MatchScore;
import com.bupt.ta.domain.entity.Notification;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.entity.ResumeSkill;
import com.bupt.ta.domain.entity.Skill;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.entity.WorkloadRecord;
import com.bupt.ta.domain.enums.ApplicationStatus;
import com.bupt.ta.domain.enums.AuditAction;
import com.bupt.ta.domain.enums.DegreeLevel;
import com.bupt.ta.domain.enums.EntityType;
import com.bupt.ta.domain.enums.JobStatus;
import com.bupt.ta.domain.enums.JobType;
import com.bupt.ta.domain.enums.NotificationType;
import com.bupt.ta.domain.enums.ProficiencyLevel;
import com.bupt.ta.domain.enums.SkillCategory;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.domain.enums.WorkloadStatus;
import com.bupt.ta.domain.value.AvailabilitySlot;
import com.bupt.ta.util.PasswordUtil;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ObjectNode;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.Instant;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Seeds demonstration data when the JSON database is first initialized.
 */
public final class DatabaseSeeder {
    public static final String DEFAULT_PASSWORD = "password";
    public static final String DEFAULT_TA_EMAIL = "test@example.com";
    public static final String DEFAULT_MO_EMAIL = "mo@example.com";
    public static final String DEFAULT_ADMIN_EMAIL = "admin@example.com";

    private static final ObjectMapper MAPPER = JsonMapperFactory.create();

    private DatabaseSeeder() {
    }

    public static void seedIfNeeded(TaDatabase database) {
        database.executeAtomically(() -> {
            if (!isCompletelyEmpty(database)) {
                return;
            }
            seedDemoData(database);
        });
    }

    private static boolean isCompletelyEmpty(TaDatabase database) {
        return database.users().findAll().isEmpty()
                && database.resumes().findAll().isEmpty()
                && database.jobs().findAll().isEmpty()
                && database.applications().findAll().isEmpty()
                && database.skills().findAll().isEmpty()
                && database.resumeSkills().findAll().isEmpty()
                && database.jobRequirements().findAll().isEmpty()
                && database.workloadRecords().findAll().isEmpty()
                && database.matchScores().findAll().isEmpty()
                && database.notifications().findAll().isEmpty()
                && database.auditLogs().findAll().isEmpty();
    }

    private static void seedDemoData(TaDatabase database) {
        Map<String, User> users = seedUsers(database);
        Map<String, Skill> skills = seedSkills(database);
        Map<String, Resume> resumes = seedResumes(database, users);
        seedResumeSkills(database, resumes, skills);

        Map<String, Job> jobs = seedJobs(database, users);
        seedSavedJobs(database, users, jobs);
        seedJobRequirements(database, jobs, skills);

        Map<String, Application> applications = seedApplications(database, users, resumes, jobs);
        seedMatchScores(database, applications);
        seedWorkloads(database, users, jobs, applications);
        seedNotifications(database, users, jobs, applications);
        seedAuditLogs(database, users, jobs, applications);
    }

    private static Map<String, User> seedUsers(TaDatabase database) {
        Map<String, User> users = new LinkedHashMap<>();
        List<SeedUser> seeds = List.of(
                new SeedUser(DEFAULT_TA_EMAIL, "Test User", UserRole.TA, "Computer Science", "TA1001",
                        "+44 7700 900101", "MSc student focused on Java labs and first-year programming support.", "en", "light"),
                new SeedUser("demo.ming@example.com", "Ming Chen", UserRole.TA, "Computer Science", "TA1002",
                        "+44 7700 900102", "Database and backend teaching assistant with strong SQL mentoring experience.", "en", "light"),
                new SeedUser("demo.aisha@example.com", "Aisha Rahman", UserRole.TA, "Computer Science", "TA1003",
                        "+44 7700 900103", "AI coursework mentor comfortable with Python, ML notebooks, and group tutorials.", "en", "dark"),
                new SeedUser("demo.li@example.com", "Li Wei", UserRole.TA, "Mathematics", "TA1004",
                        "+44 7700 900104", "Statistics PhD student with exam invigilation and problem-class experience.", "zh", "light"),
                new SeedUser("demo.priya@example.com", "Priya Patel", UserRole.TA, "Computer Science", "TA1005",
                        "+44 7700 900105", "Software engineering project mentor with Git and communication strengths.", "en", "light"),
                new SeedUser("demo.oliver@example.com", "Oliver Smith", UserRole.TA, "Computer Science", "TA1006",
                        "+44 7700 900106", "Programming bootcamp assistant with practical Java and Python tutoring background.", "en", "light"),
                new SeedUser(DEFAULT_MO_EMAIL, "Module Organiser", UserRole.MO, "Teaching Support", null,
                        "+44 7700 901001", "Coordinates teaching assistant hiring and module staffing.", "en", "light"),
                new SeedUser("demo.helen@example.com", "Dr Helen Carter", UserRole.MO, "Computer Science", null,
                        "+44 7700 901002", "Module organiser for programming, databases, and software engineering.", "en", "light"),
                new SeedUser("demo.zhang@example.com", "Prof David Zhang", UserRole.MO, "Mathematics", null,
                        "+44 7700 901003", "Module organiser for mathematics and statistics teaching support.", "en", "light"),
                new SeedUser(DEFAULT_ADMIN_EMAIL, "System Admin", UserRole.ADMIN, "Platform Operations", null,
                        "+44 7700 902001", "Maintains the TA recruitment platform and workload dashboard.", "en", "dark")
        );
        for (SeedUser seed : seeds) {
            User user = new User();
            user.setEmail(seed.email());
            user.setPasswordHash(PasswordUtil.hashPassword(DEFAULT_PASSWORD));
            user.setRole(seed.role());
            user.setFullName(seed.fullName());
            user.setPhone(seed.phone());
            user.setDepartment(seed.department());
            user.setStudentId(seed.studentId());
            user.setBio(seed.bio());
            user.setPreferredLanguage(seed.language());
            user.setPreferredAppearance(seed.appearance());
            user.setNotificationsEnabled(true);
            user.setActive(true);
            users.put(seed.email(), database.users().save(user));
        }
        return users;
    }

    private static Map<String, Skill> seedSkills(TaDatabase database) {
        Map<String, Skill> skills = new LinkedHashMap<>();
        List<SeedSkill> seeds = List.of(
                new SeedSkill("Java", SkillCategory.PROGRAMMING, "Object-oriented programming and lab support."),
                new SeedSkill("Python", SkillCategory.PROGRAMMING, "Scripting, notebooks, automation, and data-oriented coding."),
                new SeedSkill("SQL", SkillCategory.DOMAIN, "Relational queries, schema reasoning, and database labs."),
                new SeedSkill("Data Structures", SkillCategory.DOMAIN, "Algorithms, complexity, and data-structure tutorials."),
                new SeedSkill("Machine Learning", SkillCategory.DOMAIN, "Model evaluation, Python ML tooling, and coursework guidance."),
                new SeedSkill("Statistics", SkillCategory.DOMAIN, "Probability, inference, and statistics problem classes."),
                new SeedSkill("Git", SkillCategory.SOFT, "Version control, code review, and team project workflows."),
                new SeedSkill("Tutoring", SkillCategory.TEACHING, "Leading tutorials, Q&A sessions, and office hours."),
                new SeedSkill("Exam Invigilation", SkillCategory.TEACHING, "Exam supervision, regulations, and escalation procedures."),
                new SeedSkill("English", SkillCategory.LANGUAGE, "English communication for teaching and student support."),
                new SeedSkill("Mandarin", SkillCategory.LANGUAGE, "Mandarin communication for bilingual student support."),
                new SeedSkill("Communication", SkillCategory.SOFT, "Clear written and verbal collaboration with students and staff.")
        );
        for (SeedSkill seed : seeds) {
            Skill skill = new Skill();
            skill.setName(seed.name());
            skill.setCategory(seed.category());
            skill.setDescription(seed.description());
            skills.put(seed.name(), database.skills().save(skill));
        }
        return skills;
    }

    private static Map<String, Resume> seedResumes(TaDatabase database, Map<String, User> users) {
        Map<String, Resume> resumes = new LinkedHashMap<>();
        resumes.put(DEFAULT_TA_EMAIL, saveResume(database, users.get(DEFAULT_TA_EMAIL),
                "Java and Programming Lab Resume", "Computer Science", DegreeLevel.MASTER, "3.72", 18,
                "Experienced in Java lab support, office hours, and beginner-friendly debugging sessions.",
                List.of(slot(DayOfWeek.MONDAY, 9, 0, 12, 0), slot(DayOfWeek.WEDNESDAY, 14, 0, 17, 0))));
        resumes.put("demo.ming@example.com", saveResume(database, users.get("demo.ming@example.com"),
                "Database Systems Teaching Resume", "Computer Science", DegreeLevel.PHD, "3.90", 20,
                "Focuses on SQL, transaction concepts, and backend coursework mentoring.",
                List.of(slot(DayOfWeek.TUESDAY, 10, 0, 13, 0), slot(DayOfWeek.THURSDAY, 13, 0, 16, 0))));
        resumes.put("demo.aisha@example.com", saveResume(database, users.get("demo.aisha@example.com"),
                "AI and Python Coursework Resume", "Computer Science", DegreeLevel.MASTER, "3.86", 18,
                "Supports Python notebooks, ML experiments, and group project feedback.",
                List.of(slot(DayOfWeek.MONDAY, 13, 0, 16, 0), slot(DayOfWeek.FRIDAY, 9, 30, 12, 0))));
        resumes.put("demo.li@example.com", saveResume(database, users.get("demo.li@example.com"),
                "Statistics and Invigilation Resume", "Mathematics", DegreeLevel.PHD, "3.95", 12,
                "Comfortable running statistics problem classes and supervising exams.",
                List.of(slot(DayOfWeek.WEDNESDAY, 10, 0, 12, 0), slot(DayOfWeek.FRIDAY, 14, 0, 17, 0))));
        resumes.put("demo.priya@example.com", saveResume(database, users.get("demo.priya@example.com"),
                "Software Engineering Studio Resume", "Computer Science", DegreeLevel.MASTER, "3.68", 16,
                "Mentors teams on Git workflows, sprint planning, and Java web applications.",
                List.of(slot(DayOfWeek.TUESDAY, 14, 0, 17, 0), slot(DayOfWeek.THURSDAY, 9, 0, 11, 0))));
        resumes.put("demo.oliver@example.com", saveResume(database, users.get("demo.oliver@example.com"),
                "Programming Bootcamp Resume", "Computer Science", DegreeLevel.BACHELOR, "3.54", 14,
                "Strong practical programming support for bootcamps and drop-in help sessions.",
                List.of(slot(DayOfWeek.MONDAY, 10, 0, 12, 0), slot(DayOfWeek.WEDNESDAY, 15, 0, 18, 0))));
        return resumes;
    }

    private static Resume saveResume(TaDatabase database,
                                     User owner,
                                     String title,
                                     String department,
                                     DegreeLevel degreeLevel,
                                     String gpa,
                                     int maxWeeklyHours,
                                     String bio,
                                     List<AvailabilitySlot> availability) {
        Resume resume = new Resume();
        resume.setUserId(owner.getId());
        resume.setTitle(title);
        resume.setDepartment(department);
        resume.setDegreeLevel(degreeLevel);
        resume.setGpa(new BigDecimal(gpa));
        resume.setMaxWeeklyHours(maxWeeklyHours);
        resume.setBio(bio);
        resume.setAvailabilitySlots(availability);
        return database.resumes().save(resume);
    }

    private static void seedResumeSkills(TaDatabase database, Map<String, Resume> resumes, Map<String, Skill> skills) {
        addResumeSkills(database, resumes.get(DEFAULT_TA_EMAIL), skills,
                skill("Java", ProficiencyLevel.ADVANCED, 3),
                skill("Tutoring", ProficiencyLevel.ADVANCED, 2),
                skill("Communication", ProficiencyLevel.ADVANCED, 3),
                skill("English", ProficiencyLevel.EXPERT, 5));
        addResumeSkills(database, resumes.get("demo.ming@example.com"), skills,
                skill("SQL", ProficiencyLevel.EXPERT, 4),
                skill("Java", ProficiencyLevel.ADVANCED, 3),
                skill("Python", ProficiencyLevel.INTERMEDIATE, 2),
                skill("Tutoring", ProficiencyLevel.ADVANCED, 3));
        addResumeSkills(database, resumes.get("demo.aisha@example.com"), skills,
                skill("Python", ProficiencyLevel.EXPERT, 4),
                skill("Machine Learning", ProficiencyLevel.ADVANCED, 3),
                skill("SQL", ProficiencyLevel.INTERMEDIATE, 2),
                skill("Communication", ProficiencyLevel.ADVANCED, 3));
        addResumeSkills(database, resumes.get("demo.li@example.com"), skills,
                skill("Statistics", ProficiencyLevel.EXPERT, 5),
                skill("Exam Invigilation", ProficiencyLevel.ADVANCED, 2),
                skill("Mandarin", ProficiencyLevel.EXPERT, 5),
                skill("Tutoring", ProficiencyLevel.ADVANCED, 3));
        addResumeSkills(database, resumes.get("demo.priya@example.com"), skills,
                skill("Git", ProficiencyLevel.EXPERT, 4),
                skill("Java", ProficiencyLevel.INTERMEDIATE, 2),
                skill("Communication", ProficiencyLevel.EXPERT, 4),
                skill("Tutoring", ProficiencyLevel.INTERMEDIATE, 2));
        addResumeSkills(database, resumes.get("demo.oliver@example.com"), skills,
                skill("Java", ProficiencyLevel.INTERMEDIATE, 2),
                skill("Python", ProficiencyLevel.ADVANCED, 3),
                skill("Data Structures", ProficiencyLevel.INTERMEDIATE, 2),
                skill("English", ProficiencyLevel.EXPERT, 5));
    }

    private static void addResumeSkills(TaDatabase database, Resume resume, Map<String, Skill> skills, SeedResumeSkill... seeds) {
        for (SeedResumeSkill seed : seeds) {
            ResumeSkill resumeSkill = new ResumeSkill();
            resumeSkill.setResumeId(resume.getId());
            resumeSkill.setSkillId(skills.get(seed.name()).getId());
            resumeSkill.setProficiency(seed.proficiency());
            resumeSkill.setYearsExp(seed.yearsExp());
            database.resumeSkills().save(resumeSkill);
        }
    }

    private static Map<String, Job> seedJobs(TaDatabase database, Map<String, User> users) {
        Map<String, Job> jobs = new LinkedHashMap<>();
        LocalDate today = LocalDate.now();
        jobs.put("CS101", saveJob(database, users.get(DEFAULT_MO_EMAIL), "CS101", "CS101 Programming Fundamentals Lab Assistant",
                JobType.MODULE_SUPPORT, JobStatus.OPEN, 8, 2, today.plusDays(7), today.plusWeeks(12), 14, "22.50",
                "Support Java labs, review starter exercises, and help new students debug coursework."));
        jobs.put("CS204", saveJob(database, users.get("demo.helen@example.com"), "CS204", "CS204 Data Structures Tutorial Lead",
                JobType.MODULE_SUPPORT, JobStatus.OPEN, 12, 2, today.plusDays(10), today.plusWeeks(13), 21, "23.00",
                "Lead weekly tutorials covering lists, trees, graphs, algorithmic reasoning, and complexity."));
        jobs.put("CS305", saveJob(database, users.get("demo.helen@example.com"), "CS305", "CS305 Database Systems Lab TA",
                JobType.MODULE_SUPPORT, JobStatus.OPEN, 16, 2, today.plusDays(5), today.plusWeeks(11), 28, "24.00",
                "Assist SQL labs, ER modeling workshops, and transaction isolation coursework sessions."));
        jobs.put("CS401", saveJob(database, users.get("demo.helen@example.com"), "CS401", "CS401 AI Coursework Mentor",
                JobType.MODULE_SUPPORT, JobStatus.OPEN, 10, 3, today.plusDays(8), today.plusWeeks(12), 18, "24.50",
                "Mentor student groups using Python notebooks, model evaluation, and reproducible ML workflows."));
        jobs.put("MATH201", saveJob(database, users.get("demo.zhang@example.com"), "MATH201", "MATH201 Calculus Problem Class Assistant",
                JobType.MODULE_SUPPORT, JobStatus.OPEN, 6, 2, today.plusDays(6), today.plusWeeks(10), 25, "21.50",
                "Run problem classes and help students build confidence with weekly calculus exercises."));
        jobs.put("CS150", saveJob(database, users.get(DEFAULT_MO_EMAIL), "CS150", "CS150 Programming Bootcamp Support",
                JobType.MODULE_SUPPORT, JobStatus.OPEN, 14, 4, today.plusDays(3), today.plusWeeks(8), 10, "22.00",
                "Provide intensive bootcamp support for Java and Python basics before the main teaching term."));
        jobs.put("CS250", saveJob(database, users.get("demo.helen@example.com"), "CS250", "CS250 Software Engineering Studio Coach",
                JobType.MODULE_SUPPORT, JobStatus.OPEN, 9, 2, today.plusDays(11), today.plusWeeks(14), 35, "23.50",
                "Coach student teams on Git, issue tracking, servlet/JSP delivery, and demo preparation."));
        jobs.put("MATH301", saveJob(database, users.get("demo.zhang@example.com"), "MATH301", "MATH301 Statistics Exam Invigilator Pool",
                JobType.INVIGILATION, JobStatus.CLOSED, 4, 6, today.minusWeeks(2), today.minusWeeks(1), -3, "20.00",
                "Closed historical invigilation pool retained for admin and audit demonstrations."));
        return jobs;
    }

    private static Job saveJob(TaDatabase database,
                               User poster,
                               String moduleCode,
                               String title,
                               JobType type,
                               JobStatus status,
                               int requiredHours,
                               int slots,
                               LocalDate startDate,
                               LocalDate endDate,
                               int deadlineDaysFromNow,
                               String hourlyRate,
                               String description) {
        Job job = new Job();
        job.setPostedBy(poster.getId());
        job.setModuleCode(moduleCode);
        job.setTitle(title);
        job.setType(type);
        job.setStatus(status);
        job.setRequiredHours(requiredHours);
        job.setSlots(slots);
        job.setStartDate(startDate);
        job.setEndDate(endDate);
        job.setDeadline(Instant.now().plusSeconds(deadlineDaysFromNow * 24L * 60L * 60L));
        job.setHourlyRate(new BigDecimal(hourlyRate));
        job.setDescription(description);
        return database.jobs().save(job);
    }

    private static void seedSavedJobs(TaDatabase database, Map<String, User> users, Map<String, Job> jobs) {
        saveUserJobs(database, users.get(DEFAULT_TA_EMAIL), jobs.get("CS204"), jobs.get("CS250"));
        saveUserJobs(database, users.get("demo.ming@example.com"), jobs.get("CS305"), jobs.get("CS401"));
        saveUserJobs(database, users.get("demo.aisha@example.com"), jobs.get("CS401"), jobs.get("CS250"));
        saveUserJobs(database, users.get("demo.li@example.com"), jobs.get("MATH201"), jobs.get("MATH301"));
        saveUserJobs(database, users.get("demo.priya@example.com"), jobs.get("CS250"), jobs.get("CS101"));
        saveUserJobs(database, users.get("demo.oliver@example.com"), jobs.get("CS150"), jobs.get("CS101"));
    }

    private static void saveUserJobs(TaDatabase database, User user, Job... jobs) {
        Set<UUID> savedIds = java.util.Arrays.stream(jobs).map(Job::getId).collect(java.util.stream.Collectors.toSet());
        user.setSavedJobIds(savedIds);
        database.users().save(user);
    }

    private static void seedJobRequirements(TaDatabase database, Map<String, Job> jobs, Map<String, Skill> skills) {
        addRequirements(database, jobs.get("CS101"), skills,
                requirement("Java", true, ProficiencyLevel.INTERMEDIATE),
                requirement("Tutoring", true, ProficiencyLevel.INTERMEDIATE),
                requirement("Communication", false, ProficiencyLevel.INTERMEDIATE));
        addRequirements(database, jobs.get("CS204"), skills,
                requirement("Data Structures", true, ProficiencyLevel.ADVANCED),
                requirement("Java", true, ProficiencyLevel.INTERMEDIATE),
                requirement("Tutoring", false, ProficiencyLevel.INTERMEDIATE));
        addRequirements(database, jobs.get("CS305"), skills,
                requirement("SQL", true, ProficiencyLevel.ADVANCED),
                requirement("Python", false, ProficiencyLevel.INTERMEDIATE),
                requirement("Communication", false, ProficiencyLevel.INTERMEDIATE));
        addRequirements(database, jobs.get("CS401"), skills,
                requirement("Python", true, ProficiencyLevel.ADVANCED),
                requirement("Machine Learning", true, ProficiencyLevel.INTERMEDIATE),
                requirement("SQL", false, ProficiencyLevel.INTERMEDIATE));
        addRequirements(database, jobs.get("MATH201"), skills,
                requirement("Statistics", false, ProficiencyLevel.INTERMEDIATE),
                requirement("Tutoring", true, ProficiencyLevel.INTERMEDIATE),
                requirement("Communication", true, ProficiencyLevel.INTERMEDIATE));
        addRequirements(database, jobs.get("CS150"), skills,
                requirement("Java", true, ProficiencyLevel.INTERMEDIATE),
                requirement("Python", true, ProficiencyLevel.INTERMEDIATE),
                requirement("English", false, ProficiencyLevel.ADVANCED));
        addRequirements(database, jobs.get("CS250"), skills,
                requirement("Git", true, ProficiencyLevel.ADVANCED),
                requirement("Java", false, ProficiencyLevel.INTERMEDIATE),
                requirement("Communication", true, ProficiencyLevel.ADVANCED));
        addRequirements(database, jobs.get("MATH301"), skills,
                requirement("Exam Invigilation", true, ProficiencyLevel.INTERMEDIATE),
                requirement("Statistics", false, ProficiencyLevel.INTERMEDIATE),
                requirement("English", false, ProficiencyLevel.INTERMEDIATE));
    }

    private static void addRequirements(TaDatabase database, Job job, Map<String, Skill> skills, SeedRequirement... seeds) {
        for (SeedRequirement seed : seeds) {
            JobRequirement requirement = new JobRequirement();
            requirement.setJobId(job.getId());
            requirement.setSkillId(skills.get(seed.name()).getId());
            requirement.setRequired(seed.required());
            requirement.setMinProficiency(seed.proficiency());
            database.jobRequirements().save(requirement);
        }
    }

    private static Map<String, Application> seedApplications(TaDatabase database,
                                                             Map<String, User> users,
                                                             Map<String, Resume> resumes,
                                                             Map<String, Job> jobs) {
        Map<String, Application> applications = new LinkedHashMap<>();
        applications.put("app-test-cs101", saveApplication(database, resumes.get(DEFAULT_TA_EMAIL), jobs.get("CS101"),
                ApplicationStatus.ACCEPTED, users.get(DEFAULT_MO_EMAIL),
                "I have supported Java labs before and can cover two weekly sessions.", "Strong fit for first-year labs.", true));
        applications.put("app-ming-cs204", saveApplication(database, resumes.get("demo.ming@example.com"), jobs.get("CS204"),
                ApplicationStatus.ACCEPTED, users.get("demo.helen@example.com"),
                "I can lead data structures tutorials and connect examples to backend systems.", "Accepted for tutorial leadership.", true));
        applications.put("app-aisha-cs305", saveApplication(database, resumes.get("demo.aisha@example.com"), jobs.get("CS305"),
                ApplicationStatus.ACCEPTED, users.get("demo.helen@example.com"),
                "My SQL and Python experience can support database labs and troubleshooting.", "Accepted with good SQL foundation.", true));
        applications.put("app-priya-cs401", saveApplication(database, resumes.get("demo.priya@example.com"), jobs.get("CS401"),
                ApplicationStatus.PENDING, null,
                "I am interested in mentoring AI group work and can help with project communication.", null, false));
        applications.put("app-li-cs250", saveApplication(database, resumes.get("demo.li@example.com"), jobs.get("CS250"),
                ApplicationStatus.REVIEWING, users.get("demo.helen@example.com"),
                "I can support team projects and help students structure technical explanations.", "Reviewing availability against studio times.", false));
        applications.put("app-oliver-math201", saveApplication(database, resumes.get("demo.oliver@example.com"), jobs.get("MATH201"),
                ApplicationStatus.OFFER_PENDING, users.get("demo.zhang@example.com"),
                "I have strong tutoring availability and can support weekly problem classes.", "Offer pending TA confirmation.", false));
        applications.put("app-ming-cs150", saveApplication(database, resumes.get("demo.ming@example.com"), jobs.get("CS150"),
                ApplicationStatus.REJECTED, users.get(DEFAULT_MO_EMAIL),
                "I can help with bootcamp database-adjacent exercises.", "Profile is stronger for database and data structures roles.", false));
        applications.put("app-li-cs101", saveApplication(database, resumes.get("demo.li@example.com"), jobs.get("CS101"),
                ApplicationStatus.DECLINED, users.get(DEFAULT_MO_EMAIL),
                "I can assist with structured problem solving for Java beginners.", "Offer declined due to timetable conflict.", true));
        applications.put("app-priya-cs204", saveApplication(database, resumes.get("demo.priya@example.com"), jobs.get("CS204"),
                ApplicationStatus.WITHDRAWN, users.get("demo.helen@example.com"),
                "I am interested in supporting tutorials if my project schedule allows.", "Applicant withdrew before review completion.", true));
        return applications;
    }

    private static Application saveApplication(TaDatabase database,
                                               Resume resume,
                                               Job job,
                                               ApplicationStatus status,
                                               User reviewer,
                                               String coverLetter,
                                               String notes,
                                               boolean responded) {
        Application application = new Application();
        application.setResumeId(resume.getId());
        application.setJobId(job.getId());
        application.setStatus(status);
        application.setCoverLetter(coverLetter);
        if (reviewer != null) {
            application.setReviewedBy(reviewer.getId());
            application.setReviewedAt(Instant.now().minusSeconds(2 * 24L * 60L * 60L));
        }
        application.setMoNotes(notes);
        if (responded) {
            application.setTaRespondedAt(Instant.now().minusSeconds(24L * 60L * 60L));
        }
        return database.applications().save(application);
    }

    private static void seedMatchScores(TaDatabase database, Map<String, Application> applications) {
        saveMatch(database, applications.get("app-test-cs101"), "93.00", 100, 0, 10, true,
                "Excellent match for Java labs with strong communication and tutoring background.", List.of());
        saveMatch(database, applications.get("app-ming-cs204"), "88.50", 100, 0, -4, true,
                "Strong data structures fit; workload is intentionally high for overload dashboard demo.", List.of());
        saveMatch(database, applications.get("app-aisha-cs305"), "78.25", 100, 0, 2, true,
                "Good database lab fit with SQL and Python coverage.", List.of());
        saveMatch(database, applications.get("app-priya-cs401"), "42.50", 0, 2, 16, false,
                "Missing required AI skills; better aligned with software engineering studio support.",
                List.of("Python", "Machine Learning"));
        saveMatch(database, applications.get("app-li-cs250"), "58.00", 50, 1, 12, false,
                "Strong teaching profile but missing advanced Git evidence for studio coaching.",
                List.of("Git"));
        saveMatch(database, applications.get("app-oliver-math201"), "64.75", 50, 1, 14, false,
                "Good tutoring availability; mathematics-specific evidence is limited.",
                List.of("Communication"));
        saveMatch(database, applications.get("app-ming-cs150"), "72.00", 50, 1, -4, false,
                "Capable applicant, but profile and capacity are better used on higher-priority roles.",
                List.of("Python"));
        saveMatch(database, applications.get("app-li-cs101"), "61.50", 50, 1, 12, false,
                "Teaching skills are strong, but Java-specific experience is limited.",
                List.of("Java"));
        saveMatch(database, applications.get("app-priya-cs204"), "48.00", 0, 2, 16, false,
                "Good team coaching profile but insufficient data structures evidence.",
                List.of("Data Structures", "Java"));
    }

    private static void saveMatch(TaDatabase database,
                                  Application application,
                                  String scoreValue,
                                  int coveragePct,
                                  int missingRequired,
                                  int remainingHours,
                                  boolean recommend,
                                  String explanation,
                                  List<String> suggestions) {
        BigDecimal scoreValueDecimal = new BigDecimal(scoreValue);
        MatchScore score = new MatchScore();
        score.setApplicationId(application.getId());
        score.setRuleScore(scoreValueDecimal);
        score.setAiScore(scoreValueDecimal);
        score.setFinalScore(scoreValueDecimal);
        score.setSkillCoveragePct(coveragePct);
        score.setMissingRequiredCount(missingRequired);
        score.setWorkloadRemainingHours(remainingHours);
        score.setAiRecommend(recommend);
        score.setAiExplanation(explanation + " AI scoring unavailable; rule-based fallback applied.");
        score.setMissingSkillSuggestions(suggestions);
        score.setComputedAt(Instant.now().minusSeconds(60L * 60L));
        database.matchScores().save(score);
    }

    private static void seedWorkloads(TaDatabase database,
                                      Map<String, User> users,
                                      Map<String, Job> jobs,
                                      Map<String, Application> applications) {
        String semester = currentSemester();
        saveWorkload(database, users.get(DEFAULT_TA_EMAIL), jobs.get("CS101"), applications.get("app-test-cs101"),
                semester, 8, 4, WorkloadStatus.ACTIVE);
        saveWorkload(database, users.get("demo.ming@example.com"), jobs.get("CS204"), applications.get("app-ming-cs204"),
                semester, 24, 10, WorkloadStatus.ACTIVE);
        saveWorkload(database, users.get("demo.aisha@example.com"), jobs.get("CS305"), applications.get("app-aisha-cs305"),
                semester, 16, 6, WorkloadStatus.ACTIVE);
    }

    private static void saveWorkload(TaDatabase database,
                                     User ta,
                                     Job job,
                                     Application application,
                                     String semester,
                                     int assignedHours,
                                     int actualHours,
                                     WorkloadStatus status) {
        WorkloadRecord record = new WorkloadRecord();
        record.setTaId(ta.getId());
        record.setJobId(job.getId());
        record.setApplicationId(application.getId());
        record.setSemester(semester);
        record.setAssignedHours(assignedHours);
        record.setActualHours(actualHours);
        record.setStatus(status);
        database.workloadRecords().save(record);
    }

    private static void seedNotifications(TaDatabase database,
                                          Map<String, User> users,
                                          Map<String, Job> jobs,
                                          Map<String, Application> applications) {
        notify(database, users.get(DEFAULT_TA_EMAIL), NotificationType.APPLICATION_STATUS,
                "Application accepted", "Your CS101 application was accepted and workload was updated.",
                EntityType.APPLICATION, applications.get("app-test-cs101").getId(), false);
        notify(database, users.get("demo.ming@example.com"), NotificationType.WORKLOAD_ALERT,
                "Workload threshold exceeded", "You are currently above your preferred weekly workload.",
                EntityType.WORKLOAD_RECORD, applications.get("app-ming-cs204").getId(), false);
        notify(database, users.get("demo.aisha@example.com"), NotificationType.APPLICATION_STATUS,
                "Application accepted", "Your CS305 database lab application was accepted.",
                EntityType.APPLICATION, applications.get("app-aisha-cs305").getId(), true);
        notify(database, users.get("demo.priya@example.com"), NotificationType.NEW_JOB,
                "New studio coach vacancy", "CS250 Software Engineering Studio Coach is open for applications.",
                EntityType.JOB, jobs.get("CS250").getId(), false);
        notify(database, users.get("demo.li@example.com"), NotificationType.APPLICATION_STATUS,
                "Application under review", "Your CS250 application is now under review.",
                EntityType.APPLICATION, applications.get("app-li-cs250").getId(), false);
        notify(database, users.get("demo.oliver@example.com"), NotificationType.OFFER_RECEIVED,
                "Offer received", "You have an offer pending for MATH201 problem classes.",
                EntityType.APPLICATION, applications.get("app-oliver-math201").getId(), false);
        notify(database, users.get(DEFAULT_MO_EMAIL), NotificationType.NEW_APPLICANT,
                "New applicant", "A TA applied for CS101 Programming Fundamentals Lab Assistant.",
                EntityType.APPLICATION, applications.get("app-test-cs101").getId(), true);
        notify(database, users.get("demo.helen@example.com"), NotificationType.SYSTEM,
                "Match analysis ready", "Several CS applications have fresh rule-based match scores.",
                EntityType.MATCH_SCORE, applications.get("app-priya-cs401").getId(), false);
        notify(database, users.get(DEFAULT_ADMIN_EMAIL), NotificationType.SYSTEM,
                "Seed data initialized", "The demo database contains users, vacancies, applications, workload, messages, and audit logs.",
                EntityType.AUDIT_LOG, users.get(DEFAULT_ADMIN_EMAIL).getId(), false);

        message(database, users.get("demo.priya@example.com"), users.get("demo.helen@example.com"),
                "Hi Dr Carter, I can cover the Thursday CS250 studio if that helps.");
        message(database, users.get("demo.helen@example.com"), users.get("demo.priya@example.com"),
                "Thanks Priya. I will review your Git coaching experience before the shortlist meeting.");
        message(database, users.get(DEFAULT_TA_EMAIL), users.get(DEFAULT_MO_EMAIL),
                "I accepted the CS101 lab assignment and can start next week.");
        message(database, users.get(DEFAULT_MO_EMAIL), users.get(DEFAULT_TA_EMAIL),
                "Great, I have updated the rota and workload dashboard.");
    }

    private static void notify(TaDatabase database,
                               User user,
                               NotificationType type,
                               String title,
                               String message,
                               EntityType entityType,
                               UUID entityId,
                               boolean read) {
        Notification notification = new Notification();
        notification.setUserId(user.getId());
        notification.setNotifType(type);
        notification.setTitle(title);
        notification.setMessage(message);
        notification.setEntityType(entityType);
        notification.setEntityId(entityId);
        notification.setRead(read);
        database.notifications().save(notification);
    }

    private static void message(TaDatabase database, User sender, User recipient, String content) {
        Notification notification = new Notification();
        notification.setUserId(recipient.getId());
        notification.setEntityId(sender.getId());
        notification.setEntityType(EntityType.USER);
        notification.setNotifType(NotificationType.MESSAGE);
        notification.setTitle("Message");
        notification.setMessage(content);
        notification.setRead(false);
        database.notifications().save(notification);
    }

    private static void seedAuditLogs(TaDatabase database,
                                      Map<String, User> users,
                                      Map<String, Job> jobs,
                                      Map<String, Application> applications) {
        audit(database, users.get(DEFAULT_ADMIN_EMAIL), AuditAction.CREATE, EntityType.USER,
                users.get(DEFAULT_TA_EMAIL).getId(), "Seeded default TA account");
        audit(database, users.get(DEFAULT_MO_EMAIL), AuditAction.CREATE, EntityType.JOB,
                jobs.get("CS101").getId(), "Created CS101 vacancy for demo hiring flow");
        audit(database, users.get("demo.helen@example.com"), AuditAction.CREATE, EntityType.JOB,
                jobs.get("CS305").getId(), "Created database systems lab vacancy");
        audit(database, users.get("demo.helen@example.com"), AuditAction.STATUS_CHANGE, EntityType.APPLICATION,
                applications.get("app-aisha-cs305").getId(), "Accepted Aisha for CS305 labs");
        audit(database, users.get("demo.zhang@example.com"), AuditAction.STATUS_CHANGE, EntityType.APPLICATION,
                applications.get("app-oliver-math201").getId(), "Sent MATH201 offer to Oliver");
        audit(database, users.get(DEFAULT_ADMIN_EMAIL), AuditAction.UPDATE, EntityType.WORKLOAD_RECORD,
                applications.get("app-ming-cs204").getId(), "Seeded workload dashboard sample");
    }

    private static void audit(TaDatabase database,
                              User operator,
                              AuditAction action,
                              EntityType entityType,
                              UUID entityId,
                              String summary) {
        AuditLog log = new AuditLog();
        log.setOperatorId(operator.getId());
        log.setAction(action);
        log.setEntityType(entityType);
        log.setEntityId(entityId);
        ObjectNode node = MAPPER.createObjectNode();
        node.put("summary", summary);
        node.put("seeded", true);
        log.setNewValue(node);
        log.setOperatedAt(Instant.now().minusSeconds(30L * 60L));
        database.auditLogs().append(log);
    }

    private static AvailabilitySlot slot(DayOfWeek day, int startHour, int startMinute, int endHour, int endMinute) {
        AvailabilitySlot slot = new AvailabilitySlot();
        slot.setDayOfWeek(day);
        slot.setStartTime(LocalTime.of(startHour, startMinute));
        slot.setEndTime(LocalTime.of(endHour, endMinute));
        return slot;
    }

    private static SeedResumeSkill skill(String name, ProficiencyLevel proficiency, int yearsExp) {
        return new SeedResumeSkill(name, proficiency, yearsExp);
    }

    private static SeedRequirement requirement(String name, boolean required, ProficiencyLevel proficiency) {
        return new SeedRequirement(name, required, proficiency);
    }

    private static String currentSemester() {
        LocalDate today = LocalDate.now();
        return (today.getMonthValue() >= 8 ? "Fall " : "Spring ") + today.getYear();
    }

    private record SeedUser(String email,
                            String fullName,
                            UserRole role,
                            String department,
                            String studentId,
                            String phone,
                            String bio,
                            String language,
                            String appearance) {
    }

    private record SeedSkill(String name, SkillCategory category, String description) {
    }

    private record SeedResumeSkill(String name, ProficiencyLevel proficiency, int yearsExp) {
    }

    private record SeedRequirement(String name, boolean required, ProficiencyLevel proficiency) {
    }
}
