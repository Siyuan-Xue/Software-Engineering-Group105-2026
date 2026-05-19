package com.bupt.ta.web.filter;

import com.bupt.ta.web.security.CsrfTokens;
import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;

/**
 * Adds a session-bound CSRF token to dynamic pages and rejects unsafe requests
 * that do not echo it in either a form parameter or request header.
 */
@WebFilter("/*")
public class CsrfFilter implements Filter {

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;
        HttpServletResponse resp = (HttpServletResponse) response;

        if (isStaticAsset(req)) {
            chain.doFilter(request, response);
            return;
        }

        if (CsrfTokens.isUnsafeMethod(req.getMethod()) && !CsrfTokens.matches(req)) {
            resp.setStatus(HttpServletResponse.SC_FORBIDDEN);
            resp.setContentType("application/json;charset=UTF-8");
            resp.getWriter().write("{\"ok\":false,\"error\":\"Invalid CSRF token.\"}");
            return;
        }

        CsrfTokens.ensureToken(req);
        chain.doFilter(request, response);
    }

    private static boolean isStaticAsset(HttpServletRequest req) {
        String route = req.getRequestURI().substring(req.getContextPath().length());
        return route.startsWith("/css/")
                || route.startsWith("/js/")
                || route.startsWith("/images/")
                || route.startsWith("/assets/");
    }
}
