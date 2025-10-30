package ac.in.iiitd.fcs29.controller;

import ac.in.iiitd.fcs29.dto.*;
import ac.in.iiitd.fcs29.dto.enums.Role;
import ac.in.iiitd.fcs29.dto.login.AuthRequestDto;
import ac.in.iiitd.fcs29.dto.login.AuthResponseDto;
import ac.in.iiitd.fcs29.dto.login.ChangePasswordDto;
import ac.in.iiitd.fcs29.dto.login.UserDto;
import ac.in.iiitd.fcs29.dto.user.*;
import ac.in.iiitd.fcs29.service.BlacklistedTokenService;
import ac.in.iiitd.fcs29.service.JwtService;
import ac.in.iiitd.fcs29.service.UserService;
import ac.in.iiitd.fcs29.service.impl.ServerKeyService;
import jakarta.persistence.EntityExistsException;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.authentication.LockedException;
import org.springframework.security.core.AuthenticationException;
import org.springframework.util.StreamUtils;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import javax.security.auth.login.AccountLockedException;
import java.io.File;
import java.io.FileInputStream;
import java.util.*;
import java.util.zip.Deflater;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * @author sharadjain
 */
@RestController
@RequestMapping("/user")
public class UserController {

    private final UserService userService;

    private final BlacklistedTokenService blacklistedTokenService;

    private final JwtService jwtService;

    private final ServerKeyService serverKeyService;

    private static final Logger logger = LoggerFactory.getLogger("ac.in.iiitd.fcs29");

    public UserController(UserService userService, BlacklistedTokenService blacklistedTokenService,
                          JwtService jwtService, ServerKeyService serverKeyService) {
        this.userService = userService;
        this.blacklistedTokenService = blacklistedTokenService;
        this.jwtService = jwtService;
        this.serverKeyService = serverKeyService;
    }

