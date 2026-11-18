package com.fraud.edge.filter;

import com.fraud.edge.domain.RequestLogEntity;
import com.fraud.edge.repository.RequestLogRepository;
import java.time.Instant;
import java.util.Optional;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.cloud.gateway.route.Route;
import org.springframework.cloud.gateway.support.ServerWebExchangeUtils;
import org.springframework.core.Ordered;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;
import reactor.core.scheduler.Schedulers;

@Component
@RequiredArgsConstructor
@Slf4j
public class RequestAuditGlobalFilter implements GlobalFilter, Ordered {

    private static final String REQUEST_ID_HEADER = "X-Request-Id";
    private final RequestLogRepository requestLogRepository;

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        long startNanos = System.nanoTime();
        Instant requestedAt = Instant.now();

        String requestId =
                Optional.ofNullable(exchange.getRequest().getHeaders().getFirst(REQUEST_ID_HEADER))
                        .filter(id -> !id.isBlank())
                        .orElseGet(() -> UUID.randomUUID().toString());

        // Propagate X-Request-Id downstream to transaction-gateway-service / fraud-audit-service
        ServerHttpRequest mutatedRequest =
                exchange.getRequest().mutate().header(REQUEST_ID_HEADER, requestId).build();
        exchange.getResponse().getHeaders().set(REQUEST_ID_HEADER, requestId);

        return chain.filter(exchange.mutate().request(mutatedRequest).build())
                .doFinally(
                        signalType -> {
                            long durationMs = (System.nanoTime() - startNanos) / 1_000_000L;
                            Route matchedRoute =
                                    exchange.getAttribute(
                                            ServerWebExchangeUtils.GATEWAY_ROUTE_ATTR);
                            String targetService =
                                    matchedRoute != null ? matchedRoute.getId() : "unmatched";

                            Integer statusCode =
                                    exchange.getResponse().getStatusCode() != null
                                            ? exchange.getResponse().getStatusCode().value()
                                            : null;

                            String clientIp =
                                    exchange.getRequest().getRemoteAddress() != null
                                            ? exchange.getRequest()
                                                    .getRemoteAddress()
                                                    .getAddress()
                                                    .getHostAddress()
                                            : "unknown";

                            RequestLogEntity logEntry =
                                    RequestLogEntity.builder()
                                            .requestId(requestId)
                                            .method(mutatedRequest.getMethod().name())
                                            .path(mutatedRequest.getURI().getPath())
                                            .queryParams(mutatedRequest.getURI().getQuery())
                                            .clientIp(clientIp)
                                            .userAgent(
                                                    mutatedRequest
                                                            .getHeaders()
                                                            .getFirst("User-Agent"))
                                            .targetService(targetService)
                                            .statusCode(statusCode)
                                            .durationMs(durationMs)
                                            .requestedAt(requestedAt)
                                            .build();

                            // Persist asynchronously on boundedElastic so DB latency never impacts
                            // proxy response
                            requestLogRepository
                                    .save(logEntry)
                                    .subscribeOn(Schedulers.boundedElastic())
                                    .doOnError(
                                            ex ->
                                                    log.error(
                                                            "Failed to persist request log {}",
                                                            requestId,
                                                            ex))
                                    .subscribe();
                        });
    }

    @Override
    public int getOrder() {
        return Ordered.HIGHEST_PRECEDENCE;
    }
}
