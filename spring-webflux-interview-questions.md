# Spring WebFlux Interview Questions

## Basic Concepts

### 1. What is Spring WebFlux?
Spring WebFlux is a reactive web framework introduced in Spring 5. It's designed to handle asynchronous, non-blocking operations and is built on top of Project Reactor. Key features:
- Non-blocking I/O
- Reactive streams support
- Backpressure handling
- Event-loop execution model

### 2. What is the difference between Spring MVC and Spring WebFlux?
- **Spring MVC**:
  - Traditional servlet-based framework
  - Thread-per-request model
  - Synchronous, blocking operations
  - Better for simple CRUD applications
- **Spring WebFlux**:
  - Reactive, non-blocking framework
  - Event-loop model
  - Asynchronous operations
  - Better for high-concurrency, low-latency applications

### 3. What is Project Reactor?
Project Reactor is a reactive programming library that provides:
- `Mono<T>`: Publisher that emits 0 or 1 element
- `Flux<T>`: Publisher that emits 0 to N elements
- Operators for transforming, filtering, and combining streams
- Backpressure support

Example:
```java
Flux<String> flux = Flux.just("A", "B", "C")
    .map(String::toLowerCase)
    .filter(s -> s.startsWith("a"));

Mono<String> mono = Mono.just("Hello")
    .map(String::toUpperCase);
```

## Advanced Concepts

### 4. What is Backpressure in Spring WebFlux?
Backpressure is a mechanism to handle the situation when a publisher produces data faster than a subscriber can consume it. Spring WebFlux provides several strategies:

```java
// Buffer strategy
Flux.range(1, 100)
    .buffer(10)
    .subscribe(System.out::println);

// Drop strategy
Flux.range(1, 100)
    .onBackpressureDrop()
    .subscribe(System.out::println);

// Latest strategy
Flux.range(1, 100)
    .onBackpressureLatest()
    .subscribe(System.out::println);
```

### 5. How do you create a REST API with Spring WebFlux?

```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    private final UserService userService;

    public UserController(UserService userService) {
        this.userService = userService;
    }

    @GetMapping
    public Flux<User> getAllUsers() {
        return userService.findAll();
    }

    @GetMapping("/{id}")
    public Mono<User> getUserById(@PathVariable String id) {
        return userService.findById(id);
    }

    @PostMapping
    public Mono<User> createUser(@RequestBody User user) {
        return userService.save(user);
    }

    @PutMapping("/{id}")
    public Mono<User> updateUser(@PathVariable String id, @RequestBody User user) {
        return userService.update(id, user);
    }

    @DeleteMapping("/{id}")
    public Mono<Void> deleteUser(@PathVariable String id) {
        return userService.delete(id);
    }
}
```

### 6. What is the difference between Mono and Flux?
- **Mono<T>**:
  - Emits 0 or 1 element
  - Used for single-value operations
  - Example: Finding a user by ID
  ```java
  Mono<User> user = userRepository.findById(id);
  ```

- **Flux<T>**:
  - Emits 0 to N elements
  - Used for multiple-value operations
  - Example: Getting all users
  ```java
  Flux<User> users = userRepository.findAll();
  ```

### 7. How do you handle errors in Spring WebFlux?

```java
@RestController
public class UserController {
    @GetMapping("/users/{id}")
    public Mono<User> getUserById(@PathVariable String id) {
        return userService.findById(id)
            .switchIfEmpty(Mono.error(new UserNotFoundException(id)))
            .onErrorResume(e -> {
                if (e instanceof UserNotFoundException) {
                    return Mono.error(new ResponseStatusException(
                        HttpStatus.NOT_FOUND, "User not found", e));
                }
                return Mono.error(e);
            });
    }
}

// Global error handling
@ControllerAdvice
public class GlobalErrorHandler {
    @ExceptionHandler(UserNotFoundException.class)
    public ResponseEntity<Mono<String>> handleUserNotFound(UserNotFoundException ex) {
        return ResponseEntity
            .status(HttpStatus.NOT_FOUND)
            .body(Mono.just(ex.getMessage()));
    }
}
```

### 8. How do you implement WebSocket with Spring WebFlux?

```java
@Configuration
public class WebSocketConfig {
    @Bean
    public HandlerMapping webSocketHandlerMapping() {
        Map<String, WebSocketHandler> map = new HashMap<>();
        map.put("/ws", new WebSocketHandler());
        
        SimpleUrlHandlerMapping handlerMapping = new SimpleUrlHandlerMapping();
        handlerMapping.setUrlMap(map);
        handlerMapping.setOrder(-1);
        return handlerMapping;
    }
}

@Component
public class WebSocketHandler implements WebSocketHandler {
    @Override
    public Mono<Void> handle(WebSocketSession session) {
        return session.send(
            session.receive()
                .map(msg -> session.textMessage("Echo: " + msg.getPayloadAsText()))
        );
    }
}
```

