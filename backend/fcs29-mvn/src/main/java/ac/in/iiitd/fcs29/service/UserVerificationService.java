/*
 * Interface name
 *	RequestService
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
 * 	11-06-2023
 *
 * Description
 * 	This interface defines working of product related services.
 */

package ac.in.iiitd.fcs29.service;

import ac.in.iiitd.fcs29.dto.OtpDto;
import ac.in.iiitd.fcs29.dto.login.UserVerificationDto;
import ac.in.iiitd.fcs29.dto.enums.VerificationPurpose;
import ac.in.iiitd.fcs29.entity.User;

import java.util.List;
import java.util.UUID;

/**
 * @author sharadjain
 */
public interface UserVerificationService {

    UserVerificationDto getUserVerifyById(String id);

    /**
     * Method to get list of all codes in database.
     *
     * @return - list of codes
     */
    List<UserVerificationDto> getAllCodes();

    /**
     * Method to verify code.
     *
     * @param user
     * @param otp
     * @return - weather code is valid or not
     */
    Boolean verifyCode(User user, OtpDto otp);

    /**
     * Method to add new user to database.
     *
     * @param user
     * @return - added user
     */
    String generateVerificationCode(User user, VerificationPurpose purpose);

    String generateEmailVerificationCode(String email, VerificationPurpose purpose);

    Boolean verifyEmailCode(String email, OtpDto otp);

    /**
     * Delete all requests by product code.
     *
     * @param id
     */
    void deleteById(UUID id);

    /**
     * Method to clear expired tokens.
     */
    void clearExpiredTokens();
}
