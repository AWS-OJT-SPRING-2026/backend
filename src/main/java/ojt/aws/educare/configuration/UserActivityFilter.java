package ojt.aws.educare.configuration;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import ojt.aws.educare.service.MonitoringService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

public class UserActivityFilter extends OncePerRequestFilter {

    @Autowired
    private MonitoringService monitoringService;

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {

        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth != null && auth.isAuthenticated() && !"anonymousUser".equals(auth.getName())) {
            String userId = auth.getName();
            String method = request.getMethod();
            String path = request.getRequestURI();
            String ip = request.getRemoteAddr();
            String userAgent = request.getHeader("User-Agent");

            // Log activity
            monitoringService.trackActivity(userId, method + ":" + path, "API Call", ip, userAgent);

            // Immediate kick check
            if (monitoringService.isUserLocked(userId)) {
                System.out.println("Enforcing KICK for user: [" + userId + "] (lowercase match: " + userId.toLowerCase() + ")");
                response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                response.setContentType("application/json;charset=UTF-8");
                response.getWriter().write("{\"code\": 403, \"message\": \"Tài khoản của bạn tạm thời bị khóa 5-30 phút do hoạt động đáng ngờ hoặc đã bị Admin Kick. Vui lòng quay lại sau.\"}");
                return;
            }
        }

        filterChain.doFilter(request, response);
    }
}
