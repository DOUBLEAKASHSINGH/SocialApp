package ac.in.iiitd.fcs29.service;

public interface EmailService {
    void sendOtpEmail(String toEmail, String otp);
}
