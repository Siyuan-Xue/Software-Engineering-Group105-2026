package com.bupt.ta.web.filter;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;

import java.io.IOException;

/**
 * Sets servlet request and response character encodings to UTF-8 for each mapped dispatcher path.
 *
 * <p>Does not mutate {@link jakarta.servlet.http.HttpServletResponse#setContentType(String)}; JSP controllers remain responsible
 * for declaring concrete MIME types alongside implicit charset propagation.</p>
 */
@WebFilter(filterName = "encodingFilter", urlPatterns = "/*")
public class EncodingFilter implements Filter {
    private static final String DEFAULT_ENCODING = "UTF-8";

    /**
     * @param request  mutable low-level servlet request gaining {@link ServletRequest#setCharacterEncoding(String)}
     * @param response mutable servlet response inheriting UTF-8 output semantics
     * @param chain    remaining pipeline members after encoding normalisation
     */
    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {
        request.setCharacterEncoding(DEFAULT_ENCODING);
        response.setCharacterEncoding(DEFAULT_ENCODING);
        chain.doFilter(request, response);
    }
}
