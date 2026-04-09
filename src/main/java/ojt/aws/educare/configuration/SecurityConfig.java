package ojt.aws.educare.configuration;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.Ordered;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.jwt.JwtClaimValidator;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.jwt.JwtValidators;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.oauth2.core.DelegatingOAuth2TokenValidator;
import org.springframework.security.oauth2.core.OAuth2TokenValidator;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;
import java.util.Locale;

import ojt.aws.educare.entity.User;
import ojt.aws.educare.repository.UserRepository;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class SecurityConfig {

    @Value("${spring.security.oauth2.resourceserver.jwt.issuer-uri}")
    private String issuerUri;

    @Value("${cognito.app-client-id}")
    private String cognitoAppClientId;

    // ── PUBLIC_ENDPOINTS ──────────────────────────────────────────────────────
    // OLD local-auth endpoints are removed from the public list because login,
    // introspect, and logout are now fully handled by AWS Cognito Hosted UI.
    // Users authenticate via Cognito and receive a JWT that this service validates.
    private final String[] PUBLIC_ENDPOINTS = {
            "/auth/account-status",
            // OLD (commented): local auth endpoints no longer used with Cognito:
            // "/auth/login",
            // "/auth/introspect",
            // "/auth/logout",
            // "/users/register",

            // Forgot-password flow still served by this backend (not via Cognito):
            "/users/forgot-password",
            "/users/verify-otp",
            "/users/reset-password",

            // Actuator health — public so ALB/ECS health checks work without a token.
            // Path is relative to the servlet context-path (/api), so this matches
            // GET /api/actuator/health. Only "health" is exposed (see application.yml).
            "/actuator/health",
    };

    // OLD: @Autowired CustomJwtDecoder customJwtDecoder;
    // REPLACED: Spring Boot auto-configures a NimbusJwtDecoder from the
    // spring.security.oauth2.resourceserver.jwt.issuer-uri in application.yml.
    // Cognito exposes its JWK Set at:
    //   https://cognito-idp.ap-southeast-1.amazonaws.com/ap-southeast-1_VLlAOfNlC/.well-known/jwks.json
    // NOTE: CustomJwtDecoder still exists as a @Component bean and requires JWT_SIGNER_KEY
    //       to remain in the environment — remove the @Component annotation from that class
    //       once the old local-auth flow is fully decommissioned.

    // NEW: Inject the JIT user-provisioning filter (runs after JWT auth).
    @Autowired
    private CognitoUserSyncService cognitoUserSyncService;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private JwtAuthenticationEntryPoint jwtAuthenticationEntryPoint;

    @Autowired
    private CognitoAccessDeniedHandler cognitoAccessDeniedHandler;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
        httpSecurity
                // Trỏ vào CorsConfigurationSource bean bên dưới để Spring Security
                // xử lý CORS đúng bên trong filter chain của nó
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint(jwtAuthenticationEntryPoint)
                        .accessDeniedHandler(cognitoAccessDeniedHandler)
                )
                .authorizeHttpRequests(request ->
                        request.requestMatchers(PUBLIC_ENDPOINTS).permitAll()
                                // Cho phép toàn bộ OPTIONS preflight đi qua mà không cần auth
                                .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                                .anyRequest().authenticated());

        httpSecurity.oauth2ResourceServer(oauth2 ->
                oauth2.jwt(jwtConfigurer ->
                        // OLD:
                        // jwtConfigurer.decoder(customJwtDecoder)
                        //              .jwtAuthenticationConverter(jwtAuthenticationConverter())
                        //
                        // NEW: No explicit decoder — Spring Boot auto-wires NimbusJwtDecoder
                        // from application.yml issuer-uri (fetches Cognito JWKS, validates RS256).
                        jwtConfigurer.jwtAuthenticationConverter(jwtAuthenticationConverter()))
        );

        httpSecurity.csrf(AbstractHttpConfigurer::disable);

        return httpSecurity.build();
    }

    @Bean
    public JwtDecoder jwtDecoder() {
        NimbusJwtDecoder jwtDecoder = NimbusJwtDecoder.withIssuerLocation(issuerUri).build();

        OAuth2TokenValidator<Jwt> withIssuer = JwtValidators.createDefaultWithIssuer(issuerUri);
        OAuth2TokenValidator<Jwt> withClientId = new JwtClaimValidator<String>(
                "client_id",
                clientId -> cognitoAppClientId.equals(clientId)
        );
        OAuth2TokenValidator<Jwt> withTokenUse = new JwtClaimValidator<String>(
                "token_use",
                tokenUse -> "access".equals(tokenUse)
        );

        jwtDecoder.setJwtValidator(new DelegatingOAuth2TokenValidator<>(withIssuer, withClientId, withTokenUse));
        return jwtDecoder;
    }

    /**
     * CorsConfigurationSource Bean:
     * - Được Spring Security dùng trong filter chain nội bộ (.cors(cors -> cors.configurationSource(...)))
     * - Được FilterRegistrationBean bên dưới tái sử dụng để chạy trước Spring Security
     *
     * allowedOriginPatterns("*") + allowCredentials(true) là cú pháp bắt buộc trong Spring Boot 3.
     * allowedOrigins("*") sẽ lỗi khi kết hợp với allowCredentials(true).
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration config = new CorsConfiguration();
        config.setAllowedOriginPatterns(List.of("*"));
        config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS", "HEAD"));
        config.setAllowedHeaders(List.of("*"));
        config.setExposedHeaders(List.of("Authorization", "Content-Disposition"));
        config.setAllowCredentials(true);
        config.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);
        return source;
    }

    /**
     * FilterRegistrationBean chạy tại HIGHEST_PRECEDENCE (trước cả Spring Security).
     * Đảm bảo OPTIONS preflight luôn nhận được CORS headers ngay cả khi
     * Spring Security chưa kịp load. Tái sử dụng cùng config với bean bên trên.
     */
    @Bean
    public FilterRegistrationBean<CorsFilter> corsFilterRegistrationBean() {
        FilterRegistrationBean<CorsFilter> bean =
                new FilterRegistrationBean<>(new CorsFilter(corsConfigurationSource()));
        bean.setOrder(Ordered.HIGHEST_PRECEDENCE);
        return bean;
    }

    // ── OLD jwtAuthenticationConverter (reads local "role" claim, no ROLE_ prefix) ──
    // @Bean
    // JwtAuthenticationConverter jwtAuthenticationConverter() {
    //     JwtGrantedAuthoritiesConverter jwtGrantedAuthoritiesConverter = new JwtGrantedAuthoritiesConverter();
    //     jwtGrantedAuthoritiesConverter.setAuthorityPrefix("");
    //     jwtGrantedAuthoritiesConverter.setAuthoritiesClaimName("role");
    //
    //     JwtAuthenticationConverter jwtAuthenticationConverter = new JwtAuthenticationConverter();
    //     jwtAuthenticationConverter.setJwtGrantedAuthoritiesConverter(jwtGrantedAuthoritiesConverter);
    //     return jwtAuthenticationConverter;
    // }

    /**
     * NEW: Reads "custom:role" from the Cognito access token and maps it to a
     * Spring Security GrantedAuthority with the ROLE_ prefix so that all existing
     * {@code @PreAuthorize("hasRole('ADMIN')")} / hasRole('TEACHER') / hasRole('STUDENT')
     * annotations continue to work without modification.
     *
     * Mapping:
     *   ADMIN   → ROLE_ADMIN
     *   TEACHER → ROLE_TEACHER
     *   STUDENT → ROLE_STUDENT  (default for unknown values)
     *
     * IMPORTANT: Cognito adds custom attributes to the id_token by default.
     * To include "custom:role" in the access_token (which this resource server
     * validates), configure a Pre Token Generation Lambda v2 trigger in the
     * Cognito User Pool that copies the attribute into the access token claims.
     */
    @Bean
    JwtAuthenticationConverter jwtAuthenticationConverter() {
        JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
        converter.setJwtGrantedAuthoritiesConverter((Jwt jwt) -> {
            String cognitoRole = jwt.getClaimAsString("custom:role");
            String sub = jwt.getSubject();
            String username = jwt.getClaimAsString("cognito:username");
            if (username == null || username.isBlank()) {
                username = jwt.getClaimAsString("username");
            }
            String email = jwt.getClaimAsString("email");

            if (sub != null && !sub.isBlank()) {
                cognitoUserSyncService.syncUser(sub, username, email, cognitoRole);
            }

            User localUser = null;
            if (sub != null && !sub.isBlank()) {
                localUser = userRepository.findByCognitoSub(sub).orElse(null);
            }
            if (localUser == null && username != null && !username.isBlank()) {
                localUser = userRepository.findByUsername(username).orElse(null);
            }
            if (localUser == null && email != null && !email.isBlank()) {
                localUser = userRepository.findByEmail(email).orElse(null);
            }

            if (localUser == null || localUser.getRole() == null || localUser.getRole().getRoleName() == null) {
                return List.of();
            }

            String springRole = "ROLE_" + localUser.getRole().getRoleName().trim().toUpperCase(Locale.ROOT);
            return List.<GrantedAuthority>of(new SimpleGrantedAuthority(springRole));
        });
        return converter;
    }
}