### 9. How do you implement Server-Sent Events (SSE) with Spring WebFlux?

```java
@RestController
@RequestMapping("/api/events")
public class EventController {
    @GetMapping(produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public Flux<ServerSentEvent<String>> streamEvents() {
        return Flux.interval(Duration.ofSeconds(1))
            .map(sequence -> ServerSentEvent.<String>builder()
                .id(String.valueOf(sequence))
                .event("periodic-event")
                .data("SSE - " + LocalTime.now().toString())
                .build());
    }
}
```

### 10. How do you implement caching in Spring WebFlux?

```java
@Service
public class UserService {
    private final ReactiveRedisTemplate<String, User> redisTemplate;
    
    public UserService(ReactiveRedisTemplate<String, User> redisTemplate) {
        this.redisTemplate = redisTemplate;
    }
    
    public Mono<User> findById(String id) {
        return redisTemplate.opsForValue()
            .get("user:" + id)
            .switchIfEmpty(
                userRepository.findById(id)
                    .flatMap(user -> 
                        redisTemplate.opsForValue()
                            .set("user:" + id, user)
                            .thenReturn(user)
                    )
            );
    }
}
```

## Advanced Concepts and Real-World Scenarios

### 11. How do you handle concurrent requests in Spring WebFlux?
```java
@Service
public class UserService {
    private final ReactiveRedisTemplate<String, User> redisTemplate;
    private final UserRepository userRepository;

    // Using Sinks for concurrent event handling
    private final Sinks.Many<User> userSink = Sinks.many().multicast().onBackpressureBuffer();

    public Flux<User> streamUsers() {
        return userSink.asFlux();
    }

    public Mono<User> updateUser(String id, User user) {
        return userRepository.findById(id)
            .flatMap(existingUser -> {
                // Handle concurrent updates
                return redisTemplate.opsForValue()
                    .set("user:" + id, user, Duration.ofMinutes(5))
                    .then(userRepository.save(user))
                    .doOnNext(savedUser -> userSink.tryEmitNext(savedUser));
            });
    }
}
```

### 12. How do you implement circuit breaker pattern in Spring WebFlux?
```java
@Service
public class ExternalServiceClient {
    private final CircuitBreaker circuitBreaker;
    private final WebClient webClient;

    public ExternalServiceClient(WebClient webClient) {
        this.webClient = webClient;
        this.circuitBreaker = CircuitBreaker.builder()
            .failureRateThreshold(50)
            .waitDurationInOpenState(Duration.ofSeconds(60))
            .permittedNumberOfCallsInHalfOpenState(2)
            .slidingWindowSize(2)
            .build();
    }

    public Mono<Response> callExternalService() {
        return circuitBreaker.execute(() ->
            webClient.get()
                .uri("/api/external")
                .retrieve()
                .bodyToMono(Response.class)
                .timeout(Duration.ofSeconds(5))
        );
    }
}
```

### 13. How do you implement retry mechanism in Spring WebFlux?
```java
@Service
public class ResilientService {
    private final WebClient webClient;

    public Mono<Response> callWithRetry() {
        return webClient.get()
            .uri("/api/unstable")
            .retrieve()
            .bodyToMono(Response.class)
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                .filter(ex -> ex instanceof WebClientResponseException)
                .doBeforeRetry(signal -> 
                    log.info("Retrying after failure: {}", signal.failure()))
            );
    }

    public Mono<Response> callWithExponentialBackoff() {
        return webClient.get()
            .uri("/api/unstable")
            .retrieve()
            .bodyToMono(Response.class)
            .retryWhen(Retry.backoff(3, Duration.ofSeconds(1))
                .maxBackoff(Duration.ofSeconds(10))
                .jitter(0.1)
            );
    }
}
```

### 14. How do you implement request/response correlation in Spring WebFlux?
```java
@Component
public class CorrelationFilter implements WebFilter {
    private static final String CORRELATION_ID = "X-Correlation-ID";

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String correlationId = exchange.getRequest()
            .getHeaders()
            .getFirst(CORRELATION_ID);
        
        if (correlationId == null) {
            correlationId = UUID.randomUUID().toString();
        }

        return chain.filter(exchange)
            .contextWrite(Context.of("correlationId", correlationId))
            .doFinally(signalType -> 
                exchange.getResponse()
                    .getHeaders()
                    .add(CORRELATION_ID, correlationId)
            );
    }
}

@Service
public class LoggingService {
    public Mono<Void> logRequest(String message) {
        return Mono.deferContextual(ctx -> {
            String correlationId = ctx.get("correlationId");
            log.info("[{}] {}", correlationId, message);
            return Mono.empty();
        });
    }
}
```

