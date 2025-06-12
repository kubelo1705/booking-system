package com.booking.gateway.filter;

import com.booking.gateway.model.UserAction;
import com.booking.gateway.repository.UserActionRepository;
import com.booking.gateway.util.JwtUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.http.HttpHeaders;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.util.Date;

@Component
public class GlobalGatewayFilter implements GlobalFilter, Ordered {
    private static final Logger logger = LoggerFactory.getLogger(GlobalGatewayFilter.class);
    private static final String USER_INFO_HEADER = "X-User-Info";
    private static final String REDIS_USER_PREFIX = "user-info:";
    private static final Duration USER_CACHE_TTL = Duration.ofMinutes(30);

    @Autowired
    private UserActionRepository userActionRepository;
    @Autowired
    private StringRedisTemplate redisTemplate;
    private final WebClient userWebClient = WebClient.builder().baseUrl("http://user-service:8080").build();

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        final String path = exchange.getRequest().getPath().toString();
        final String method = exchange.getRequest().getMethod().name();
        String userId = extractUserIdFromToken(exchange);
        if (userId == null) {
            return unauthorized(exchange);
        }
        return getUserInfo(userId)
                .flatMap(userInfoJson -> {
                    ServerWebExchange mutatedExchange = mutateExchangeWithUserInfo(exchange, userInfoJson);
                    logRequest(method, path, userId, userInfoJson.contains("from cache"));
                    saveUserAction(userId, method, path);
                    return chain.filter(mutatedExchange).doOnSuccess(done -> logResponse(mutatedExchange, method, path, userId));
                })
                .onErrorResume(e -> unauthorized(exchange));
    }

    private String extractUserIdFromToken(ServerWebExchange exchange) {
        String authHeader = exchange.getRequest().getHeaders().getFirst(HttpHeaders.AUTHORIZATION);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return null;
        }
        String token = authHeader.substring(7);
        try {
            return JwtUtil.validateTokenAndGetUserId(token);
        } catch (Exception e) {
            return null;
        }
    }

    private Mono<String> getUserInfo(String userId) {
        String redisKey = REDIS_USER_PREFIX + userId;
        String cachedUserInfo = redisTemplate.opsForValue().get(redisKey);
        if (cachedUserInfo != null) {
            return Mono.just(cachedUserInfo);
        }
        return userWebClient.get()
                .uri("/users/" + userId)
                .retrieve()
                .bodyToMono(String.class)
                .doOnNext(userInfoJson -> redisTemplate.opsForValue().set(redisKey, userInfoJson, USER_CACHE_TTL));
    }

    private ServerWebExchange mutateExchangeWithUserInfo(ServerWebExchange exchange, String userInfoJson) {
        ServerHttpRequest mutatedRequest = exchange.getRequest().mutate()
                .header(USER_INFO_HEADER, userInfoJson)
                .build();
        return exchange.mutate().request(mutatedRequest).build();
    }

    private void logRequest(String method, String path, String userId, boolean fromCache) {
        if (fromCache) {
            logger.info("Request: {} {} by {} (from cache)", method, path, userId);
        } else {
            logger.info("Request: {} {} by {}", method, path, userId);
        }
    }

    private void logResponse(ServerWebExchange exchange, String method, String path, String userId) {
        int status = exchange.getResponse().getStatusCode() != null ? exchange.getResponse().getStatusCode().value() : 0;
        logger.info("Response: {} {} by {} -> {}", method, path, userId, status);
    }

    private void saveUserAction(String userId, String method, String path) {
        userActionRepository.save(new UserAction(userId, method, path, new Date()));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(org.springframework.http.HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1; // Highest precedence
    }
} 