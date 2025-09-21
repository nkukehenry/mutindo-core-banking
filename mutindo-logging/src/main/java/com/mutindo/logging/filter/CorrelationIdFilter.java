package com.mutindo.logging.filter;

import jakarta.servlet.*;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.UUID;

/**
 * Filter to add correlation ID to all requests for distributed tracing
 */
@Slf4j
@Component
public class CorrelationIdFilter implements Filter {

    private static final String CORRELATION_ID_HEADER = "X-Correlation-ID";
    private static final String CORRELATION_ID_MDC_KEY = "correlationId";

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain)
            throws IOException, ServletException {

        HttpServletRequest httpRequest = (HttpServletRequest) request;
        HttpServletResponse httpResponse = (HttpServletResponse) response;

        try {
            // Get correlation ID from header or generate new one
            String correlationId = httpRequest.getHeader(CORRELATION_ID_HEADER);
            if (correlationId == null || correlationId.trim().isEmpty()) {
                correlationId = UUID.randomUUID().toString();
            }

            // Add to MDC for logging
            MDC.put(CORRELATION_ID_MDC_KEY, correlationId);
            MDC.put("userId", getCurrentUserId(httpRequest));
            MDC.put("branchId", getCurrentBranchId(httpRequest));

            // Add to response header
            httpResponse.setHeader(CORRELATION_ID_HEADER, correlationId);

            log.debug("Processing request with correlation ID: {}", correlationId);

            chain.doFilter(request, response);

        } finally {
            // Clear MDC to prevent memory leaks
            MDC.clear();
        }
    }

    private String getCurrentUserId(HttpServletRequest request) {
        // Extract from JWT token or session - implementation depends on auth mechanism
        return request.getHeader("X-User-ID");
    }

    private String getCurrentBranchId(HttpServletRequest request) {
        // Extract from JWT token or session - implementation depends on auth mechanism
        return request.getHeader("X-Branch-ID");
    }
}
