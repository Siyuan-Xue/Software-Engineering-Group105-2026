package com.bupt.ta.web.servlet;

import com.bupt.ta.config.AppConfig;
import com.bupt.ta.config.DatabaseConfig;
import com.bupt.ta.model.Job;
import com.bupt.ta.model.Resume;
import com.bupt.ta.model.User;
import com.bupt.ta.model.enums.DegreeLevel;
import com.bupt.ta.model.enums.JobStatus;
import com.bupt.ta.model.enums.JobType;
import com.bupt.ta.model.enums.UserRole;
import com.bupt.ta.persistence.TaDatabase;
import com.bupt.ta.service.DbDemoService;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.io.IOException;
import java.lang.reflect.Proxy;
import java.math.BigDecimal;
import java.nio.file.Path;
import java.time.Instant;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class DbDemoServletIntegrationTest {

    @TempDir
    Path tempDir;

    @Test
    void doGetShouldLoadListsAndEnumsForView() throws Exception {
        DbDemoService service = createService();
        User mo = service.saveUser(user("mo@example.com", UserRole.MO, "MO User"));
        User ta = service.saveUser(user("ta@example.com", UserRole.TA, "TA User"));
        Resume resume = service.saveResume(resume(ta.getId()));
        Job job = service.saveJob(job(mo.getId()));

        TestExchange exchange = new TestExchange(Map.of(
                "editEntity", "job",
                "editId", job.getId().toString()
        ));

        new TestableDbDemoServlet(service).handleGet(exchange.request, exchange.response);

        assertEquals("/WEB-INF/jsp/db-demo.jsp", exchange.forwardedPath);
        assertEquals(2, ((java.util.List<?>) exchange.attributes.get("users")).size());
        assertEquals(1, ((java.util.List<?>) exchange.attributes.get("resumes")).size());
        assertEquals(1, ((java.util.List<?>) exchange.attributes.get("jobs")).size());
        assertNotNull(exchange.attributes.get("roles"));
        assertNotNull(exchange.attributes.get("editingJob"));
        assertEquals(resume.getTitle(), ((Map<?, ?>) exchange.attributes.get("resumeLabelsById")).get(resume.getId()));
    }

    @Test
    void doPostShouldRedirectAfterSuccessfulCreate() throws Exception {
        DbDemoService service = createService();
        TestExchange exchange = new TestExchange(Map.of(
                "entity", "user",
                "operation", "create",
                "email", "created@example.com",
                "passwordHash", "hash",
                "fullName", "Created User",
                "role", "TA"
        ));

        new TestableDbDemoServlet(service).handlePost(exchange.request, exchange.response);

        assertTrue(exchange.redirectedUrl.startsWith("/ta105/db-demo?successMessage="));
        assertTrue(service.listUsers().stream().anyMatch(user -> "created@example.com".equals(user.getEmail())));
    }

    @Test
    void doPostShouldForwardWithErrorWhenValidationFails() throws Exception {
        DbDemoService service = createService();
        TestExchange exchange = new TestExchange(Map.of(
                "entity", "resume",
                "operation", "create",
                "userId", "00000000-0000-0000-0000-000000000001",
                "title", "Broken Resume",
                "degreeLevel", "MASTER",
                "maxWeeklyHours", "10"
        ));

        new TestableDbDemoServlet(service).handlePost(exchange.request, exchange.response);

        assertEquals("/WEB-INF/jsp/db-demo.jsp", exchange.forwardedPath);
        assertTrue(((String) exchange.attributes.get("errorMessage")).startsWith("Resume owner does not exist"));
        assertEquals("resume", exchange.attributes.get("editEntity"));
    }

    @Test
    void doPostShouldReactivateInactiveUser() throws Exception {
        DbDemoService service = createService();
        User user = service.saveUser(user("ta@example.com", UserRole.TA, "TA User"));
        service.deactivateUser(user.getId());

        TestExchange exchange = new TestExchange(Map.of(
                "entity", "user",
                "operation", "activate",
                "id", user.getId().toString()
        ));

        new TestableDbDemoServlet(service).handlePost(exchange.request, exchange.response);

        assertTrue(exchange.redirectedUrl.startsWith("/ta105/db-demo?successMessage="));
        assertTrue(service.findUser(user.getId()).orElseThrow().isActive());
    }

    private DbDemoService createService() {
        TaDatabase database = TaDatabase.open(DatabaseConfig.of(tempDir, AppConfig.createObjectMapper()));
        return DbDemoService.from(database);
    }

    private User user(String email, UserRole role, String fullName) {
        User user = new User();
        user.setEmail(email);
        user.setPasswordHash("hash");
        user.setRole(role);
        user.setFullName(fullName);
        return user;
    }

    private Resume resume(java.util.UUID userId) {
        Resume resume = new Resume();
        resume.setUserId(userId);
        resume.setTitle("Resume");
        resume.setDegreeLevel(DegreeLevel.MASTER);
        resume.setGpa(BigDecimal.valueOf(3.8));
        resume.setMaxWeeklyHours(12);
        return resume;
    }

    private Job job(java.util.UUID posterId) {
        Job job = new Job();
        job.setPostedBy(posterId);
        job.setTitle("Job");
        job.setType(JobType.MODULE_SUPPORT);
        job.setStatus(JobStatus.OPEN);
        job.setRequiredHours(8);
        job.setSlots(1);
        job.setDeadline(Instant.now().plusSeconds(3600));
        return job;
    }

    static class TestableDbDemoServlet extends DbDemoServlet {
        TestableDbDemoServlet(DbDemoService dbDemoService) {
            super(dbDemoService);
        }

        void handleGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            doGet(request, response);
        }

        void handlePost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
            doPost(request, response);
        }
    }

    static class TestExchange {
        final Map<String, String> parameters;
        final Map<String, Object> attributes = new HashMap<>();
        final HttpServletRequest request;
        final HttpServletResponse response;
        String forwardedPath;
        String redirectedUrl;

        TestExchange(Map<String, String> parameters) {
            this.parameters = parameters;
            this.request = (HttpServletRequest) Proxy.newProxyInstance(
                    getClass().getClassLoader(),
                    new Class[]{HttpServletRequest.class},
                    (proxy, method, args) -> switch (method.getName()) {
                        case "getParameter" -> this.parameters.get((String) args[0]);
                        case "setAttribute" -> {
                            attributes.put((String) args[0], args[1]);
                            yield null;
                        }
                        case "getAttribute" -> attributes.get((String) args[0]);
                        case "removeAttribute" -> {
                            attributes.remove((String) args[0]);
                            yield null;
                        }
                        case "getContextPath" -> "/ta105";
                        case "getRequestDispatcher" -> createDispatcher((String) args[0]);
                        default -> null;
                    }
            );
            this.response = (HttpServletResponse) Proxy.newProxyInstance(
                    getClass().getClassLoader(),
                    new Class[]{HttpServletResponse.class},
                    (proxy, method, args) -> {
                        if ("sendRedirect".equals(method.getName())) {
                            redirectedUrl = (String) args[0];
                        }
                        return null;
                    }
            );
        }

        private RequestDispatcher createDispatcher(String path) {
            return (RequestDispatcher) Proxy.newProxyInstance(
                    getClass().getClassLoader(),
                    new Class[]{RequestDispatcher.class},
                    (proxy, method, args) -> {
                        if ("forward".equals(method.getName())) {
                            forwardedPath = path;
                        }
                        return null;
                    }
            );
        }
    }
}
