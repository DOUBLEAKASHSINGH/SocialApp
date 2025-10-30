package ac.in.iiitd.fcs29.dto.user;

import ac.in.iiitd.fcs29.dto.OtpDto;
import ac.in.iiitd.fcs29.dto.enums.Role;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;

/**
 * 
 * @author sharadjain
 *
 */
@Setter
@Getter
public class UserResponseDto implements Serializable {
	@Serial
	private static final long serialVersionUID = -1972716304208376774L;

    private String email;
    private String firstName;
    private String lastName;
	private String bio;
	private Role role;
	private OtpDto otp;
	private Boolean isVerified = false;
	private Boolean isCredentialsExpired = false;
	private Boolean isAccountLocked = false;
	private LocalDateTime createdAt;
	private LocalDateTime lastUpdatedAt;

}
