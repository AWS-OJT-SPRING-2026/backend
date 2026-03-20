package ojt.aws.educare.service;

public interface EmailService {
    void sendOtpEmail(String toEmail, String otpCode, String fullName);
}