    /**
     * Method to get all users.
     *
     * @param pageRequestDto -
     * @return - list of users
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/all")
    public ResponseEntity<PageResponseDto<UserResponseDto>> getAllUsers(@RequestBody PageRequestDto pageRequestDto,
                                                                        HttpServletRequest request) {
        try {
            System.out.println("searchUsers = " + pageRequestDto.getPage() + ", " + pageRequestDto.getPageSize());

            String email = jwtService.extractUsername(jwtService.extractJwtFromHeader(request));

            // page count start with 0.
            return ResponseEntity.ok(userService.getAllUsers(email, pageRequestDto.getPage() - 1,
                    pageRequestDto.getPageSize(), pageRequestDto.getSortBy(), pageRequestDto.isSortDesc()));
        } catch (EntityExistsException | IllegalArgumentException e) {
            System.out.println("getAllUsers Error = " + e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
            System.out.println("getAllUsers Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to get user by id.
     *
     * @param id - User ID
     * @return - UserResponseDto
     */
    @GetMapping({"/id/{id}", "/id"})
    public ResponseEntity<UserResponseDto> getUserById(@PathVariable(required = false) String id,
                                                       HttpServletRequest request) {
        try {
            String email;
            if (id == null || id.isEmpty()) {
                System.out.println("getUserById = id is empty");
                email = jwtService.extractUsername(jwtService.extractJwtFromHeader(request));
            } else {
                System.out.println("user request by id = " + id);
                // Verify received user with JWT's user.
                userService.verifyUserWithJWT(id, jwtService.extractJwtFromHeader(request));
                email = id;
            }

            return ResponseEntity.ok(userService.getUserById(email));
        } catch (EntityExistsException | EntityNotFoundException e) {
            System.out.println("getUserById Error = " + e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
            System.out.println("getUserById Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to check if user credentials are valid.
     *
     * @param request - request details
     * @return - user details
     */
    @PostMapping("/validate")
    public ResponseEntity<AuthResponseDto> validate(HttpServletRequest request) {
        System.out.println("Validate controller - " + request.getHeader("Authorization"));
        try {
            String email = jwtService.extractUsername(jwtService.extractJwtFromHeader(request));
            System.out.println(email);
            UserResponseDto user = userService.getUserById(email);
            return ResponseEntity.ok(new AuthResponseDto(user.getEmail(), user.getFirstName(), user.getLastName(),
                    user.getRole(), ""));
        } catch (Exception e) {
            System.out.println("Validate user Error = " + e.getMessage());
            System.out.println(Arrays.toString(e.getStackTrace()));
            HttpStatus status = HttpStatus.FORBIDDEN;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to generate OTP for user.
     *
     * @param otpDto   - OTP details
     * @param request  - request details
     * @return - OTP status
     */
    @PostMapping("/generate-otp")
    public ResponseEntity<Boolean> generateOtp(@RequestBody OtpDto otpDto, HttpServletRequest request) {
        System.out.println("Generate OTP controller - " + request.getHeader("Authorization"));
        try {
            String email = jwtService.extractUsername(jwtService.extractJwtFromHeader(request));
            userService.sendOtp(email, otpDto.getPurpose());
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            System.out.println("Generate OTP Error = " + e.getMessage());
            System.out.println(Arrays.toString(e.getStackTrace()));
            HttpStatus status = HttpStatus.FORBIDDEN;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to generate OTP for email verification.
     *
     * @param emailOtpDto - user email
     * @return - OTP status
     */
    @PostMapping("/generate-email-otp")
    public ResponseEntity<Boolean> generateEmailOtp(@RequestBody EmailOtpDto emailOtpDto) {
        System.out.println("Generate Email OTP controller - " + emailOtpDto.getEmail());
        try {
            userService.sendEmailOtp(emailOtpDto.getEmail(), emailOtpDto.getPurpose());
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            System.out.println("Generate Email OTP Error = " + e.getMessage());
            System.out.println(Arrays.toString(e.getStackTrace()));
            HttpStatus status = HttpStatus.FORBIDDEN;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to handle logout.
     *
     * @param request - request details
     * @return - logout status
     */
    @PostMapping("/logout")
    public ResponseEntity<Boolean> logout(HttpServletRequest request) {
        System.out.println("Logout controller - " + request.getHeader("Authorization"));
        try {
            String authToken = jwtService.extractJwtFromHeader(request);
            blacklistedTokenService.addToBlacklist(authToken, jwtService.extractExpiration(authToken));
            return ResponseEntity.ok(true);
        } catch (Exception e) {
            System.out.println("Validate user Error = " + e.getMessage());
            System.out.println(Arrays.toString(e.getStackTrace()));
            HttpStatus status = HttpStatus.FORBIDDEN;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to check if user exists in database.
     *
     * @return - user
     */
    @PostMapping("/login")
//    public ResponseEntity<AuthResponseDto> login(@RequestBody AuthRequestDto user) {
    public ResponseEntity<AuthResponseDto> login(@RequestBody Map<String, String> encrypted) {
        try {
            AuthRequestDto user = serverKeyService.decryptPayload((encrypted), AuthRequestDto.class);

//            AuthResponseDto resp = userService.login(user);

//        System.out.println("Login controller - " + user.getEmail());
//        try {
            return ResponseEntity.ok(userService.login(user));
        } catch (AccountLockedException e) {
            System.out.println("login user account locked Error = " + e.getMessage());
            HttpStatus status = HttpStatus.LOCKED;
            return ResponseEntity.status(status).build();
        } catch (LockedException e) {
            System.out.println("login user auth temp locked Error = " + e.getMessage());
            HttpStatus status = HttpStatus.TOO_MANY_REQUESTS;
            return ResponseEntity.status(status).build();
        } catch (AuthenticationException | NoSuchElementException e) {
            System.out.println("login user auth Error = " + e.getMessage());
            System.out.println(Arrays.toString(e.getStackTrace()));
            HttpStatus status = HttpStatus.UNAUTHORIZED;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
            System.out.println("login user Error = " + e.getMessage());
//            System.out.println(Arrays.toString(e.getStackTrace()));
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to check if user otp for login is valid.
     *
     * @return - user details
     */
    @PostMapping("/login-otp")
//    public ResponseEntity<AuthResponseDto> loginOtp(HttpServletRequest request, @RequestBody OtpDto otp) {
    public ResponseEntity<AuthResponseDto> loginOtp(@RequestBody Map<String, String> encrypted,
                                                    HttpServletRequest request) {
        System.out.println("loginOtp controller - " + request.getHeader("Authorization"));
        try {
            String jwt = jwtService.extractJwtFromHeader(request);
            OtpDto otp = serverKeyService.decryptPayload((encrypted), OtpDto.class);
            return ResponseEntity.ok(userService.loginOtp(jwt, otp));
        } catch (AuthenticationException | NoSuchElementException | IllegalArgumentException |
                 EntityNotFoundException e) {
            System.out.println("loginOtp Error = " + e.getMessage());
            HttpStatus status = HttpStatus.UNAUTHORIZED;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
            System.out.println("loginOtp user Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to add user to database.
     *
     * @return - added user.
     */
    @PostMapping("/register")
//    public ResponseEntity<AuthResponseDto> addUser(@RequestBody UserDto user) {
    public ResponseEntity<AuthResponseDto> addUser(@RequestBody Map<String, String> encrypted) {
        try {
            UserDto user = serverKeyService.decryptPayload((encrypted), UserDto.class);
            System.out.println("Register User - " + user.getEmail());
            if (user.getRole().equals(Role.ADMIN)) {
                System.out.println("addUser - user cannot add admin");
                HttpStatus status = HttpStatus.UNAUTHORIZED;
                return ResponseEntity.status(status).build();
            }
            user.setRole(Role.UNVERIFIED);
            return addNewUser(user, false);
        } catch (Exception e) {
            System.out.println("add user Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to add admin user to database.
     *
     * @return - added user.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PostMapping("/register-admin")
//    public ResponseEntity<AuthResponseDto> addAdminUser(@RequestBody UserDto user) {
    public ResponseEntity<AuthResponseDto> addAdminUser(@RequestBody Map<String, String> encrypted) {
        try {
            UserDto user = serverKeyService.decryptPayload((encrypted), UserDto.class);
            System.out.println("Register Admin User - " + user.getEmail());
            return addNewUser(user, true);
        } catch (Exception e) {
            System.out.println("add admin user Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Adds a new user to the system based on the provided user details.
     *
     * @param user The user details to be added encapsulated in a UserDto object.
     * @return A ResponseEntity containing an AuthResponseDto object if the user is successfully added,
     * or an empty ResponseEntity with an appropriate HTTP status in case of an error.
     */
    private ResponseEntity<AuthResponseDto> addNewUser(UserDto user, Boolean isAdminRequest) {
        try {
            return ResponseEntity.ok(userService.addUser(user, isAdminRequest));
        } catch (IllegalArgumentException | EntityExistsException e) {
            System.out.println("add user Error = " + e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
            System.out.println("add user Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to reset user password.
     *
     * @return - status of the password reset
     */
    @PostMapping("/reset-password")
//    public ResponseEntity<Boolean> resetPassword(@RequestBody ChangePasswordDto changePasswordDto,
//                                                 HttpServletRequest request) {
    public ResponseEntity<Boolean> resetPassword(@RequestBody Map<String, String> encrypted,
                                                    HttpServletRequest request) {
        try {
            ChangePasswordDto changePasswordDto = serverKeyService.decryptPayload((encrypted), ChangePasswordDto.class);
            System.out.println("Reset Password - " + changePasswordDto.getEmail());
            boolean isAdminReq = false;
            try {
                String userId = jwtService.extractUsername(jwtService.extractJwtFromHeader(request));
                UserResponseDto user = userService.getUserById(userId);
                isAdminReq = user.getRole().equals(Role.ADMIN);
            } catch (Exception e) {
                System.out.println("Reset Password - No JWT");
            }
            userService.resetPassword(changePasswordDto.getEmail(), changePasswordDto.getNewPassword(),
                    changePasswordDto.getOtp(), isAdminReq);
            return ResponseEntity.ok(true);
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            System.out.println("resetPassword Error = " + e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
            System.out.println("resetPassword Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to edit user.
     *
     * @param user - user details
     * @return - user.
     */
    @PostMapping("/edit")
    public ResponseEntity<UserResponseDto> editUser(@RequestBody UserResponseDto user,
                                                    HttpServletRequest request) {
        System.out.println("editUser - " + user.getEmail());
        try {
            // Verify received user with JWT's user.
            userService.verifyUserWithJWT(user.getEmail(), jwtService.extractJwtFromHeader(request));

            boolean isAdmin = userService.getUserById(user.getEmail()).getRole().equals(Role.ADMIN);

            return ResponseEntity.ok(userService.editUser(user, isAdmin));
        } catch (EntityNotFoundException | IllegalArgumentException e) {
            System.out.println("editUser Error = " + e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
            System.out.println("editUser Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * @param user - user details
     * @return - added user.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/lock")
    public ResponseEntity<UserResponseDto> updateUserLock(@RequestBody UserDto user,
                                                          HttpServletRequest request) {
        try {
            System.out.println("updateUserLock User - " + user.getEmail());
            String adminId = jwtService.extractUsername(jwtService.extractJwtFromHeader(request));

            return ResponseEntity.ok(userService.updateUserLock(user, user.getIsAccountLocked(), adminId));
        } catch (EntityExistsException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
            System.out.println("updateUserLock user Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * @param user - user details
     * @return - added user.
     */
    @PreAuthorize("hasRole('ADMIN')")
    @PutMapping("/verify")
    public ResponseEntity<UserResponseDto> verifyUser(@RequestBody UserDto user,
                                                      HttpServletRequest request) {
        try {
            System.out.println("Verify User - " + user.getEmail());
            String adminId = jwtService.extractUsername(jwtService.extractJwtFromHeader(request));

            return ResponseEntity.ok(userService.verifyUser(user, user.getIsVerified(), adminId));
        } catch (EntityExistsException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
            System.out.println("verify user Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    @PostMapping(value = {"/image/{email}", "/image"}, consumes = {MediaType.ALL_VALUE})
    public ResponseEntity<Boolean> addUserImage(@ModelAttribute ProfilePictureRequestDto profilePictureRequestDto,
                                            HttpServletRequest request) {
        System.out.println("addUserImage User - " + profilePictureRequestDto.getEmail());
        try {
            // Verify received user with JWT's user.
            userService.verifyUserWithJWT(profilePictureRequestDto.getEmail(),
                    jwtService.extractJwtFromHeader(request));

            return ResponseEntity.ok(userService.updateProfilePicture(profilePictureRequestDto));
        } catch (EntityExistsException | IllegalArgumentException e) {
            System.out.println("add profilePictureRequestDto Error = " + e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
            System.out.println("add profilePictureRequestDto Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    @GetMapping("/image/{id}")
    public ResponseEntity<ProfilePictureResponseDto> getUserImage(@PathVariable String id,
                                                                  HttpServletRequest request) {
        System.out.println("getUserImage User - " + id);
        try {
            // Verify received user with JWT's user.
//            userService.verifyUserWithJWT(id, jwtService.extractJwtFromHeader(request));
            ProfilePictureResponseDto image = userService.getProfilePicture(id);
            if (image.getProfilePicture() == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
//                    .header(Constants.CONTENT_TYPE, image.getProfilePictureType())
                    .body(image);
        } catch (EntityExistsException | IllegalArgumentException e) {
            System.out.println("getUserImage Error = " + e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
            System.out.println("getUserImage Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    @PostMapping(value = "/doc", consumes = {MediaType.ALL_VALUE})
    public ResponseEntity<Boolean> addUserDocument(
            @RequestPart("document") MultipartFile document,
            @RequestPart("docType") String docType,
            @RequestPart("user") UserResponseDto user,
            HttpServletRequest request) {
        UserDocumentRequestDto userDocumentRequestDto = new UserDocumentRequestDto();
        userDocumentRequestDto.setDocument(document);
        userDocumentRequestDto.setDocType(docType);
        userDocumentRequestDto.setUser(user);
        System.out.println("addUserDocument User - " + userDocumentRequestDto.getUser().getEmail());
        try {
            // Verify received user with JWT's user.
            userService.verifyUserWithJWT(userDocumentRequestDto.getUser().getEmail(),
                    jwtService.extractJwtFromHeader(request));

            return ResponseEntity.ok(userService.addUserDocument(userDocumentRequestDto));
        } catch (EntityExistsException | IllegalArgumentException e) {
            System.out.println("add userDocumentRequestDto Error = " + e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
            System.out.println("add userDocumentRequestDto Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    @GetMapping("/doc/{id}")
    public ResponseEntity<UserDocumentResponseDto> getUserDocument(@PathVariable String id,
                                                                   HttpServletRequest request) {
        System.out.println("getUserDocument User - " + id);
        try {
            // Verify received user with JWT's user.
            userService.verifyUserWithJWT(id, jwtService.extractJwtFromHeader(request));
            UserDocumentResponseDto doc = userService.getUserDocument(id);
            if (doc.getDocument() == null) {
                return ResponseEntity.notFound().build();
            }
            return ResponseEntity.ok()
//                    .header(Constants.CONTENT_TYPE, doc.getDocType())
                    .body(doc);
        } catch (EntityExistsException | IllegalArgumentException e) {
            System.out.println("getUserDocument Error = " + e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
            System.out.println("getUserDocument Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to search users.
     *
     * @param userSearchDto -
     * @return - list of users
     */
    @PostMapping("/search")
    public ResponseEntity<PageResponseDto<UserResponseDto>> searchUsers(@RequestBody UserSearchDto userSearchDto) {
        try {
            System.out.println("searchUsers = " + userSearchDto.getFirstName() + userSearchDto.getLastName() +
                    userSearchDto.getEmail());

            // page count start with 0.
            return ResponseEntity.ok(userService.searchUsers(userSearchDto,
                    userSearchDto.getPage() - 1, userSearchDto.getPageSize()));
        } catch (EntityExistsException | IllegalArgumentException e) {
            System.out.println("searchUsers Error = " + e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
            System.out.println("searchUsers Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * @param user - user details
     * @return -
     */
    @PreAuthorize("hasRole('ADMIN')")
    @DeleteMapping("/delete")
    public ResponseEntity<Boolean> deleteUser(@RequestBody UserResponseDto user, HttpServletRequest request) {
        try {
            System.out.println("deleteUser User - " + user.getEmail());
            String adminId = jwtService.extractUsername(jwtService.extractJwtFromHeader(request));
//            if (userService.getUserById(user.getEmail()).getRole().equals(Role.ADMIN)) {
//                HttpStatus status = HttpStatus.BAD_REQUEST;
//                return ResponseEntity.status(status).build();
//            }
            userService.deleteUser(user, adminId);
            return ResponseEntity.ok(true);
        } catch (EntityExistsException | IllegalArgumentException e) {
            System.out.println(e.getMessage());
            HttpStatus status = HttpStatus.BAD_REQUEST;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
            System.out.println("deleteUser user Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    /**
     * Method to download logs as zip.
     *
     * @param response - response
     */
    @PreAuthorize("hasRole('ADMIN')")
    @GetMapping("/download-logs")
    public void downloadCompressedLogs(HttpServletResponse response) {
        File logsDir = new File("logs");
        if (!logsDir.exists() || !logsDir.isDirectory()) {
            response.setStatus(HttpServletResponse.SC_NOT_FOUND);
            return;
        }

        response.setContentType("application/zip");
        response.setHeader("Content-Disposition", "attachment; filename=logs.zip");

        try (ZipOutputStream zos = new ZipOutputStream(response.getOutputStream())) {
            zos.setLevel(Deflater.BEST_COMPRESSION); // Maximum compression

            for (File file : Objects.requireNonNull(logsDir.listFiles())) {
                if (file.isFile()) {
                    try (FileInputStream fis = new FileInputStream(file)) {
                        ZipEntry zipEntry = new ZipEntry(file.getName());
                        zos.putNextEntry(zipEntry);
                        StreamUtils.copy(fis, zos);
                        zos.closeEntry();
                    }
                }
            }

            zos.finish();
        } catch (Exception e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }
}
