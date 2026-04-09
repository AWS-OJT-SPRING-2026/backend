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

/**
 * CognitoUserSyncService — Just-In-Time (JIT) user provisioning for Cognito identities.
 *
 * <p>Called by {@link CognitoUserSyncFilter} on every authenticated request.
 * Responsibilities:
 * <ul>
 *   <li>Detect whether the Cognito user already has a local account.</li>
 *   <li>Create a new {@link User} row on first login (JIT provisioning).</li>
 *   <li>Link existing email-based accounts to their Cognito subject (sub).</li>
 *   <li>Keep the local role in sync with {@code custom:role} from Cognito.</li>
 *   <li>Prevent duplicate inserts via unique-constraint-safe checks.</li>
 * </ul>
 *
 * <p>Lookup order:
 * <ol>
 *   <li>By {@code cognitoSub} — fastest path for returning users.</li>
 *   <li>By {@code username} — keeps username-login accounts compatible.</li>
 *   <li>By {@code email} — fallback when email claim exists.</li>
 *   <li>Create new — first-time Cognito login, no pre-existing account.</li>
 * </ol>
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CognitoUserSyncService {

    private final UserRepository userRepository;
    private final RoleRepository roleRepository;

    /**
     * Synchronises a Cognito identity with the local {@code users} table.
     *
     * @param cognitoSub the Cognito {@code sub} claim — stable UUID per user
     * @param username   preferred username extracted from Cognito claims (if present)
     * @param email      email address from Cognito claims (can be null for username login)
     * @param cognitoRole value of the {@code custom:role} claim (ADMIN | TEACHER | STUDENT | null)
     */
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
            // Last-resort: try every role name in descending privilege so the user
            // at least gets a valid row. Without a row, CurrentUserProvider throws
            // USER_NOT_FOUND which maps to HTTP 401 — worse than a wrong role.
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

    /**
     * Updates the user's role if the Cognito role differs from the stored one.
     * Saves only when a change is detected to avoid unnecessary dirty writes.
     */
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

    /**
     * Maps a Cognito role string to the matching {@link Role} entity.
     * Cognito value (case-insensitive) → DB roleName:
     *   ADMIN   → ADMIN
     *   TEACHER → TEACHER
     *   STUDENT → STUDENT
     *   default → STUDENT
     */
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
