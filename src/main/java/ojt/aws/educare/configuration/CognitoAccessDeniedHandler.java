package ojt.aws.educare.configuration;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.entity.User;
import ojt.aws.educare.exception.ErrorCode;
import ojt.aws.educare.repository.UserRepository;
import org.springframework.http.MediaType;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
@RequiredArgsConstructor
public class CognitoAccessDeniedHandler implements AccessDeniedHandler {

    private final UserRepository userRepository;
    private final ObjectMapper objectMapper;

    @Override
    public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException)
            throws IOException {
        ErrorCode errorCode = resolveErrorCode();

        response.setStatus(errorCode.getStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(errorCode.getMessage())
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        response.flushBuffer();
    }

    private ErrorCode resolveErrorCode() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !(authentication.getPrincipal() instanceof Jwt jwt)) {
            return ErrorCode.UNAUTHORIZED;
        }

        Optional<User> userOpt = findUser(jwt);
        if (userOpt.isPresent() && "LOCKED".equalsIgnoreCase(userOpt.get().getStatus())) {
            return ErrorCode.USER_INACTIVE;
        }

        return ErrorCode.UNAUTHORIZED;
    }

    private Optional<User> findUser(Jwt jwt) {
        String sub = jwt.getSubject();
        if (sub != null && !sub.isBlank()) {
            Optional<User> bySub = userRepository.findByCognitoSub(sub);
            if (bySub.isPresent()) {
                return bySub;
            }
        }

        String username = jwt.getClaimAsString("cognito:username");
        if (username == null || username.isBlank()) {
            username = jwt.getClaimAsString("username");
        }
        if (username != null && !username.isBlank()) {
            Optional<User> byUsername = userRepository.findByUsername(username);
            if (byUsername.isPresent()) {
                return byUsername;
            }
        }

        String email = jwt.getClaimAsString("email");
        if (email != null && !email.isBlank()) {
            return userRepository.findByEmail(email);
        }

        return Optional.empty();
    }
}


