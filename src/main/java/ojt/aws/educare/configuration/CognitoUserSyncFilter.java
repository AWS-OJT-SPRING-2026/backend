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