### 15. How do you implement request throttling in Spring WebFlux?
```java
@Component
public class ThrottlingFilter implements WebFilter {
    private final RateLimiter rateLimiter;
    private final Map<String, RateLimiter> userRateLimiters = new ConcurrentHashMap<>();

    public ThrottlingFilter() {
        this.rateLimiter = RateLimiter.create(100.0); // Global rate limit
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        String userId = exchange.getRequest()
            .getHeaders()
            .getFirst("X-User-ID");

        RateLimiter userLimiter = userRateLimiters.computeIfAbsent(userId,
            k -> RateLimiter.create(10.0)); // Per-user rate limit

        if (!rateLimiter.tryAcquire() || !userLimiter.tryAcquire()) {
            return Mono.error(new ResponseStatusException(
                HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded"));
        }

        return chain.filter(exchange);
    }
}
```

### 16. How do you implement request validation in Spring WebFlux?
```java
@RestController
@RequestMapping("/api/users")
public class UserController {
    @PostMapping
    public Mono<ResponseEntity<User>> createUser(@Valid @RequestBody UserDTO userDTO) {
        return Mono.just(userDTO)
            .map(this::validateUser)
            .flatMap(validationResult -> {
                if (!validationResult.isValid()) {
                    return Mono.error(new ValidationException(validationResult.getErrors()));
                }
                return userService.createUser(userDTO);
            })
            .map(user -> ResponseEntity.ok(user))
            .onErrorResume(ValidationException.class, ex ->
                Mono.just(ResponseEntity.badRequest().build())
            );
    }

    private ValidationResult validateUser(UserDTO userDTO) {
        List<String> errors = new ArrayList<>();
        
        if (userDTO.getAge() < 18) {
            errors.add("User must be at least 18 years old");
        }
        
        if (userDTO.getEmail() != null && !isValidEmail(userDTO.getEmail())) {
            errors.add("Invalid email format");
        }
        
        return new ValidationResult(errors.isEmpty(), errors);
    }
}
```

### 17. How do you implement request/response transformation in Spring WebFlux?
```java
@Component
public class TransformationFilter implements WebFilter {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        ServerHttpResponse response = exchange.getResponse();

        // Transform request
        ServerHttpRequest transformedRequest = request.mutate()
            .headers(headers -> {
                headers.add("X-Transformed", "true");
                // Add other transformations
            })
            .build();

        // Transform response
        ServerHttpResponse transformedResponse = response.mutate()
            .headers(headers -> {
                headers.add("X-Response-Time", 
                    String.valueOf(System.currentTimeMillis()));
            })
            .build();

        return chain.filter(exchange.mutate()
            .request(transformedRequest)
            .response(transformedResponse)
            .build());
    }
}
```

### 18. How do you implement request caching in Spring WebFlux?
```java
@Service
public class CachingService {
    private final ReactiveRedisTemplate<String, String> redisTemplate;
    private final WebClient webClient;

    public Mono<String> getCachedData(String key) {
        return redisTemplate.opsForValue()
            .get(key)
            .switchIfEmpty(
                webClient.get()
                    .uri("/api/data/" + key)
                    .retrieve()
                    .bodyToMono(String.class)
                    .flatMap(data ->
                        redisTemplate.opsForValue()
                            .set(key, data, Duration.ofMinutes(5))
                            .thenReturn(data)
                    )
            );
    }

    public Mono<Void> invalidateCache(String key) {
        return redisTemplate.delete(key)
            .then();
    }
}
```

### 19. How do you implement request batching in Spring WebFlux?
```java
@Service
public class BatchProcessingService {
    private final Sinks.Many<Request> requestSink = Sinks.many().multicast().onBackpressureBuffer();
    private final Flux<List<Request>> batchedRequests;

    public BatchProcessingService() {
        this.batchedRequests = requestSink.asFlux()
            .bufferTimeout(100, Duration.ofMillis(100))
            .filter(list -> !list.isEmpty());
    }

    public Mono<Void> processRequest(Request request) {
        return Mono.fromRunnable(() -> 
            requestSink.tryEmitNext(request)
        );
    }

    public Flux<Response> processBatch() {
        return batchedRequests
            .flatMap(this::processBatchRequests)
            .flatMapIterable(Function.identity());
    }

    private Mono<List<Response>> processBatchRequests(List<Request> requests) {
        // Process batch of requests
        return Mono.just(requests.stream()
            .map(this::processRequest)
            .collect(Collectors.toList()));
    }
}
```

