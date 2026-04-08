package ojt.aws.educare.configuration;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import ojt.aws.educare.dto.response.ApiResponse;
import ojt.aws.educare.exception.ErrorCode;
import org.springframework.http.MediaType;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.web.AuthenticationEntryPoint;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationEntryPoint implements AuthenticationEntryPoint {

    private static final String SESSION_EXPIRED_MESSAGE = "Phiên làm việc của bạn đã hết hạn. Vui lòng đăng nhập lại.";

    private final ObjectMapper objectMapper;

    @Override
    public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException)
            throws IOException {
        ErrorCode errorCode = ErrorCode.UNAUTHENTICATED;
        String message = isExpiredToken(authException)
                ? SESSION_EXPIRED_MESSAGE
                : errorCode.getMessage();

        response.setStatus(errorCode.getStatusCode().value());
        response.setContentType(MediaType.APPLICATION_JSON_VALUE);
        response.setCharacterEncoding("UTF-8");

        ApiResponse<?> apiResponse = ApiResponse.builder()
                .code(errorCode.getCode())
                .message(message)
                .build();

        response.getWriter().write(objectMapper.writeValueAsString(apiResponse));
        response.flushBuffer();
    }

    private boolean isExpiredToken(AuthenticationException authException) {
        Throwable cursor = authException;
        while (cursor != null) {
            String message = cursor.getMessage();
            if (message != null) {
                String normalized = message.toLowerCase();
                if (normalized.contains("expired") || normalized.contains("jwt expired")) {
                    return true;
                }
            }
            cursor = cursor.getCause();
        }
        return false;
    }
}
