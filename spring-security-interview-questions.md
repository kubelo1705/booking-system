# Spring Security Interview Questions

## Basic Concepts

### 1. What is Spring Security and what are its key features?

Spring Security is a powerful and highly customizable authentication and authorization framework for Java applications. Key features include:

1. **Authentication**
   - Form-based authentication
   - Basic authentication
   - OAuth2/OIDC
   - JWT
   - LDAP
   - Custom authentication

2. **Authorization**
   - Role-based access control (RBAC)
   - Method-level security
   - URL-based security
   - Custom authorization

3. **Security Features**
   - CSRF protection
   - Session management
   - Remember-me functionality
   - Password encoding
   - Security headers
   - XSS protection

### 2. How do you configure basic authentication in Spring Security?

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/public/**").permitAll()
                .anyRequest().authenticated()
            )
            .httpBasic(Customizer.withDefaults())
            .formLogin(form -> form
                .loginPage("/login")
                .permitAll()
            );
        
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
}
```

### 3. What is the difference between Authentication and Authorization?

**Authentication** is the process of verifying who a user is:
```java
import org.springframework.security.authentication.BadCredentialsException;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service
public class AuthenticationService {
    private final UserDetailsService userDetailsService;
    private final PasswordEncoder passwordEncoder;

    public AuthenticationService(UserDetailsService userDetailsService, 
                               PasswordEncoder passwordEncoder) {
        this.userDetailsService = userDetailsService;
        this.passwordEncoder = passwordEncoder;
    }

    public Authentication authenticate(String username, String password) {
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        
        if (!passwordEncoder.matches(password, userDetails.getPassword())) {
            throw new BadCredentialsException("Invalid password");
        }
        
        return new UsernamePasswordAuthenticationToken(
            userDetails, null, userDetails.getAuthorities());
    }
}
```

**Authorization** is the process of verifying what a user has access to:
```java
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Service;

@Service
public class AuthorizationService {
    public boolean hasAccess(UserDetails user, String resource) {
        return user.getAuthorities().stream()
            .anyMatch(authority -> 
                authority.getAuthority().equals("ROLE_ADMIN") ||
                (authority.getAuthority().equals("ROLE_USER") && 
                 resource.startsWith("/user/"))
            );
    }
}
```

## Advanced Concepts

### 4. How do you implement JWT authentication in Spring Security?

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.filter.OncePerRequestFilter;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

@Configuration
@EnableWebSecurity
public class JwtSecurityConfig {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    public JwtSecurityConfig(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf.disable())
            .sessionManagement(session -> session
                .sessionCreationPolicy(SessionCreationPolicy.STATELESS))
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/api/auth/**").permitAll()
                .anyRequest().authenticated()
            )
            .addFilterBefore(jwtAuthenticationFilter(),
                UsernamePasswordAuthenticationFilter.class);
        
        return http.build();
    }

    @Bean
    public JwtAuthenticationFilter jwtAuthenticationFilter() {
        return new JwtAuthenticationFilter(jwtTokenProvider);
    }
}

@Component
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    
    private final JwtTokenProvider jwtTokenProvider;
    
    public JwtAuthenticationFilter(JwtTokenProvider jwtTokenProvider) {
        this.jwtTokenProvider = jwtTokenProvider;
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException, IOException {
        String token = extractToken(request);
        
        if (token != null && jwtTokenProvider.validateToken(token)) {
            Authentication auth = jwtTokenProvider.getAuthentication(token);
            SecurityContextHolder.getContext().setAuthentication(auth);
        }
        
        filterChain.doFilter(request, response);
    }
    
    private String extractToken(HttpServletRequest request) {
        String bearerToken = request.getHeader("Authorization");
        if (bearerToken != null && bearerToken.startsWith("Bearer ")) {
            return bearerToken.substring(7);
        }
        return null;
    }
}
```

### 5. How do you implement OAuth2 with Spring Security?

```java
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.oauth2.client.registration.ClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.OAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.reactive.function.client.ServletOAuth2AuthorizedClientExchangeFilterFunction;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.reactive.function.client.WebClient;

@Configuration
@EnableWebSecurity
public class OAuth2SecurityConfig {
    
    private final ClientRegistrationRepository clientRegistrationRepository;
    
    public OAuth2SecurityConfig(ClientRegistrationRepository clientRegistrationRepository) {
        this.clientRegistrationRepository = clientRegistrationRepository;
    }
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .oauth2Login(oauth2 -> oauth2
                .clientRegistrationRepository(clientRegistrationRepository)
                .authorizedClientService(authorizedClientService())
                .loginPage("/login")
            )
            .authorizeHttpRequests(auth -> auth
                .requestMatchers("/", "/login**", "/error**").permitAll()
                .anyRequest().authenticated()
            );
        
        return http.build();
    }

    @Bean
    public OAuth2AuthorizedClientService authorizedClientService() {
        return new InMemoryOAuth2AuthorizedClientService(clientRegistrationRepository);
    }
    
    @Bean
    public WebClient webClient(OAuth2AuthorizedClientRepository authorizedClientRepository) {
        ServletOAuth2AuthorizedClientExchangeFilterFunction oauth2 = 
            new ServletOAuth2AuthorizedClientExchangeFilterFunction(
                clientRegistrationRepository, authorizedClientRepository);
        oauth2.setDefaultOAuth2AuthorizedClient(true);
        return WebClient.builder()
            .apply(oauth2.oauth2Configuration())
            .build();
    }
}
```

### 6. How do you implement method-level security?

```java
@Configuration
@EnableMethodSecurity
public class MethodSecurityConfig {
    // Configuration
}

@Service
public class UserService {
    
    @PreAuthorize("hasRole('ADMIN')")
    public User createUser(UserDTO userDTO) {
        // Only admins can create users
    }
    
    @PreAuthorize("hasRole('ADMIN') or #username == authentication.name")
    public User getUser(String username) {
        // Admins can get any user, users can only get themselves
    }
    
    @PostAuthorize("returnObject.username == authentication.name")
    public User getCurrentUser() {
        // Users can only access their own data
    }
    
    @PreAuthorize("hasRole('ADMIN')")
    @PostAuthorize("returnObject.size() <= 10")
    public List<User> getUsers() {
        // Only admins can get users, max 10 users
    }
}
```

### 7. How do you implement custom authentication?

```java
@Component
public class CustomAuthenticationProvider implements AuthenticationProvider {
    
    @Override
    public Authentication authenticate(Authentication authentication) 
            throws AuthenticationException {
        String username = authentication.getName();
        String password = authentication.getCredentials().toString();
        
        // Custom authentication logic
        if (isValidUser(username, password)) {
            return new UsernamePasswordAuthenticationToken(
                username, password, getAuthorities(username));
        }
        
        throw new BadCredentialsException("Invalid credentials");
    }
    
    @Override
    public boolean supports(Class<?> authentication) {
        return UsernamePasswordAuthenticationToken.class
            .isAssignableFrom(authentication);
    }
}
```

### 8. How do you implement remember-me functionality?

```java
@Configuration
@EnableWebSecurity
public class RememberMeConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .rememberMe(remember -> remember
                .key("uniqueAndSecret")
                .tokenValiditySeconds(86400) // 24 hours
                .userDetailsService(userDetailsService)
            );
        
        return http.build();
    }
}
```

## Security Best Practices

### 9. How do you implement password encoding?

```java
@Configuration
public class PasswordEncoderConfig {
    
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12); // 12 rounds
    }
}

@Service
public class UserService {
    
    private final PasswordEncoder passwordEncoder;
    
    public User createUser(UserDTO userDTO) {
        User user = new User();
        user.setUsername(userDTO.getUsername());
        user.setPassword(passwordEncoder.encode(userDTO.getPassword()));
        return userRepository.save(user);
    }
}
```

### 10. How do you implement CSRF protection?

```java
@Configuration
@EnableWebSecurity
public class CsrfConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .csrf(csrf -> csrf
                .csrfTokenRepository(CookieCsrfTokenRepository.withHttpOnlyFalse())
                .ignoringRequestMatchers("/api/**") // Disable CSRF for API endpoints
            );
        
        return http.build();
    }
}
```

### 11. How do you implement session management?

```java
@Configuration
@EnableWebSecurity
public class SessionConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .sessionManagement(session -> session
                .maximumSessions(1)
                .expiredUrl("/login?expired")
                .and()
                .sessionCreationPolicy(SessionCreationPolicy.IF_REQUIRED)
                .invalidSessionUrl("/login?invalid")
                .sessionFixation().migrateSession()
            );
        
        return http.build();
    }
}
```

### 12. How do you implement security headers?

```java
@Configuration
@EnableWebSecurity
public class SecurityHeadersConfig {
    
    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
            .headers(headers -> headers
                .contentSecurityPolicy(csp -> csp
                    .policyDirectives("default-src 'self'")
                )
                .frameOptions(frame -> frame
                    .deny()
                )
                .xssProtection(xss -> xss
                    .block(true)
                )
                .httpStrictTransportSecurity(hsts -> hsts
                    .includeSubDomains(true)
                    .maxAgeInSeconds(31536000)
                )
            );
        
        return http.build();
    }
}
```

## Testing Security

### 13. How do you test Spring Security configurations?

```java
@SpringBootTest
@AutoConfigureMockMvc
class SecurityTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Test
    void whenAnonymousUser_thenAccessDenied() throws Exception {
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isUnauthorized());
    }
    
    @Test
    @WithMockUser(roles = "ADMIN")
    void whenAdminUser_thenAccessGranted() throws Exception {
        mockMvc.perform(get("/api/users"))
            .andExpect(status().isOk());
    }
    
    @Test
    @WithUserDetails("user")
    void whenUserDetails_thenAccessGranted() throws Exception {
        mockMvc.perform(get("/api/profile"))
            .andExpect(status().isOk());
    }
}
```

### 14. How do you test JWT authentication?

```java
@SpringBootTest
@AutoConfigureMockMvc
class JwtAuthenticationTest {
    
    @Autowired
    private MockMvc mockMvc;
    
    @Autowired
    private JwtTokenProvider jwtTokenProvider;
    
    @Test
    void whenValidToken_thenAccessGranted() throws Exception {
        String token = jwtTokenProvider.createToken("user", "ROLE_USER");
        
        mockMvc.perform(get("/api/profile")
            .header("Authorization", "Bearer " + token))
            .andExpect(status().isOk());
    }
    
    @Test
    void whenInvalidToken_thenAccessDenied() throws Exception {
        mockMvc.perform(get("/api/profile")
            .header("Authorization", "Bearer invalid-token"))
            .andExpect(status().isUnauthorized());
    }
}
```

## Common Security Issues and Solutions

### 15. How do you handle security exceptions?

```java
@ControllerAdvice
public class SecurityExceptionHandler {
    
    @ExceptionHandler(AccessDeniedException.class)
    public ResponseEntity<ErrorResponse> handleAccessDeniedException(
            AccessDeniedException ex) {
        return ResponseEntity
            .status(HttpStatus.FORBIDDEN)
            .body(new ErrorResponse("Access denied", ex.getMessage()));
    }
    
    @ExceptionHandler(AuthenticationException.class)
    public ResponseEntity<ErrorResponse> handleAuthenticationException(
            AuthenticationException ex) {
        return ResponseEntity
            .status(HttpStatus.UNAUTHORIZED)
            .body(new ErrorResponse("Authentication failed", ex.getMessage()));
    }
}
```

### 16. How do you implement rate limiting?

```java
@Component
public class RateLimitingFilter extends OncePerRequestFilter {
    
    private final RateLimiter rateLimiter;
    
    public RateLimitingFilter() {
        this.rateLimiter = RateLimiter.create(100.0); // 100 requests per second
    }
    
    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                  HttpServletResponse response,
                                  FilterChain filterChain) throws ServletException {
        if (!rateLimiter.tryAcquire()) {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            return;
        }
        
        filterChain.doFilter(request, response);
    }
}
```

Remember:
- Always use HTTPS in production
- Implement proper password policies
- Use secure session management
- Implement proper logging
- Regular security audits
- Keep dependencies updated
- Follow security best practices
- Implement proper error handling
- Use appropriate security headers
- Regular penetration testing
