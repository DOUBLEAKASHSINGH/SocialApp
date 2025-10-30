/*
 * Class name
 *	RequestServiceImpl
 *
 * Version info
 *	JavaSE-11
 *
 * Copyright notice
 *
 * Author info
 *	Name: Sharad Jain
 *	Email-ID: sharad.jain@nagarro.com
 *
 * Creation date
 * 	31-05-2023
 *
 * Last updated By
 * 	Sharad Jain
 *
 * Last updated Date
 * 	12-06-2023
 *
 * Description
 * 	This is a service class for review request services.
 */

package ac.in.iiitd.fcs29.service.impl;

import ac.in.iiitd.fcs29.constant.Constants;
import ac.in.iiitd.fcs29.dto.OtpDto;
import ac.in.iiitd.fcs29.dto.login.UserVerificationDto;
import ac.in.iiitd.fcs29.dto.enums.VerificationPurpose;
import ac.in.iiitd.fcs29.entity.User;
import ac.in.iiitd.fcs29.entity.UserVerification;
import ac.in.iiitd.fcs29.repository.UserVerificationRepository;
import ac.in.iiitd.fcs29.service.EmailService;
import ac.in.iiitd.fcs29.service.UserVerificationService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

/**
 * @author sharadjain
 */
@Service
public class UserVerificationServiceImpl implements UserVerificationService {

    private final UserVerificationRepository userVerifyRepo;
    private final EmailService emailService;

    private final ModelMapper modelMapper;
    private final PasswordEncoder passwordEncoder;

    private final Map<String, UserVerification> emailOtpStore = new ConcurrentHashMap<>();

    public UserVerificationServiceImpl(UserVerificationRepository userVerifyRepo, ModelMapper modelMapper,
                                       PasswordEncoder passwordEncoder, EmailService emailService) {
        this.userVerifyRepo = userVerifyRepo;
        this.modelMapper = modelMapper;
        this.passwordEncoder = passwordEncoder;
        this.emailService = emailService;
    }

    /**
     * See in interface.
     */
    @Override
    public UserVerificationDto getUserVerifyById(String id) {
        Optional<UserVerification> userVerify = userVerifyRepo.findById(UUID.fromString(id));
        if (userVerify.isPresent()) {
            return modelMapper.map(userVerify.get(), UserVerificationDto.class);
        }
        throw new EntityNotFoundException("User verification does not exist with otp = " + id);
    }

