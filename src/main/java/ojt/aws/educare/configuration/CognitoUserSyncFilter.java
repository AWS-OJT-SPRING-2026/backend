package ojt.aws.educare.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationToken;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

/**
 * CognitoUserSyncFilter — JIT (Just-In-Time) user provisioning for Cognito.
 *
 * <p>Runs once per request, <em>after</em> Spring Security's
 * {@code BearerTokenAuthenticationFilter} has validated the Cognito JWT and
 * populated the {@link SecurityContextHolder}.
 *
 * <p>If the authenticated principal is a {@link JwtAuthenticationToken} (i.e.,
 * the request carries a valid Cognito access token), this filter extracts:
 * <ul>
 *   <li>{@code sub} — Cognito user UUID (stable identifier)</li>
 *   <li>{@code cognito:username | username} — username claim (if present)</li>
 *   <li>{@code email} — user's email address (optional for username login)</li>
 *   <li>{@code custom:role} — application role attribute</li>
 * </ul>
 * and delegates to {@link CognitoUserSyncService} to create or update the local
 * {@code users} row.
 *
 * <p>For public endpoints (unauthenticated requests) the filter is a no-op.
 *
 * <p><strong>Registration note:</strong> This bean is intentionally NOT registered
 * as a standalone servlet filter. It is added to the Spring Security filter chain
 * via {@code httpSecurity.addFilterAfter(...)} in {@link SecurityConfig}, and the
 * auto-registration is disabled by {@code cognitoUserSyncFilterRegistration()}.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class CognitoUserSyncFilter extends OncePerRequestFilter {

    private final CognitoUserSyncService cognitoUserSyncService;

    @Override
    protected void doFilterInternal(HttpServletRequest request,
                                    HttpServletResponse response,
                                    FilterChain filterChain)
            throws ServletException, IOException {

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();

            // Only act when the request is authenticated with a Cognito JWT.
            // Anonymous / unauthenticated requests are passed through untouched.
            if (authentication instanceof JwtAuthenticationToken jwtAuth) {
                Jwt jwt = jwtAuth.getToken();

                String sub        = jwt.getSubject();
                String username   = jwt.getClaimAsString("cognito:username");
                if (username == null || username.isBlank()) {
                    username = jwt.getClaimAsString("username");
                }
                String email      = jwt.getClaimAsString("email");
                String cognitoRole = jwt.getClaimAsString("custom:role");

                if (sub != null && !sub.isBlank()) {
                    cognitoUserSyncService.syncUser(sub, username, email, cognitoRole);
                } else {
                    log.warn("[CognitoSyncFilter] JWT missing 'sub' — skipping sync. sub={} username={} email={}", sub, username, email);
                }
            }
        } catch (Exception ex) {
            // Sync failures must NOT block the request — log and continue.
            // The user is already authenticated; a DB hiccup should not cause a 500.
            log.error("[CognitoSyncFilter] User sync failed — request will proceed anyway", ex);
        }

        filterChain.doFilter(request, response);
    }
}
