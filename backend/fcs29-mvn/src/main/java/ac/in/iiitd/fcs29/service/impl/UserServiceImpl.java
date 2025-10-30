/*
 * Class name
 *	UserServiceImpl
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
 * 	31-05-2023
 *
 * Description
 * 	This is a service class for user services.
 */

package ac.in.iiitd.fcs29.service.impl;

import ac.in.iiitd.fcs29.dto.enums.Role;
import ac.in.iiitd.fcs29.dto.enums.VerificationPurpose;
import ac.in.iiitd.fcs29.dto.login.AuthRequestDto;
import ac.in.iiitd.fcs29.dto.login.AuthResponseDto;
import ac.in.iiitd.fcs29.dto.login.UserDto;
import ac.in.iiitd.fcs29.dto.user.*;
import ac.in.iiitd.fcs29.entity.UserDocument;
import ac.in.iiitd.fcs29.constant.Constants;
import ac.in.iiitd.fcs29.dto.*;
import ac.in.iiitd.fcs29.entity.User;
import ac.in.iiitd.fcs29.repository.UserDocumentRepository;
import ac.in.iiitd.fcs29.repository.UserRepository;
import ac.in.iiitd.fcs29.service.BlacklistedTokenService;
import ac.in.iiitd.fcs29.service.JwtService;
import ac.in.iiitd.fcs29.service.UserService;
import ac.in.iiitd.fcs29.service.UserVerificationService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import javax.security.auth.login.AccountLockedException;
import java.io.IOException;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

/**
 * @author sharadjain
 */
@Service
public class UserServiceImpl implements UserService {

    private final UserRepository userRepo;

    private final UserDocumentRepository userDocumentRepo;

    private final ModelMapper modelMapper;

    private final JwtService jwtService;

    private final UserVerificationService userVerificationService;

    private final AuthenticationManager authManager;

    private final PasswordEncoder passwordEncoder;
    private final UserDetailsService userDetailsService;
    private final BlacklistedTokenService blacklistedTokenService;

    public UserServiceImpl(UserRepository userRepo, UserDocumentRepository userDocumentRepo, ModelMapper modelMapper,
                           JwtService jwtService, UserVerificationService userVerificationService,
                           AuthenticationManager authManager, PasswordEncoder passwordEncoder,
                           UserDetailsService userDetailsService, BlacklistedTokenService blacklistedTokenService) {
        this.userRepo = userRepo;
        this.userDocumentRepo = userDocumentRepo;
        this.modelMapper = modelMapper;
        this.jwtService = jwtService;
        this.userVerificationService = userVerificationService;
        this.authManager = authManager;
        this.passwordEncoder = passwordEncoder;
        this.userDetailsService = userDetailsService;
        this.blacklistedTokenService = blacklistedTokenService;
    }

    @Override
    public List<UserResponseDto> getAllUsers() {
        return userRepo.findAll().stream().map(user -> modelMapper.map(user, UserResponseDto.class))
                .collect(Collectors.toList());
    }

    @Override
    public PageResponseDto<UserResponseDto> getAllUsers(String email, int page, int pageSize,
                                                        String[] sortBy, boolean sortDesc) {
        Sort sort = Sort.unsorted();
        for (String s : sortBy) {
            if (!Constants.USER_SORT_FIELDS.contains(s)) {
                throw new IllegalArgumentException("Invalid sort field: " + s);
            }
            Sort.Direction direction = sortDesc ? Sort.Direction.DESC : Sort.Direction.ASC;
            sort = sort.and(Sort.by(direction, s));
        }
        Pageable pageable = PageRequest.of(page, pageSize, sort);
        Page<User> users = userRepo.findByEmailIsNotIgnoreCase(email, pageable);
//        Page<User> users = userRepo.findAll(pageable);
        return createPageResponseDto(pageSize, users);
    }

