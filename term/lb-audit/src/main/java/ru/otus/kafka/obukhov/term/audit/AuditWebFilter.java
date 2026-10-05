package ru.otus.kafka.obukhov.term.audit;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.slf4j.MDC;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.server.ServerWebExchange;
import org.springframework.web.server.WebFilter;
import org.springframework.web.server.WebFilterChain;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

@Slf4j
@RequiredArgsConstructor
public class AuditWebFilter implements WebFilter, Ordered {

    private final AuditPublisher publisher;
    private final AuditProperties properties;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        long start = System.currentTimeMillis();

        String path = exchange.getRequest().getURI().getPath();
        String method = exchange.getRequest().getMethod().name();

        // Пропускаем служебные пути (actuator и т.д.)
        if (shouldSkip(path)) {
            return chain.filter(exchange);
        }

        return chain.filter(exchange)
                .doFinally(signalType -> {
                    long duration = System.currentTimeMillis() - start;
                    Integer status = exchange.getResponse().getStatusCode() != null
                            ? exchange.getResponse().getStatusCode().value()
                            : 0;

                    AuditEvent event = AuditEvent.builder()
                            .traceId(getTraceId())
                            .userId(getUserId(exchange))
                            .serviceName(properties.getServiceName())
                            .endpoint(path)
                            .method(method)
                            .status(status)
                            .durationMs(duration)
                            .timestamp(OffsetDateTime.now())
                            .clientIp(getClientIp(exchange))
                            .build();

                    publisher.publish(event);
                });
    }

    @Override
    public int getOrder() {
        // Выполняется после фильтров безопасности,
        // но до маршрутизации Gateway
        return Ordered.LOWEST_PRECEDENCE - 100;
    }

    private boolean shouldSkip(String path) {
        for (String pattern : properties.getExcludePaths()) {
            if (path.startsWith(pattern.replace("/**", ""))) {
                return true;
            }
        }
        return false;
    }

    private String getTraceId() {
        // Micrometer кладёт traceId в MDC при включённом трейсинге
        String traceId = MDC.get("traceId");
        return traceId != null ? traceId : "unknown";
    }

    private String getUserId(ServerWebExchange exchange) {
        // На gateway userId уже в SecurityContext, т.к. JWT валидируется до этого фильтра
        // Но проще взять из заголовка, если фильтр сработал после UserIdHeaderFilter
        String userId = exchange.getRequest().getHeaders().getFirst("X-User-Id");
        if (userId != null) {
            return userId;
        }
        // Fallback: пробуем из SecurityContext реактивно — но это усложняет код
        return null;
    }

    private String getClientIp(ServerWebExchange exchange) {
        String forwarded = exchange.getRequest().getHeaders().getFirst("X-Forwarded-For");
        if (forwarded != null && !forwarded.isEmpty()) {
            return forwarded.split(",")[0].trim();
        }
        return exchange.getRequest().getRemoteAddress() != null
                ? exchange.getRequest().getRemoteAddress().getAddress().getHostAddress()
                : "unknown";
    }
}