/*
 * Interface name
 *	UserService
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
 * 	29-05-2023
 *
 * Last updated By
 * 	Sharad Jain
 *
 * Last updated Date
 * 	02-06-2023
 *
 * Description
 * 	This interface defines working of user related services.
 */

package ac.in.iiitd.fcs29.service;

import ac.in.iiitd.fcs29.dto.*;
import ac.in.iiitd.fcs29.dto.enums.VerificationPurpose;
import ac.in.iiitd.fcs29.dto.login.AuthRequestDto;
import ac.in.iiitd.fcs29.dto.login.AuthResponseDto;
import ac.in.iiitd.fcs29.dto.login.UserDto;
import ac.in.iiitd.fcs29.dto.user.*;
import ac.in.iiitd.fcs29.entity.User;

import javax.security.auth.login.AccountLockedException;
import java.util.List;

/**
 * @author sharadjain
 */
public interface UserService {

    /**
     * Method to get list of all users in database.
     *
     * @return - list of users
     */
    List<UserResponseDto> getAllUsers();

    String sendOtp(User auth, VerificationPurpose purpose);

    String sendOtp(String email, VerificationPurpose purpose);

    String sendEmailOtp(String email, VerificationPurpose purpose);

    boolean verifyOtp(String email, OtpDto otp);

    boolean verifyOtp(User auth, OtpDto otp);

    /**
     * Method to check user login.
     *
     * @param user
     * @return
     */
    AuthResponseDto login(AuthRequestDto user) throws AccountLockedException;

    PageResponseDto<UserResponseDto> getAllUsers(String email, int page, int pageSize,
                                                 String[] sortBy, boolean sortDesc);

    void verifyUserWithJWT(String userID, String jwt);

    /**
     * Method to get user by id.
     *
     * @param id
     * @return - user data
     */
    UserResponseDto getUserById(String id);

    AuthResponseDto loginOtp(String jwt, OtpDto otp);

    void resetPassword(String email, String newPassword, OtpDto otp, boolean isAdminRequest);

    /**
     * Method to add new user to database.
     *
     * @param user
     * @return - added user
     */
    AuthResponseDto addUser(UserDto user, boolean isAdminRequest);

    UserResponseDto editUser(UserResponseDto userDto, boolean isAdminRequest);

    UserResponseDto updateUserLock(UserDto userDto, boolean lock, String adminId);

    UserResponseDto verifyUser(UserDto user, boolean isVerified, String adminId);

    boolean updateProfilePicture(ProfilePictureRequestDto profilePictureRequestDto);

    ProfilePictureResponseDto getProfilePicture(String email);

    boolean addUserDocument(UserDocumentRequestDto userDocumentRequestDto);

    UserDocumentResponseDto getUserDocument(String id);

    PageResponseDto<UserResponseDto> searchUsers(UserSearchDto userSearchDto, int page, int pageSize);

    /**
     * Method to delete user from database.
     *
     * @param user - user to be deleted
     * @param adminId - admin id
     */
    void deleteUser(UserResponseDto user, String adminId);

    /**
     * Method to count number of users.
     */
    long count();
}
