package ojt.aws.educare.service.Impl;

import jakarta.mail.MessagingException;
import jakarta.mail.internet.MimeMessage;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import ojt.aws.educare.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
@FieldDefaults(level = AccessLevel.PRIVATE)
public class EmailServiceImpl implements EmailService {

    final JavaMailSender mailSender;

    @Value("${spring.mail.username}")
    String fromEmail;

    @Override
    public void sendOtpEmail(String toEmail, String otpCode, String fullName) {
        try {
            MimeMessage message = mailSender.createMimeMessage();
            MimeMessageHelper helper = new MimeMessageHelper(message, true, "UTF-8");

            helper.setFrom(fromEmail);
            helper.setTo(toEmail);
            helper.setSubject("Mã xác nhận khôi phục mật khẩu - EduCare");

            // Tạo form HTML cho Email nhìn chuyên nghiệp
            String htmlContent = "<div style='font-family: Arial, sans-serif; padding: 20px; color: #333;'>"
                    + "<h2>Xin chào " + fullName + ",</h2>"
                    + "<p>Bạn vừa yêu cầu khôi phục mật khẩu tại hệ thống <b>EduCare</b>.</p>"
                    + "<p>Mã xác nhận (OTP) của bạn là: <strong style='font-size: 24px; color: #d9534f;'>" + otpCode + "</strong></p>"
                    + "<p><i>Mã này sẽ hết hạn sau 1 phút 30 giây. Vui lòng không chia sẻ mã này cho bất kỳ ai.</i></p>"
                    + "<hr>"
                    + "<p style='font-size: 12px; color: #777;'>Nếu bạn không yêu cầu thay đổi mật khẩu, vui lòng bỏ qua email này.</p>"
                    + "</div>";

            helper.setText(htmlContent, true);

            mailSender.send(message);
        } catch (MessagingException e) {
            throw new RuntimeException("Lỗi khi gửi email: " + e.getMessage());
        }
    }
}