package ojt.aws.educare.controller;

import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.repository.UserRepository;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.LinkedHashMap;
import java.util.Map;

// OLD imports (kept for reference — remove once old methods are decommissioned):
// import com.nimbusds.jose.JOSEException;
// import ojt.aws.educare.dto.request.IntrospectRequest;
// import ojt.aws.educare.dto.request.LoginRequest;
// import ojt.aws.educare.dto.request.LogoutRequest;
// import ojt.aws.educare.dto.response.AuthenticationResponse;
// import ojt.aws.educare.dto.response.IntrospectResponse;
// import org.springframework.web.bind.annotation.PostMapping;
// import org.springframework.web.bind.annotation.RequestBody;
// import java.text.ParseException;

@RestController
@RequestMapping("/auth")
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE, makeFinal = true)
public class AuthenticationController {
    UserRepository userRepository;


    // ── OLD local-auth endpoints (commented — now handled by Cognito Hosted UI) ──

    // @PostMapping("/login")
    // ApiResponse<AuthenticationResponse> login(@RequestBody LoginRequest request) {
    //     var result = authenticationService.authenticate(request);
    //     return ApiResponse.<AuthenticationResponse>builder()
    //             .result(result)
    //             .build();
    // }

    // @PostMapping("/introspect")
    // ApiResponse<IntrospectResponse> introspect(@RequestBody IntrospectRequest request)
    //         throws ParseException, JOSEException {
    //     var result = authenticationService.introspect(request);
    //     return ApiResponse.<IntrospectResponse>builder()
    //             .result(result)
    //             .build();
    // }

    // @PostMapping("/logout")
    // ApiResponse<Void> logout(@RequestBody LogoutRequest request)
    //         throws ParseException, JOSEException {
    //     authenticationService.logout(request);
    //     return ApiResponse.<Void>builder()
    //             .build();
    // }

    // ── NEW: Cognito authenticated-user info endpoint ─────────────────────────
    /**
     * GET /auth/me
     *
     * Returns the authenticated Cognito user's identity claims extracted directly
     * from the validated JWT.  No database lookup required — the resource server
     * has already verified the token signature using Cognito's JWKS.
     *
     * Claims read:
     *   - sub          → Cognito user identifier (UUID)
     *   - email        → user's email address
     *   - custom:role  → application role (ADMIN | TEACHER | STUDENT)
     *
     * Security: endpoint is protected by default (not in PUBLIC_ENDPOINTS).
     * A valid Cognito access token must be sent as Authorization: Bearer <token>.
     */
    @GetMapping("/me")
    ApiResponse<Map<String, Object>> me(@AuthenticationPrincipal Jwt jwt) {
        Map<String, Object> claims = new LinkedHashMap<>();
        claims.put("sub",         jwt.getSubject());
        claims.put("email",       jwt.getClaimAsString("email"));
        claims.put("custom:role", jwt.getClaimAsString("custom:role"));
        // Optional convenience fields — present in id_token, may be absent in access_token.
        claims.put("name",        jwt.getClaimAsString("name"));
        claims.put("username",    jwt.getClaimAsString("cognito:username"));

        return ApiResponse.<Map<String, Object>>builder()
                .result(claims)
                .build();
    }

    @GetMapping("/account-status")
    ApiResponse<Map<String, Object>> accountStatus(@RequestParam("identifier") String identifier) {
        var normalized = identifier == null ? "" : identifier.trim();

        var userOpt = userRepository.findByUsername(normalized);
        if (userOpt.isEmpty()) {
            userOpt = userRepository.findByEmail(normalized);
        }

        boolean locked = userOpt
                .map(user -> "LOCKED".equalsIgnoreCase(user.getStatus()))
                .orElse(false);

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("locked", locked);

        return ApiResponse.<Map<String, Object>>builder()
                .result(result)
                .build();
    }
}
