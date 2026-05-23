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
 * Global CSRF hardening layered on all URL mappings alongside {@link AuthFilter}.
 *
 * <p>Static asset prefixes short-circuit to avoid needless token issuance. Stateless {@code GET} traversals lazily populate
 * request attributes ({@link CsrfTokens#ensureToken(HttpServletRequest)}) expected by forms; mutating verbs without a matching token
 * receive HTTP {@code 403} with a terse JSON envelope suitable for AJAX clients.</p>
 *
 * @see CsrfTokens
 */
@WebFilter("/*")
public class CsrfFilter implements Filter {

    /**
     * @param request  HTTP request inspected for verbs and echoed tokens
     * @param response JSON error channel when verification fails prior to servlet execution
     * @param chain    downstream filters and target servlet mappings
     */
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

    /** @return {@code true} when the route targets theme or script bundles exempt from verification */
    private static boolean isStaticAsset(HttpServletRequest req) {
        String route = req.getRequestURI().substring(req.getContextPath().length());
        return route.startsWith("/css/")
                || route.startsWith("/js/")
                || route.startsWith("/images/")
                || route.startsWith("/assets/");
    }
}
