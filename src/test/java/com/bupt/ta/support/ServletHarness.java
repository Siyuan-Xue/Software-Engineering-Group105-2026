package com.bupt.ta.support;

import com.bupt.ta.db.facade.DatabaseProvider;
import com.bupt.ta.db.facade.TaDatabase;
import com.bupt.ta.domain.entity.User;
import jakarta.servlet.FilterChain;
import jakarta.servlet.RequestDispatcher;
import jakarta.servlet.ServletConfig;
import jakarta.servlet.ServletContext;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Proxy;
import java.util.HashMap;
import java.util.Map;

public final class ServletHarness {
    private final Map<String, Object> contextAttributes = new HashMap<>();
    private final Map<String, Object> requestAttributes = new HashMap<>();
    private final Map<String, Object> sessionAttributes = new HashMap<>();
    private final Map<String, String> parameters = new HashMap<>();
    private final Map<String, String[]> parameterValues = new HashMap<>();
    private final Map<String, String> headers = new HashMap<>();
    private final ServletContext servletContext = servletContextProxy();
    private final ServletConfig servletConfig = servletConfigProxy();
    private final HttpServletRequest request = requestProxy();
    private final HttpServletResponse response = responseProxy();
    private final HttpSession session = sessionProxy();
    private final FilterChain chain = chainProxy();
    private final StringWriter responseBody = new StringWriter();
    private final PrintWriter responseWriter = new PrintWriter(responseBody, true);

    private String contextPath = "";
    private String requestUri = "/";
    private String queryString;
    private String method = "GET";
    private String redirectLocation;
    private String forwardedPath;
    private String contentType;
    private String characterEncoding;
    private int status = 200;
    private String errorMessage;
    private int chainCalls;
    private boolean hasSession;

    public ServletContext servletContext() {
        return servletContext;
    }

    public ServletConfig servletConfig() {
        return servletConfig;
    }

    public HttpServletRequest request() {
        return request;
    }

    public HttpServletResponse response() {
        return response;
    }

    public HttpSession session() {
        hasSession = true;
        return session;
    }

    public FilterChain chain() {
        return chain;
    }

    public ServletHarness bindDatabase(TaDatabase database) {
        DatabaseProvider.bind(servletContext, database);
        return this;
    }

    public ServletHarness currentUser(User user) {
        session().setAttribute("currentUser", user);
        return this;
    }

    public ServletHarness method(String method) {
        this.method = method;
        return this;
    }

    public ServletHarness route(String route) {
        this.requestUri = contextPath + route;
        return this;
    }

    public ServletHarness contextPath(String contextPath) {
        this.contextPath = contextPath;
        if (!requestUri.startsWith(contextPath)) {
            this.requestUri = contextPath + requestUri;
        }
        return this;
    }

    public ServletHarness queryString(String queryString) {
        this.queryString = queryString;
        return this;
    }

    public ServletHarness parameter(String name, String value) {
        parameters.put(name, value);
        parameterValues.put(name, new String[]{value});
        return this;
    }

    public ServletHarness parameterValues(String name, String... values) {
        parameterValues.put(name, values);
        if (values != null && values.length > 0) {
            parameters.put(name, values[0]);
        }
        return this;
    }

    public ServletHarness header(String name, String value) {
        headers.put(name, value);
        return this;
    }

    public ServletHarness requestAttribute(String name, Object value) {
        requestAttributes.put(name, value);
        return this;
    }

    public Object requestAttribute(String name) {
        return requestAttributes.get(name);
    }

    public Object sessionAttribute(String name) {
        return sessionAttributes.get(name);
    }

    public String redirectLocation() {
        return redirectLocation;
    }

    public String forwardedPath() {
        return forwardedPath;
    }

    public int status() {
        return status;
    }

    public String errorMessage() {
        return errorMessage;
    }

    public String contentType() {
        return contentType;
    }

    public String characterEncoding() {
        return characterEncoding;
    }

    public String responseBody() {
        responseWriter.flush();
        return responseBody.toString();
    }

    public int chainCalls() {
        return chainCalls;
    }

