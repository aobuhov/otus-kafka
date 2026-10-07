package ru.otus.kafka.obukhov.term.msapigateway.filter;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.security.core.context.ReactiveSecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class UserIdHeaderFilter implements GlobalFilter, Ordered {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        return ReactiveSecurityContextHolder.getContext()
                .map(ctx -> ctx.getAuthentication())
                .filter(auth -> auth instanceof JwtAuthenticationToken)
                .cast(JwtAuthenticationToken.class)
                .map(JwtAuthenticationToken::getToken)
                .map(Jwt::getSubject)  // "sub" claim = userId
                .flatMap(userId -> {
                    // Мутируем запрос, добавляя заголовок X-User-Id
                    ServerWebExchange mutatedExchange = exchange.mutate()
                            .request(r -> r.header("X-User-Id", userId))
                            .build();
                    return chain.filter(mutatedExchange);
                })
                // Если пользователь не аутентифицирован — пропускаем без заголовка
                // (Spring Security уже отклонит запрос на защищённых путях)
                .switchIfEmpty(chain.filter(exchange));
    }

    @Override
    public int getOrder() {
        // Выполняется после SecurityWebFilterChain
        return Ordered.LOWEST_PRECEDENCE;
    }
}