### 20. How do you implement request tracing in Spring WebFlux?
```java
@Component
public class TracingFilter implements WebFilter {
    private final Tracer tracer;

    public TracingFilter(Tracer tracer) {
        this.tracer = tracer;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        Span span = tracer.buildSpan("http-request")
            .withTag("path", exchange.getRequest().getPath().toString())
            .start();

        return chain.filter(exchange)
            .doFinally(signalType -> {
                span.setTag("status", exchange.getResponse().getStatusCode().value());
                span.finish();
            });
    }
}

@Service
public class TracedService {
    private final Tracer tracer;

    public Mono<String> processRequest() {
        return Mono.deferContextual(ctx -> {
            Span span = tracer.buildSpan("process-request").start();
            return Mono.just("processed")
                .doFinally(signalType -> span.finish());
        });
    }
}
```

Remember:
- Always consider backpressure in your reactive streams
- Use appropriate operators for your use case
- Handle errors properly
- Consider performance implications
- Use appropriate caching strategies
- Implement proper monitoring and tracing
- Consider security implications
- Test thoroughly with reactive testing tools

## Best Practices

### 21. What are the best practices for Spring WebFlux?

1. **Use Appropriate Operators**:
   ```java
   // Good
   Flux<User> users = userRepository.findAll()
       .filter(user -> user.isActive())
       .map(this::enrichUser)
       .timeout(Duration.ofSeconds(5));

   // Bad
   Flux<User> users = userRepository.findAll()
       .doOnNext(user -> {
           // Side effects
           log.info("Processing user: {}", user);
       });
   ```

2. **Handle Backpressure**:
   ```java
   // Good
   Flux<User> users = userRepository.findAll()
       .onBackpressureBuffer(100)
       .timeout(Duration.ofSeconds(5));

   // Bad
   Flux<User> users = userRepository.findAll()
       .doOnNext(user -> {
           // No backpressure handling
           processUser(user);
       });
   ```

3. **Error Handling**:
   ```java
   // Good
   Mono<User> user = userRepository.findById(id)
       .timeout(Duration.ofSeconds(5))
       .onErrorResume(e -> {
           log.error("Error fetching user", e);
           return Mono.error(new CustomException("User not found"));
       });

   // Bad
   Mono<User> user = userRepository.findById(id)
       .doOnError(e -> {
           // Just logging, no proper error handling
           log.error("Error", e);
       });
   ```

4. **Resource Management**:
   ```java
   // Good
   Mono<Void> result = Mono.using(
       () -> createResource(),
       resource -> processResource(resource),
       resource -> cleanupResource(resource)
   );

   // Bad
   Resource resource = createResource();
   try {
       processResource(resource);
   } finally {
       cleanupResource(resource);
   }
   ```

### 22. How do you test Spring WebFlux applications?

```java
@SpringBootTest
@AutoConfigureWebTestClient
class UserControllerTest {
    @Autowired
    private WebTestClient webTestClient;

    @Test
    void testGetUser() {
        webTestClient.get()
            .uri("/api/users/1")
            .exchange()
            .expectStatus().isOk()
            .expectBody()
            .jsonPath("$.id").isEqualTo("1")
            .jsonPath("$.name").isEqualTo("John");
    }

    @Test
    void testGetAllUsers() {
        webTestClient.get()
            .uri("/api/users")
            .exchange()
            .expectStatus().isOk()
            .expectBodyList(User.class)
            .hasSize(2);
    }
}
```

### 23. How do you implement security in Spring WebFlux?

```java
@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain springSecurityFilterChain(ServerHttpSecurity http) {
        return http
            .authorizeExchange()
                .pathMatchers("/api/public/**").permitAll()
                .pathMatchers("/api/admin/**").hasRole("ADMIN")
                .anyExchange().authenticated()
            .and()
            .csrf().disable()
            .httpBasic()
            .and()
            .build();
    }
}
```

### 24. How do you implement rate limiting in Spring WebFlux?

```java
@Configuration
public class RateLimiterConfig {
    @Bean
    public RateLimiter rateLimiter() {
        return RateLimiter.create(100.0); // 100 requests per second
    }
}

@Component
public class RateLimiterFilter implements WebFilter {
    private final RateLimiter rateLimiter;

    public RateLimiterFilter(RateLimiter rateLimiter) {
        this.rateLimiter = rateLimiter;
    }

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
        if (rateLimiter.tryAcquire()) {
            return chain.filter(exchange);
        }
        return Mono.error(new ResponseStatusException(
            HttpStatus.TOO_MANY_REQUESTS, "Rate limit exceeded"));
    }
}
```

Remember:
- Use appropriate reactive types (Mono/Flux)
- Handle backpressure properly
- Implement proper error handling
- Use appropriate operators
- Test thoroughly
- Consider security implications
- Monitor performance
- Use appropriate caching strategies 