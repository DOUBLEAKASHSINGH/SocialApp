package ac.in.iiitd.fcs29.service.impl;

import ac.in.iiitd.fcs29.constant.Constants;
import ac.in.iiitd.fcs29.service.EmailService;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.stereotype.Service;


@Service
public class EmailServiceImpl implements EmailService {

    private final JavaMailSender mailSender;

    @Value("${app.mail.from}")
    private String fromEmail;

    public EmailServiceImpl(JavaMailSender mailSender) {
        this.mailSender = mailSender;
    }

    @Override
    public void sendOtpEmail(String toEmail, String otp) {
        try {
            SimpleMailMessage message = new SimpleMailMessage();
            message.setFrom(fromEmail);
            message.setTo(toEmail);
            message.setSubject("FCS Project - OTP Code");
            message.setText("Your One-Time Password (OTP) is: " + otp + "\nThis code will expire in " +
                    Constants.OTP_DURATION_MIN + " minutes.");

            mailSender.send(message);
        } catch (Exception e) {
            System.out.println("Failed to send OTP email: " + e);
            throw new RuntimeException("Failed to send OTP email", e);
        }
    }
}

