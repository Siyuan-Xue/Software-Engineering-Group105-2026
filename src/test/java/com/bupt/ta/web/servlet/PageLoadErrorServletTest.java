package com.bupt.ta.web.servlet;

import com.bupt.ta.db.core.DatabaseException;
import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.db.repository.ApplicationRepository;
import com.bupt.ta.db.repository.NotificationRepository;
import com.bupt.ta.db.repository.ResumeRepository;
import com.bupt.ta.db.repository.UserRepository;
import com.bupt.ta.domain.entity.User;
import com.bupt.ta.domain.enums.UserRole;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class PageLoadErrorServletTest {

    private final Map<String, Object> attributes = new HashMap<>();
    private final Map<String, Object> contextAttributes = new HashMap<>();
    private User currentUser;
    private ServletContext servletContext;
    private HttpServletRequest request;
    private HttpServletResponse response;
    private String forwardedPath;

    @BeforeEach
    void setUp() {
        attributes.clear();
        contextAttributes.clear();
        forwardedPath = null;

        currentUser = new User();
        currentUser.setId(UUID.randomUUID());
        currentUser.setRole(UserRole.TA);
        currentUser.setFullName("Test TA");

        servletContext = servletProxy();
        request = requestProxy();
        response = responseProxy();
    }

    @Test
    void dashboardShouldSetLoadErrorWhenDatabaseFails() throws Exception {
        attributes.put("userRole", "TA");
        DashboardServlet servlet = initServlet(new DashboardServlet(), failingDatabase(repositoryMethod("resumes", "listByUserId")));
        servlet.doGet(request, response);

        assertEquals("loadError", attributes.get("pageState"));
        assertEquals("/portal/dashboard.jsp", forwardedPath);
    }

    @Test
    void applicationsShouldSetLoadErrorWhenDatabaseFails() throws Exception {
        attributes.put("userRole", "TA");
        ApplicationsServlet servlet = initServlet(new ApplicationsServlet(), failingDatabase(repositoryMethod("resumes", "listByUserId")));
        servlet.doGet(request, response);

        assertEquals("loadError", attributes.get("pageState"));
        assertEquals("/portal/applications.jsp", forwardedPath);
    }

    @Test
    void dashboardShouldUseEmptyActivitiesForUnsupportedRole() throws Exception {
        attributes.put("userRole", "DEMO");
        DashboardServlet servlet = initServlet(new DashboardServlet(), emptyDatabase());
        servlet.doGet(request, response);

        assertEquals("emptyActivities", attributes.get("pageState"));
        assertEquals("/portal/dashboard.jsp", forwardedPath);
    }

    @Test
    void applicationsLoadErrorShouldClearFlashSuccessMessage() throws Exception {
        attributes.put("userRole", "TA");
        ApplicationsServlet servlet = initServlet(new ApplicationsServlet(), failingDatabase(repositoryMethod("resumes", "listByUserId")));
        whenRequestHasQueryParam("successMessage", "Saved");
        servlet.doGet(request, response);

        assertEquals("loadError", attributes.get("pageState"));
        assertEquals(null, attributes.get("successMessage"));
    }

    @Test
    void resumesShouldSetLoadErrorWhenDatabaseFails() throws Exception {
        ResumesServlet servlet = initServlet(new ResumesServlet(), failingDatabase(repositoryMethod("resumes", "listByUserId")));
        servlet.doGet(request, response);

        assertEquals("loadError", attributes.get("pageState"));
        assertEquals("/portal/resumes.jsp", forwardedPath);
    }

    @Test
    void vacanciesShouldSetLoadErrorWhenDatabaseFails() throws Exception {
        VacanciesServlet servlet = initServlet(new VacanciesServlet(), failingDatabase(repositoryMethod("users", "findAll")));
        servlet.doGet(request, response);

        assertEquals("loadError", attributes.get("pageState"));
        assertEquals("/portal/vacancies.jsp", forwardedPath);
    }

    @Test
    void messagesShouldSetLoadErrorWhenDatabaseFails() throws Exception {
        MessagesServlet servlet = initServlet(new MessagesServlet(), failingDatabase(repositoryMethod("notifications", "findAll")));
        servlet.doGet(request, response);

        assertEquals("loadError", attributes.get("pageState"));
        assertEquals("/portal/messages.jsp", forwardedPath);
    }

    @Test
    void workloadsShouldSetLoadErrorWhenDatabaseFails() throws Exception {
        currentUser.setRole(UserRole.ADMIN);
        WorkloadsServlet servlet = initServlet(new WorkloadsServlet(), failingDatabase(repositoryMethod("applications", "findAll")));
        servlet.doGet(request, response);

        assertEquals("loadError", attributes.get("pageState"));
        assertEquals("/portal/workloads.jsp", forwardedPath);
    }

    private static FailingCall repositoryMethod(String repoName, String methodName) {
        return db -> {
            throw new DatabaseException("Simulated failure in " + repoName + "." + methodName);
        };
    }

    @SuppressWarnings("unchecked")
    private <T extends HttpServlet> T initServlet(T servlet, TaDatabase database) throws Exception {
        DatabaseProvider.bind(servletContext, database);
        servlet.init(servletConfigProxy());
        return servlet;
    }

    private TaDatabase emptyDatabase() {
        return (TaDatabase) java.lang.reflect.Proxy.newProxyInstance(
                TaDatabase.class.getClassLoader(),
                new Class[]{TaDatabase.class},
                (proxy, method, args) -> defaultValue(method.getReturnType()));
    }

    private void whenRequestHasQueryParam(String name, String value) {
        request = (HttpServletRequest) java.lang.reflect.Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{HttpServletRequest.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getSession" -> sessionProxy();
                    case "getAttribute" -> attributes.get((String) args[0]);
                    case "setAttribute" -> {
                        attributes.put((String) args[0], args[1]);
                        yield null;
                    }
                    case "getServletContext" -> servletContext;
                    case "getContextPath" -> "";
                    case "getRequestDispatcher" -> dispatcherProxy((String) args[0]);
                    case "getParameter" -> name.equals(args[0]) ? value : null;
                    case "getMethod" -> "GET";
                    default -> null;
                });
    }

    private TaDatabase failingDatabase(FailingCall failingCall) {
        return (TaDatabase) java.lang.reflect.Proxy.newProxyInstance(
                TaDatabase.class.getClassLoader(),
                new Class[]{TaDatabase.class},
                (proxy, method, args) -> {
                    if ("resumes".equals(method.getName())) {
                        return throwingRepository(ResumeRepository.class, "listByUserId", failingCall, proxy);
                    }
                    if ("applications".equals(method.getName())) {
                        return throwingRepository(ApplicationRepository.class, "findAll", failingCall, proxy);
                    }
                    if ("users".equals(method.getName())) {
                        return throwingRepository(UserRepository.class, "findAll", failingCall, proxy);
                    }
                    if ("notifications".equals(method.getName())) {
                        return throwingRepository(NotificationRepository.class, "findAll", failingCall, proxy);
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    private Object throwingRepository(
            Class<?> repoType,
            String failMethod,
            FailingCall failingCall,
            Object dbProxy) {
        return java.lang.reflect.Proxy.newProxyInstance(
                repoType.getClassLoader(),
                new Class[]{repoType},
                (p, m, a) -> {
                    if (failMethod.equals(m.getName())) {
                        failingCall.run((TaDatabase) dbProxy);
                    }
                    return defaultValue(m.getReturnType());
                });
    }

    @FunctionalInterface
    private interface FailingCall {
        void run(TaDatabase db);
    }

    private static Object defaultValue(Class<?> type) {
        if (type == boolean.class) {
            return false;
        }
        if (type == int.class) {
            return 0;
        }
        if (type == long.class) {
            return 0L;
        }
        if (type.isPrimitive()) {
            return 0;
        }
        return null;
    }

    private ServletContext servletProxy() {
        return (ServletContext) java.lang.reflect.Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{ServletContext.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getAttribute" -> contextAttributes.get((String) args[0]);
                    case "setAttribute" -> {
                        contextAttributes.put((String) args[0], args[1]);
                        yield null;
                    }
                    case "log" -> null;
                    default -> null;
                });
    }

    private HttpServletRequest requestProxy() {
        return (HttpServletRequest) java.lang.reflect.Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{HttpServletRequest.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getSession" -> sessionProxy();
                    case "getAttribute" -> attributes.get((String) args[0]);
                    case "setAttribute" -> {
                        attributes.put((String) args[0], args[1]);
                        yield null;
                    }
                    case "getServletContext" -> servletContext;
                    case "getContextPath" -> "";
                    case "getRequestDispatcher" -> dispatcherProxy((String) args[0]);
                    case "getParameter" -> null;
                    case "getMethod" -> "GET";
                    default -> null;
                });
    }

    private HttpSession sessionProxy() {
        return (HttpSession) java.lang.reflect.Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{HttpSession.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getAttribute" -> "currentUser".equals(args[0]) ? currentUser : null;
                    default -> null;
                });
    }

    private HttpServletResponse responseProxy() {
        return (HttpServletResponse) java.lang.reflect.Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{HttpServletResponse.class},
                (proxy, method, args) -> null);
    }

    private RequestDispatcher dispatcherProxy(String path) {
        return (RequestDispatcher) java.lang.reflect.Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{RequestDispatcher.class},
                (proxy, method, args) -> {
                    if ("forward".equals(method.getName())) {
                        forwardedPath = path;
                    }
                    return null;
                });
    }

    private ServletConfig servletConfigProxy() {
        return (ServletConfig) java.lang.reflect.Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{ServletConfig.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getServletContext" -> servletContext;
                    default -> null;
                });
    }
}
