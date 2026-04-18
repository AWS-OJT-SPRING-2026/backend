package ojt.aws.educare.configuration;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import ojt.aws.educare.entity.Role;
import ojt.aws.educare.entity.User;
import ojt.aws.educare.repository.RoleRepository;
import ojt.aws.educare.repository.UserRepository;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class CognitoUserSyncService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    @Transactional
    public void syncUser(String cognitoSub, String username, String email, String cognitoRole) {
        if (cognitoSub == null || cognitoSub.isBlank()) {
            log.warn("[CognitoSync] Missing sub claim — skip sync");
            return;
        }

        // ── 1. Fast path: already linked by cognitoSub ────────────────────────
        Optional<User> bySubOpt = userRepository.findByCognitoSub(cognitoSub);
        if (bySubOpt.isPresent()) {
            User user = bySubOpt.get();
            updateRoleIfChanged(user, cognitoRole);
            return;
        }

        // ── 2. Migration path: existing account with same username ────────────
        if (username != null && !username.isBlank()) {
            Optional<User> byUsernameOpt = userRepository.findByUsername(username);
            if (byUsernameOpt.isPresent()) {
                User user = byUsernameOpt.get();
                user.setCognitoSub(cognitoSub);
                if (email != null && !email.isBlank() && !email.equalsIgnoreCase(user.getEmail())) {
                    user.setEmail(email);
                }
                updateRoleIfChanged(user, cognitoRole);
                userRepository.save(user);
                log.info("[CognitoSync] Linked username {} to Cognito sub {}", username, cognitoSub);
                return;
            }
        }

        // ── 3. Migration path: existing account with same email ───────────────
        if (email != null && !email.isBlank()) {
            Optional<User> byEmailOpt = userRepository.findByEmail(email);
            if (byEmailOpt.isPresent()) {
                User user = byEmailOpt.get();
            // Attach Cognito sub so future lookups hit path 1.
            user.setCognitoSub(cognitoSub);
                if (username != null && !username.isBlank() && !username.equalsIgnoreCase(user.getUsername())) {
                    user.setUsername(username);
                }
            updateRoleIfChanged(user, cognitoRole);
            userRepository.save(user);
            log.info("[CognitoSync] Linked existing account {} to Cognito sub {}", email, cognitoSub);
            return;
            }
        }

        // ── 4. JIT provisioning: brand-new Cognito user ───────────────────────
        Role role = resolveRole(cognitoRole);
        if (role == null) {
            for (String fallback : List.of("STUDENT", "TEACHER", "ADMIN")) {
                role = roleRepository.findByRoleName(fallback).orElse(null);
                if (role != null) {
                    log.warn("[CognitoSync] Resolved role '{}' not in DB; provisioning sub={} with fallback role {}",
                            cognitoRole, cognitoSub, fallback);
                    break;
                }
            }
        }
        if (role == null) {
            log.error("[CognitoSync] No roles exist in DB — cannot provision user sub={}", cognitoSub);
            return;
        }

        String localUsername =
                (username != null && !username.isBlank())
                        ? username
                        : (email != null && !email.isBlank() ? email : cognitoSub);
        String localEmail = (email != null && !email.isBlank()) ? email : localUsername + "@local.cognito";
        String localFullName = (username != null && !username.isBlank()) ? username : localUsername;

        User newUser = User.builder()
                .username(localUsername)
                .password("COGNITO_USER")         // sentinel; never used for local auth
                .fullName(localFullName)          // overwritten when user fills in profile
                .email(localEmail)
                .phone("")                        // required NOT NULL; collected later via profile
                .status("ACTIVE")
                .cognitoSub(cognitoSub)
                .role(role)
                .build();

        userRepository.save(newUser);
        log.info("[CognitoSync] Provisioned new user {} with role {}", localUsername, role.getRoleName());
    }

    // ── Private helpers ───────────────────────────────────────────────────────

    private void updateRoleIfChanged(User user, String cognitoRole) {
        if (cognitoRole == null) return;
        Role newRole = resolveRole(cognitoRole);
        if (newRole == null) return;
        if (!newRole.getRoleID().equals(user.getRole().getRoleID())) {
            log.info("[CognitoSync] Role change for {}: {} → {}",
                    user.getEmail(), user.getRole().getRoleName(), newRole.getRoleName());
            user.setRole(newRole);
            userRepository.save(user);
        }
    }

    private Role resolveRole(String cognitoRole) {
        String roleName = "STUDENT"; // safe default
        if (cognitoRole != null) {
            roleName = switch (cognitoRole.trim().toUpperCase()) {
                case "ADMIN"   -> "ADMIN";
                case "TEACHER" -> "TEACHER";
                default        -> "STUDENT";
            };
        }
        Optional<Role> found = roleRepository.findByRoleName(roleName);
        if (found.isEmpty()) {
            log.warn("[CognitoSync] Role '{}' not found in roles table", roleName);
        }
        return found.orElse(null);
    }
}
