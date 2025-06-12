package com.booking.gateway.controller;

import com.booking.gateway.util.JwtUtil;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Map;

@RestController
@RequestMapping("/auth")
public class AuthController {
    private final WebClient webClient;

    public AuthController(@Value("${user.service.url:http://user-service:8080}") String userServiceUrl) {
        this.webClient = WebClient.builder().baseUrl(userServiceUrl).build();
    }

    @PostMapping("/login")
    public Mono<ResponseEntity<Map<String, String>>> login(@RequestBody Map<String, String> loginRequest) {
        // Forward login to user-service for real authentication
        return webClient.post()
                .uri("/auth/login")
                .bodyValue(loginRequest)
                .retrieve()
                .bodyToMono(Map.class)
                .map(userInfo -> {
                    String userId = (String) userInfo.get("id");
                    String token = JwtUtil.generateToken(userId);
                    return ResponseEntity.ok(Map.of("token", token));
                })
                .onErrorResume(e -> Mono.just(ResponseEntity.status(HttpStatus.UNAUTHORIZED).body(Map.of("error", "Invalid credentials"))));
    }
} 