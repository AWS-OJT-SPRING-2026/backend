package ojt.aws.educare.configuration;

import lombok.RequiredArgsConstructor;
import ojt.aws.educare.entity.User;
import ojt.aws.educare.exception.AppException;
import ojt.aws.educare.exception.ErrorCode;
import ojt.aws.educare.repository.UserRepository;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null) {
            throw new AppException(ErrorCode.USER_NOT_FOUND);
        }

        Jwt jwt = extractJwt(authentication.getPrincipal());
        if (jwt != null) {
            String sub = jwt.getSubject();
            if (sub != null && !sub.isBlank()) {
                var bySub = userRepository.findByCognitoSub(sub);
                if (bySub.isPresent()) {
                    return bySub.get();
                }
            }

            String cognitoUsername = jwt.getClaimAsString("cognito:username");
            if (cognitoUsername == null || cognitoUsername.isBlank()) {
                cognitoUsername = jwt.getClaimAsString("username");
            }
            if (cognitoUsername != null && !cognitoUsername.isBlank()) {
                var byUsername = userRepository.findByUsername(cognitoUsername);
                if (byUsername.isPresent()) {
                    return byUsername.get();
                }
            }

            String email = jwt.getClaimAsString("email");
            if (email != null && !email.isBlank()) {
                var byEmail = userRepository.findByEmail(email);
                if (byEmail.isPresent()) {
                    return byEmail.get();
                }
            }
        }

        String principalName = authentication.getName();
        if (principalName != null && !principalName.isBlank()) {
            var byUsername = userRepository.findByUsername(principalName);
            if (byUsername.isPresent()) {
                return byUsername.get();
            }

            var byEmail = userRepository.findByEmail(principalName);
            if (byEmail.isPresent()) {
                return byEmail.get();
            }

            var bySub = userRepository.findByCognitoSub(principalName);
            if (bySub.isPresent()) {
                return bySub.get();
            }
        }

        throw new AppException(ErrorCode.USER_NOT_FOUND);
    }

    private Jwt extractJwt(Object principal) {
        if (principal instanceof Jwt jwt) {
            return jwt;
        }
        return null;
    }
}

