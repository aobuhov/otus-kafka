package ru.otus.kafka.obukhov.term.audit;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.slf4j.MDC;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.util.AntPathMatcher;

import java.io.IOException;
import java.time.OffsetDateTime;

@RequiredArgsConstructor
public class AuditFilter extends OncePerRequestFilter {

    private final AuditPublisher publisher;
    private final AuditProperties properties;
    private final AntPathMatcher pathMatcher = new AntPathMatcher();

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        long start = System.currentTimeMillis();

        try {
            filterChain.doFilter(request, response);
        } finally {
            long duration = System.currentTimeMillis() - start;

            AuditEvent event = AuditEvent.builder()
                    .traceId(getTraceId(request))
                    .userId(getUserId(request.getHeader("X-User-Id")))
                    .serviceName(properties.getServiceName())
                    .endpoint(request.getRequestURI())
                    .method(request.getMethod())
                    .status(response.getStatus())
                    .durationMs(duration)
                    .timestamp(OffsetDateTime.now())
                    .clientIp(request.getRemoteAddr())
                    .build();

            publisher.publish(event);
        }
    }

    @Override
    protected boolean shouldNotFilter(HttpServletRequest request) {
        String path = request.getRequestURI();
        for (String pattern : properties.getExcludePaths()) {
            if (pathMatcher.match(pattern, path)) {
                return true;
            }
        }
        return false;
    }

    private String getTraceId(HttpServletRequest request) {
        // Micrometer Tracing кладёт traceId в MDC как "traceId"
        String traceId = MDC.get("traceId");
        if (traceId != null) {
            return traceId;
        }
        // Fallback: заголовок от upstream
        traceId = request.getHeader("X-Trace-Id");
        return traceId != null ? traceId : "unknown";
    }

    private String getUserId(String userId) {
        return userId == null ? "incognito": userId;
    }
}