    @Override
    public List<UserVerificationDto> getAllCodes() {
        return userVerifyRepo.findAll().stream()
                .map(request -> modelMapper.map(request, UserVerificationDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public String generateVerificationCode(User user, VerificationPurpose purpose) {
        UserVerification code = new UserVerification();
        code.setUser(user);
        code.setPurpose(purpose);
        String otp = generateOtpCode();
        code.setVerificationCode(passwordEncoder.encode(otp));
//        System.out.println("OTP Generated: " + otp);
//        System.out.println("OTP Generated Encoded: " + code.getVerificationCode());
        // 3 minutes + 30 seconds extra
        Duration duration = Duration.ofSeconds(Constants.OTP_DURATION_SEC);
        code.setExpireAt(LocalDateTime.now().plus(duration));
        UserVerification otpToken = userVerifyRepo.saveAndFlush(code);
        emailService.sendOtpEmail(user.getEmail(), otp);
        return otpToken.getId().toString();
    }

    @Override
    public String generateEmailVerificationCode(String email, VerificationPurpose purpose) {
        UserVerification code = new UserVerification();
        User user = new User();
        user.setEmail(email);
        code.setUser(user);
        code.setPurpose(purpose);
        String otp = generateOtpCode();
        code.setVerificationCode(passwordEncoder.encode(otp));
        // 3 minutes + 30 seconds extra
        Duration duration = Duration.ofSeconds(Constants.OTP_DURATION_SEC);
        code.setExpireAt(LocalDateTime.now().plus(duration));
        emailOtpStore.put(email, code);
        emailService.sendOtpEmail(email, otp);
//        System.out.println("OTP Generated: " + otp);
//        System.out.println("OTP Generated Encoded: " + code.getVerificationCode());
//        System.out.println("Email OTP Stored: " + emailOtpStore.get(email).getVerificationCode());
        return otp;
    }

    @Override
    public Boolean verifyCode(User user, OtpDto otp) {
        String otpCode = passwordEncoder.encode(otp.getOtp());
//        System.out.println("verify otp code: " + otp.getOtp());
//        System.out.println("verify otp code encoded: " + otpCode);
        List<UserVerification> userVerifyOpt = userVerifyRepo.findByUser(user);
        if (userVerifyOpt.isEmpty()) return false;
        for (UserVerification userVerify : userVerifyOpt) {
            if (passwordEncoder.matches(otp.getOtp(), userVerify.getVerificationCode()) &&
                    userVerify.getPurpose() == otp.getPurpose()) {
                // otp matches
                if (isCodeValid(userVerify)) {
                    userVerifyRepo.saveAndFlush(userVerify);
                    return true;
                }
            }
        }
        return false;
    }

    @Override
    public Boolean verifyEmailCode(String email, OtpDto otp) {
        UserVerification userVerification = emailOtpStore.get(email);
//        System.out.println("verify email code: " + otp.getOtp() + " for email: " + email);
        if (userVerification == null) return false;
//        System.out.println("Verify code:" + userVerification.getVerificationCode() + " matches: " +
//                passwordEncoder.matches(otp.getOtp(), userVerification.getVerificationCode()));
//        System.out.println("Verify code: " + userVerification.getPurpose() + " matches: " + otp.getPurpose() + " match: " +
//                (userVerification.getPurpose() == otp.getPurpose()));
        if (passwordEncoder.matches(otp.getOtp(), userVerification.getVerificationCode()) &&
                userVerification.getPurpose() == otp.getPurpose()) {
//            System.out.println("Code Matches");
            // otp matches
            if (isCodeValid(userVerification)) {
                // otp is valid
                emailOtpStore.remove(email);
                return true;
            }
        }
        return false;
    }

    private boolean isCodeValid(UserVerification userVerification) {
        Duration duration = Duration.ofMinutes(Constants.OTP_DURATION_MIN);
        LocalDateTime now = LocalDateTime.now();
        if (!userVerification.getIsExpired() && !now.isAfter(userVerification.getExpireAt())) {
            // otp not expired
            System.out.println("Code Not Expired");
//            LocalDateTime createdTime = userVerification.getCreatedAt();
//            if (Duration.between(createdTime, LocalDateTime.now()).compareTo(duration) <= 0) {
//                System.out.println("Code Not Expired-2");
//                // otp not expired (double check with creation time)
//                // Code is still valid. Set it as expired in database.
//                userVerification.setIsExpired(true);
//                return true;
//            }
            return true;
        }
        System.out.println("Code Expired");
        return false;
    }

    /**
     * @param id - ID
     */
    @Override
    public void deleteById(UUID id) {
        if (userVerifyRepo.existsById(id)) userVerifyRepo.deleteById(id);
        else throw new NoSuchElementException("Request does not exist with id = " + id);
    }

    /**
     * Generates a 6-digit random OTP (One Time Password) code.
     *
     * @return A randomly generated 6-digit OTP code as a String.
     */
    private String generateOtpCode() {
        // Thread safe random
        SecureRandom random = new SecureRandom();
        StringBuilder otp = new StringBuilder();
        for (int i = 0; i < Constants.OTP_LENGTH; i++) {
            otp.append(random.nextInt(10));
        }
        return otp.toString();
    }

    /**
     * Clears all expired verification codes from the database.
     */
    @Override
    @Scheduled(cron = "0 0 * * * *")
//    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void clearExpiredTokens() {
        System.out.println("Clearing expired user verification tokens.");
        userVerifyRepo.deleteAllByExpireAtBefore(LocalDateTime.now());
    }

    @Scheduled(cron = "0 */10 * * * *")
    public void cleanExpiredEmailTokens() {
        System.out.println("Clearing expired email user verification tokens.");
        emailOtpStore.entrySet().removeIf(entry -> {
            UserVerification userVerification = entry.getValue();
            return !isCodeValid(userVerification);
        });
    }
}
