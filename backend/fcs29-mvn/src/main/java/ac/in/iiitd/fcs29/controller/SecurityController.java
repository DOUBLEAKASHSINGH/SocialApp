package ac.in.iiitd.fcs29.controller;

import ac.in.iiitd.fcs29.dto.KeyDto;
import ac.in.iiitd.fcs29.dto.SharedKeyRequestDto;
import ac.in.iiitd.fcs29.dto.enums.Role;
import ac.in.iiitd.fcs29.dto.user.UserResponseDto;
import ac.in.iiitd.fcs29.service.BlacklistedTokenService;
import ac.in.iiitd.fcs29.service.JwtService;
import ac.in.iiitd.fcs29.service.UserService;
import ac.in.iiitd.fcs29.service.impl.ServerKeyService;
import jakarta.persistence.EntityNotFoundException;
import jakarta.servlet.http.HttpServletRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

/**
 * @author sharadjain
 */
@RestController
@RequestMapping("/security")
public class SecurityController {

    private static final Logger logger = LoggerFactory.getLogger("ac.in.iiitd.fcs29");
    private final UserService userService;
    private final BlacklistedTokenService blacklistedTokenService;
    private final JwtService jwtService;
    private final ServerKeyService serverKeyService;

    public SecurityController(UserService userService, BlacklistedTokenService blacklistedTokenService,
                              JwtService jwtService, ServerKeyService serverKeyService) {
        this.userService = userService;
        this.blacklistedTokenService = blacklistedTokenService;
        this.jwtService = jwtService;
        this.serverKeyService = serverKeyService;
    }

    @GetMapping("/public-key")
    public ResponseEntity<KeyDto> getPublicKey() {
        String key = serverKeyService.getPublicKeyBase64();
        KeyDto keyDto = new KeyDto(key);
        return ResponseEntity.ok(keyDto);
    }

    @PostMapping("/shared-key/p2p")
    public ResponseEntity<KeyDto> getUserKey(@RequestBody SharedKeyRequestDto sharedKeyRequestDto,
                                             HttpServletRequest request) {
        try {
            System.out.println("sharedKeyRequestDto = " + sharedKeyRequestDto.getUser1() + " " + sharedKeyRequestDto.getUser2());
            String userId = jwtService.extractUsername(jwtService.extractJwtFromHeader(request));
            UserResponseDto user = userService.getUserById(userId);
            boolean isAdminReq = user.getRole().equals(Role.ADMIN);
            System.out.println("isAdminReq = " + isAdminReq + " userId = " + userId);
            if (!isAdminReq) {
                if (!sharedKeyRequestDto.getUser1().equalsIgnoreCase(userId)
                        && !sharedKeyRequestDto.getUser2().equalsIgnoreCase(userId)) {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
                }
            }
            String key = serverKeyService.getUserSharedKey(sharedKeyRequestDto.getUser1(),
                    sharedKeyRequestDto.getUser2(), sharedKeyRequestDto.getPublicKey());
            KeyDto keyDto = new KeyDto(key);
            return ResponseEntity.ok(keyDto);
        } catch (EntityNotFoundException e) {
            System.out.println("getUserKey Error = " + e.getMessage());
            HttpStatus status = HttpStatus.NOT_FOUND;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
            System.out.println("getUserKey Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }

    @PostMapping("/shared-key/group")
    public ResponseEntity<KeyDto> getGroupKey(@RequestBody SharedKeyRequestDto sharedKeyRequestDto,
                                              HttpServletRequest request) {
        try {
            String userId = jwtService.extractUsername(jwtService.extractJwtFromHeader(request));
            UserResponseDto user = userService.getUserById(userId);
            boolean isAdminReq = user.getRole().equals(Role.ADMIN);
            String key = serverKeyService.getGroupSharedKey(sharedKeyRequestDto.getGroupId(), userId, isAdminReq,
                    sharedKeyRequestDto.getPublicKey());
            KeyDto keyDto = new KeyDto(key);
            return ResponseEntity.ok(keyDto);
        } catch (EntityNotFoundException e) {
            System.out.println("getGroupKey Error = " + e.getMessage());
            HttpStatus status = HttpStatus.NOT_FOUND;
            return ResponseEntity.status(status).build();
        } catch (Exception e) {
            System.out.println("getGroupKey Error = " + e.getMessage());
            HttpStatus status = HttpStatus.INTERNAL_SERVER_ERROR;
            return ResponseEntity.status(status).build();
        }
    }
}
