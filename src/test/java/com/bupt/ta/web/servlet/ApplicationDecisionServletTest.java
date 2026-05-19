package com.bupt.ta.web.servlet;

import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.Application;
import com.bupt.ta.domain.entity.Job;
import com.bupt.ta.domain.entity.Resume;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.ApplicationStatus;
import com.bupt.ta.domain.enums.UserRole;
import com.bupt.ta.support.ServletHarness;
import com.bupt.ta.support.TestData;
import com.bupt.ta.support.TestDatabases;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class ApplicationDecisionServletTest {
    @TempDir
    Path tempDir;

    @Test
    void unauthenticatedRequestsShouldRedirectToLogin() throws Exception {
        TaDatabase db = TestDatabases.open(tempDir);
        ApplicationDecisionServlet servlet = init(new ApplicationDecisionServlet(), db);
        ServletHarness harness = new ServletHarness();

        servlet.doPost(harness.request(), harness.response());

        assertTrue(harness.redirectLocation().startsWith("/login?errorMessage="));
    }

    @Test
    void missingOrInvalidParametersShouldRedirectBackToApplications() throws Exception {
        TaDatabase db = TestDatabases.open(tempDir);
        User mo = db.users().save(TestData.user(TestData.uniqueEmail("decision-mo"), UserRole.MO, "Decision MO"));
        ApplicationDecisionServlet servlet = init(new ApplicationDecisionServlet(), db);

        ServletHarness missing = new ServletHarness().currentUser(mo);
        servlet.doPost(missing.request(), missing.response());
        assertTrue(missing.redirectLocation().startsWith("/applications?errorMessage="));

        ServletHarness invalid = new ServletHarness()
                .currentUser(mo)
                .parameter("applicationId", "not-a-uuid")
                .parameter("action", "review");
        servlet.doPost(invalid.request(), invalid.response());
        assertTrue(invalid.redirectLocation().startsWith("/applications?errorMessage="));
    }

    @Test
    void moduleOwnerCanStartReviewAndReturnToDetail() throws Exception {
        TaDatabase db = TestDatabases.open(tempDir);
        Fixture fixture = fixture(db);
        ApplicationDecisionServlet servlet = init(new ApplicationDecisionServlet(), db);
        ServletHarness harness = new ServletHarness()
                .currentUser(fixture.mo())
                .parameter("applicationId", fixture.application().getId().toString())
                .parameter("action", "review")
                .parameter("returnTo", "detail");

        servlet.doPost(harness.request(), harness.response());

        assertEquals(ApplicationStatus.REVIEWING,
                db.applications().findById(fixture.application().getId()).orElseThrow().getStatus());
        assertTrue(harness.redirectLocation()
                .startsWith("/application/detail?applicationId=" + fixture.application().getId() + "&successMessage="));
    }

    @Test
    void wrongRoleShouldRedirectWithErrorWithoutChangingStatus() throws Exception {
        TaDatabase db = TestDatabases.open(tempDir);
        Fixture fixture = fixture(db);
        ApplicationDecisionServlet servlet = init(new ApplicationDecisionServlet(), db);
        ServletHarness harness = new ServletHarness()
                .currentUser(fixture.ta())
                .parameter("applicationId", fixture.application().getId().toString())
                .parameter("action", "offer");

        servlet.doPost(harness.request(), harness.response());

        assertEquals(ApplicationStatus.PENDING,
                db.applications().findById(fixture.application().getId()).orElseThrow().getStatus());
        assertTrue(harness.redirectLocation().startsWith("/applications?errorMessage="));
    }

    private ApplicationDecisionServlet init(ApplicationDecisionServlet servlet, TaDatabase db) throws Exception {
        ServletHarness initHarness = new ServletHarness().bindDatabase(db);
        servlet.init(initHarness.servletConfig());
        return servlet;
    }

    private Fixture fixture(TaDatabase db) {
        User ta = db.users().save(TestData.user(TestData.uniqueEmail("decision-ta"), UserRole.TA, "Decision TA"));
        User mo = db.users().save(TestData.user(TestData.uniqueEmail("decision-mo"), UserRole.MO, "Decision MO"));
        Resume resume = db.resumes().save(TestData.resume(ta.getId(), "Decision Resume"));
        Job job = db.jobs().save(TestData.openJob(mo.getId(), "Decision Job"));
        Application application = db.applications().save(TestData.application(resume.getId(), job.getId(), ApplicationStatus.PENDING));
        return new Fixture(ta, mo, application);
    }

    private record Fixture(User ta, User mo, Application application) {
    }
}