    private ServletContext servletContextProxy() {
        return (ServletContext) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{ServletContext.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getAttribute" -> contextAttributes.get((String) args[0]);
                    case "setAttribute" -> {
                        contextAttributes.put((String) args[0], args[1]);
                        yield null;
                    }
                    case "removeAttribute" -> {
                        contextAttributes.remove((String) args[0]);
                        yield null;
                    }
                    case "getContextPath" -> contextPath;
                    case "log" -> null;
                    default -> defaultValue(method.getReturnType());
                });
    }

    private ServletConfig servletConfigProxy() {
        return (ServletConfig) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{ServletConfig.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getServletContext" -> servletContext;
                    case "getServletName" -> "test-servlet";
                    case "getInitParameter" -> null;
                    default -> defaultValue(method.getReturnType());
                });
    }

    private HttpServletRequest requestProxy() {
        return (HttpServletRequest) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{HttpServletRequest.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getSession" -> getSession(args);
                    case "changeSessionId" -> "changed-session";
                    case "getAttribute" -> requestAttributes.get((String) args[0]);
                    case "setAttribute" -> {
                        requestAttributes.put((String) args[0], args[1]);
                        yield null;
                    }
                    case "removeAttribute" -> {
                        requestAttributes.remove((String) args[0]);
                        yield null;
                    }
                    case "getServletContext" -> servletContext;
                    case "getContextPath" -> contextPath;
                    case "getRequestURI" -> requestUri;
                    case "getQueryString" -> queryString;
                    case "getMethod" -> ServletHarness.this.method;
                    case "getHeader" -> headers.get((String) args[0]);
                    case "getParameter" -> parameters.get((String) args[0]);
                    case "getParameterValues" -> parameterValues.get((String) args[0]);
                    case "getRequestDispatcher" -> dispatcherProxy((String) args[0]);
                    default -> defaultValue(method.getReturnType());
                });
    }

    private Object getSession(Object[] args) {
        if (args == null || args.length == 0) {
            hasSession = true;
            return session;
        }
        boolean create = Boolean.TRUE.equals(args[0]);
        if (hasSession || create) {
            hasSession = true;
            return session;
        }
        return null;
    }

    private HttpServletResponse responseProxy() {
        return (HttpServletResponse) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{HttpServletResponse.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "sendRedirect" -> {
                        redirectLocation = (String) args[0];
                        yield null;
                    }
                    case "sendError" -> {
                        status = (int) args[0];
                        errorMessage = args.length > 1 ? (String) args[1] : null;
                        yield null;
                    }
                    case "setStatus" -> {
                        status = (int) args[0];
                        yield null;
                    }
                    case "setContentType" -> {
                        contentType = (String) args[0];
                        yield null;
                    }
                    case "getContentType" -> contentType;
                    case "setCharacterEncoding" -> {
                        characterEncoding = (String) args[0];
                        yield null;
                    }
                    case "getCharacterEncoding" -> characterEncoding;
                    case "getWriter" -> responseWriter;
                    default -> defaultValue(method.getReturnType());
                });
    }

    private HttpSession sessionProxy() {
        return (HttpSession) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{HttpSession.class},
                (proxy, method, args) -> switch (method.getName()) {
                    case "getAttribute" -> sessionAttributes.get((String) args[0]);
                    case "setAttribute" -> {
                        sessionAttributes.put((String) args[0], args[1]);
                        yield null;
                    }
                    case "removeAttribute" -> {
                        sessionAttributes.remove((String) args[0]);
                        yield null;
                    }
                    case "invalidate" -> {
                        sessionAttributes.clear();
                        hasSession = false;
                        yield null;
                    }
                    case "getId" -> "test-session";
                    default -> defaultValue(method.getReturnType());
                });
    }

    private RequestDispatcher dispatcherProxy(String path) {
        return (RequestDispatcher) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{RequestDispatcher.class},
                (proxy, method, args) -> {
                    if ("forward".equals(method.getName())) {
                        forwardedPath = path;
                    }
                    return defaultValue(method.getReturnType());
                });
    }

    private FilterChain chainProxy() {
        return (FilterChain) Proxy.newProxyInstance(
                getClass().getClassLoader(),
                new Class[]{FilterChain.class},
                (proxy, method, args) -> {
                    if ("doFilter".equals(method.getName())) {
                        chainCalls++;
                    }
                    return defaultValue(method.getReturnType());
                });
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
        if (type == void.class || !type.isPrimitive()) {
            return null;
        }
        return 0;
    }
}