    @Override
    public void verifyUserWithJWT(String userID, String jwt) {
        String username = jwtService.extractUsername(jwt);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!jwtService.isAuthTokenValid(jwt, userDetails)) { // both jwt and user details have same username
            throw new IllegalArgumentException("Invalid JWT token");
        }
        if (!userID.equals(username) && userDetails.getAuthorities().stream().noneMatch(
                grantedAuthority -> grantedAuthority.getAuthority().equals("ROLE_ADMIN"))
        ) {
            throw new IllegalArgumentException("Error user's JWT ID and ID in request body don't match");
        }
    }

    /**
     * Method to clear password from object.
     *
     * @param user -
     * @return -
     */
    private UserDto clearPassword(UserDto user) {
        user.setPassword("");
        return user;
    }

    /**
     * See in interface.
     */
    @Override
    public UserResponseDto getUserById(String id) {
        Optional<User> user = userRepo.findById(id);
        if (user.isPresent()) {
            return modelMapper.map(user.get(), UserResponseDto.class);
        }
        throw new EntityNotFoundException("User does not exist with email-id = " + id);
    }

    @Override
    public String sendOtp(User auth, VerificationPurpose purpose) {
        return userVerificationService.generateVerificationCode(auth, purpose);
    }

    @Override
    public String sendOtp(String email, VerificationPurpose purpose) {
        User auth = userRepo.findById(email)
                .orElseThrow(() -> new EntityNotFoundException("User does not exist with email-id = " + email));
        return userVerificationService.generateVerificationCode(auth, purpose);
    }

    @Override
    public String sendEmailOtp(String email, VerificationPurpose purpose) {
        return userVerificationService.generateEmailVerificationCode(email, purpose);
    }

    @Override
    public boolean verifyOtp(String email, OtpDto otp) {
        User auth = userRepo.findById(email)
                .orElseThrow(() -> new EntityNotFoundException("User does not exist with email-id = " + email));
        return userVerificationService.verifyCode(auth, otp);
    }

    @Override
    public boolean verifyOtp(User auth, OtpDto otp) {
        return userVerificationService.verifyCode(auth, otp);
    }

    @Override
    @Transactional
    public AuthResponseDto login(AuthRequestDto user) throws AccountLockedException {
        // throw exception if auth fails (AuthenticationException, BadCredentialsException)
        // handles password encryption using AuthenticationProvider
        authManager.authenticate(new UsernamePasswordAuthenticationToken(user.getEmail(), user.getPassword()));
        System.out.println("Login auth success");
        // User authenticated in above statement.
        Optional<User> optUser = userRepo.findById(user.getEmail());
        if (optUser.isEmpty()) {
            throw new EntityNotFoundException("User does not exist with email-id = " + user.getEmail());
        } else if (optUser.get().getIsAccountLocked()) {
            throw new AccountLockedException("User access is blocked with email-id = " + user.getEmail());
        }
        User auth = optUser.get();
        String otpToken = userVerificationService.generateVerificationCode(auth, VerificationPurpose.USER_LOGIN);
        String token = jwtService.generateOtpJwtToken(auth, otpToken);
//        String token = jwtService.generateToken(auth);
        return new AuthResponseDto(auth.getEmail(), auth.getFirstName(), auth.getLastName(), auth.getRole(), token);
    }

    @Override
    @Transactional
    public AuthResponseDto loginOtp(String jwt, OtpDto otp) {
        String username = jwtService.extractUsername(jwt);
        UserDetails userDetails = userDetailsService.loadUserByUsername(username);
        if (!jwtService.isOtpTokenValid(jwt, userDetails)) {
            throw new IllegalArgumentException("Invalid OTP JWT token");
        }
        User user = userRepo.findById(username)
                .orElseThrow(() -> new EntityNotFoundException("User not found"));
        if (!userVerificationService.verifyCode(user, otp)) {
            throw new EntityNotFoundException("Invalid OTP");
        }
        System.out.println("Otp auth success");
        // blacklist old jwt token.
        blacklistedTokenService.addToBlacklist(jwt, jwtService.extractExpiration(jwt));
        // User authenticated in above statement. Generate new jwt token.
        Optional<User> opt_user = userRepo.findById(username);
        if (opt_user.isEmpty()) {
            throw new EntityNotFoundException("User does not exist with email-id = " + username);
        }
        User auth = opt_user.get();
        String token = jwtService.generateToken(auth);
        return new AuthResponseDto(auth.getEmail(), auth.getFirstName(), auth.getLastName(), auth.getRole(), token);
    }

    @Override
    public void resetPassword(String email, String newPassword, OtpDto otp, boolean isAdminRequest) {
        User user = userRepo.findById(email)
                .orElseThrow(() -> new EntityNotFoundException("User does not exist with email-id = " + email));
        if (!isAdminRequest &&
                otp.getPurpose() != VerificationPurpose.PASSWORD_RESET ||
                !userVerificationService.verifyEmailCode(user.getEmail(), otp)) {
            throw new IllegalArgumentException("Invalid OTP");
        }
        user.setPassword(passwordEncoder.encode(newPassword));
        userRepo.save(user);
    }

    @Override
    public AuthResponseDto addUser(UserDto user, boolean isAdminRequest) {
        if (userRepo.existsById(user.getEmail()))
            throw new EntityExistsException("User already exists with id = " + user.getEmail());

        if (!isAdminRequest &&
                (user.getOtp().getPurpose() != VerificationPurpose.USER_REGISTRATION ||
                !userVerificationService.verifyEmailCode(user.getEmail(), user.getOtp())))
            throw new IllegalArgumentException("Invalid OTP");
        User auth = modelMapper.map(user, User.class);
        if (auth.getRole() == Role.ADMIN && isAdminRequest) {
            auth.setIsVerified(true);
        }
        auth.setPassword(passwordEncoder.encode(user.getPassword()));
        userRepo.saveAndFlush(auth);
        String token = jwtService.generateToken(auth);
        return new AuthResponseDto(auth.getEmail(), auth.getFirstName(), auth.getLastName(), auth.getRole(), token);
    }

    @Override
    @Transactional
    public UserResponseDto editUser(UserResponseDto userDto, boolean isAdminRequest) {
        Optional<User> auth = userRepo.findById(userDto.getEmail());
        if (auth.isEmpty())
            throw new EntityNotFoundException("User does not exist with id = " + userDto.getEmail());
        User user = auth.get();
        user.setFirstName(userDto.getFirstName());
        user.setLastName(userDto.getLastName());
        user.setBio(userDto.getBio());
        if (isAdminRequest)
            user.setRole(userDto.getRole());

        return modelMapper.map(userRepo.saveAndFlush(user), UserResponseDto.class);
    }

    @Override
    @Transactional
    public UserResponseDto updateUserLock(UserDto userDto, boolean lock, String adminId) {
        Optional<User> user = userRepo.findById(userDto.getEmail());
        if (user.isPresent()) {
            User admin = userRepo.findById(adminId).orElseThrow(
                    () -> new EntityNotFoundException("Admin does not exist with id = " + adminId)
            );
            if (userDto.getOtp().getPurpose() != VerificationPurpose.ACCOUNT_ACTIVATION ||
                    !userVerificationService.verifyCode(admin, userDto.getOtp()))
                throw new IllegalArgumentException("Invalid OTP");
            User auth = user.get();
            auth.setIsAccountLocked(lock);
            return modelMapper.map(userRepo.save(auth), UserResponseDto.class);
        }
        throw new EntityNotFoundException("User does not exist with id = " + userDto.getEmail());
    }

    @Override
    @Transactional
    public UserResponseDto verifyUser(UserDto user, boolean isVerified, String adminId) {
        Optional<User> opt_user = userRepo.findById(user.getEmail());
        if (opt_user.isPresent()) {
            User admin = userRepo.findById(adminId).orElseThrow(
                    () -> new EntityNotFoundException("Admin does not exist with id = " + adminId)
            );
            if (user.getOtp().getPurpose() != VerificationPurpose.ACCOUNT_VERIFICATION ||
                    !userVerificationService.verifyCode(admin, user.getOtp()))
                throw new IllegalArgumentException("Invalid OTP");
            User auth = opt_user.get();
            // Do not change role if user is admin.
            if (isVerified) {
                if (user.getRole() == Role.UNVERIFIED) {
                    auth.setRole(Role.USER);
                }
            } else {
                if (user.getRole() == Role.USER) {
                    auth.setRole(Role.UNVERIFIED);
                }
            }
            auth.setIsVerified(isVerified);
            return modelMapper.map(userRepo.save(auth), UserResponseDto.class);
        }
        throw new EntityNotFoundException("User does not exist with id = " + user.getEmail());
    }

    @Override
    public boolean updateProfilePicture(ProfilePictureRequestDto profilePictureRequestDto) {
        // Validate the image
        MultipartFile file = profilePictureRequestDto.getProfilePicture();
        if (file.isEmpty() || !Objects.requireNonNull(file.getContentType()).startsWith(Constants.IMAGE_PREFIX)) {
            throw new IllegalArgumentException("Only image files are allowed, not " + file.getContentType());
        }

        // Find the user
        User user = userRepo.findById(profilePictureRequestDto.getEmail())
                        .orElseThrow(() -> new IllegalArgumentException("User not found"));
        try {
            // Set image data in the user entity
            // Store the image as bytes
            user.setProfilePicture(profilePictureRequestDto.getProfilePicture().getBytes());
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading image data");
        }
        user.setProfilePictureType(profilePictureRequestDto.getProfilePicture().getContentType());

        // Save the updated user record
        userRepo.saveAndFlush(user);
        return true;
    }

    @Override
    public ProfilePictureResponseDto getProfilePicture(String email) {
        // Find the user
        User user = userRepo.findById(email).orElseThrow(() -> new IllegalArgumentException("User not found"));
        ProfilePictureResponseDto profilePictureResponseDto = new ProfilePictureResponseDto();
        profilePictureResponseDto.setProfilePicture(user.getProfilePicture() != null ? user.getProfilePicture() :
                new byte[0]);
        profilePictureResponseDto.setProfilePictureType(user.getProfilePictureType());
        profilePictureResponseDto.setEmail(user.getEmail());
        return profilePictureResponseDto;
    }

    @Override
    public boolean addUserDocument(UserDocumentRequestDto userDocumentRequestDto) {
        // Validate the image
        MultipartFile file = userDocumentRequestDto.getDocument();
        if (file.isEmpty() ||
                (!Objects.requireNonNull(file.getContentType()).startsWith(Constants.MULTIPART_PREFIX) &&
                !Objects.requireNonNull(file.getContentType()).startsWith(Constants.PDF_PREFIX))) {
            throw new IllegalArgumentException("Only multipart and pdf files are allowed, not " + file.getContentType());
        }

        // Find the user
        User user = userRepo.findById(userDocumentRequestDto.getUser().getEmail())
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        Optional<UserDocument> userDocument = userDocumentRepo.findByUser(user);
        try {
            if (userDocument.isEmpty()) {
                UserDocument newUserDocument = new UserDocument();
                newUserDocument.setDocType(userDocumentRequestDto.getDocType());
                newUserDocument.setDocument(userDocumentRequestDto.getDocument().getBytes());
                newUserDocument.setUser(user);
                userDocumentRepo.saveAndFlush(newUserDocument);
            } else {
                UserDocument existingUserDocument = userDocument.get();
                existingUserDocument.setDocType(userDocumentRequestDto.getDocType());
                existingUserDocument.setDocument(userDocumentRequestDto.getDocument().getBytes());
                userDocumentRepo.saveAndFlush(existingUserDocument);
            }
        } catch (IOException e) {
            throw new IllegalArgumentException("Error reading document data");
        }
        return true;
    }

    @Override
    public UserDocumentResponseDto getUserDocument(String email) {
        // Find the user
        User user = userRepo.findById(email)
                .orElseThrow(() -> new IllegalArgumentException("User not found"));
        UserDocument userDocument = userDocumentRepo.findByUser(user)
                .orElseThrow(() -> new IllegalArgumentException("User document not found"));
        UserDocumentResponseDto userDocumentResponseDto = new UserDocumentResponseDto();
        userDocumentResponseDto.setDocument(userDocument.getDocument() != null ? userDocument.getDocument() :
                new byte[0]);
        userDocumentResponseDto.setDocType(userDocument.getDocType());
        return userDocumentResponseDto;
    }

    /**
     * @param userSearchDto -
     * @param page -
     * @param pageSize -
     * @return -
     */
    @Override
    public PageResponseDto<UserResponseDto> searchUsers(UserSearchDto userSearchDto, int page, int pageSize) {
        Pageable pageable = PageRequest.of(page, pageSize);
        Page<User> users = userRepo.searchUsers(userSearchDto.getFirstName(), userSearchDto.getLastName(),
                userSearchDto.getEmail(), pageable);

        return createPageResponseDto(pageSize, users);
    }

    private PageResponseDto<UserResponseDto> createPageResponseDto(int pageSize, Page<User> userPage) {
        List<UserResponseDto> userPages = userPage.getContent().stream()
                .map(user -> modelMapper.map(user, UserResponseDto.class))
                .collect(Collectors.toList());

        PageResponseDto<UserResponseDto> userResponse = new PageResponseDto<>();
        userResponse.setItems(userPages);
        userResponse.setPageSize(pageSize);
        userResponse.setTotalSize(userPage.getTotalElements());
        return userResponse;
    }

    @Override
    @Transactional
    public void deleteUser(UserResponseDto user, String adminId) {
        if (userRepo.existsById(user.getEmail())) {
            User admin = userRepo.findById(adminId).orElseThrow(
                    () -> new EntityNotFoundException("Admin does not exist with id = " + adminId)
            );
            if (user.getEmail().equalsIgnoreCase(Constants.SUPER_ADMIN)) {
                throw new IllegalArgumentException("Cannot delete super admin");
            }
            if (user.getOtp().getPurpose() != VerificationPurpose.USER_DELETION ||
                    !userVerificationService.verifyCode(admin, user.getOtp()))
                throw new IllegalArgumentException("Invalid OTP");
            userRepo.deleteById(user.getEmail());
        }
        else throw new EntityNotFoundException("User does not exist with id = " + user.getEmail());
    }

    @Override
    public long count() {
        return userRepo.count();
    }

}
