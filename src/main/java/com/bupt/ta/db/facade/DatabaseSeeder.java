package com.bupt.ta.db.facade;

import com.bupt.ta.domain.entity.Skill;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.SkillCategory;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.util.PasswordUtil;

import java.util.List;

public final class DatabaseSeeder {
    public static final String DEFAULT_PASSWORD = "password";
    public static final String DEFAULT_TA_EMAIL = "test@example.com";
    public static final String DEFAULT_MO_EMAIL = "mo@example.com";
    public static final String DEFAULT_ADMIN_EMAIL = "admin@example.com";

    private static final List<SeedUser> DEFAULT_USERS = List.of(
            new SeedUser(DEFAULT_TA_EMAIL, "Test User", UserRole.TA, "Computer Science", "TA1001"),
            new SeedUser(DEFAULT_MO_EMAIL, "Module Organiser", UserRole.MO, "Teaching Support", null),
            new SeedUser(DEFAULT_ADMIN_EMAIL, "System Admin", UserRole.ADMIN, "Platform Operations", null)
    );

    private static final List<SeedSkill> DEFAULT_SKILLS = List.of(
            new SeedSkill("Java", SkillCategory.PROGRAMMING, "Object-oriented programming and coursework support."),
            new SeedSkill("Python", SkillCategory.PROGRAMMING, "Scripting, automation, and data-oriented coding support."),
            new SeedSkill("SQL", SkillCategory.DOMAIN, "Database querying, schema reasoning, and lab assistance."),
            new SeedSkill("Tutoring", SkillCategory.TEACHING, "Leading tutorials, Q&A sessions, and office hours."),
            new SeedSkill("English", SkillCategory.LANGUAGE, "English communication for teaching and student support."),
            new SeedSkill("Communication", SkillCategory.SOFT, "Clear written and verbal collaboration with students and staff.")
    );

    private DatabaseSeeder() {
    }

    public static void seedIfNeeded(TaDatabase database) {
        database.executeAtomically(() -> {
            DEFAULT_USERS.forEach(seed -> ensureUser(database, seed));
            DEFAULT_SKILLS.forEach(seed -> ensureSkill(database, seed));
        });
    }

    private static void ensureUser(TaDatabase database, SeedUser seed) {
        if (database.users().findByEmail(seed.email()).isPresent()) {
            return;
        }

        User user = new User();
        user.setEmail(seed.email());
        user.setPasswordHash(PasswordUtil.hashPassword(DEFAULT_PASSWORD));
        user.setRole(seed.role());
        user.setFullName(seed.fullName());
        user.setDepartment(seed.department());
        user.setStudentId(seed.studentId());
        user.setActive(true);
        database.users().save(user);
    }

    private static void ensureSkill(TaDatabase database, SeedSkill seed) {
        if (database.skills().findByNameIgnoreCase(seed.name()).isPresent()) {
            return;
        }

        Skill skill = new Skill();
        skill.setName(seed.name());
        skill.setCategory(seed.category());
        skill.setDescription(seed.description());
        database.skills().save(skill);
    }

    private record SeedUser(String email, String fullName, UserRole role, String department, String studentId) {
    }

    private record SeedSkill(String name, SkillCategory category, String description) {
    }
}
