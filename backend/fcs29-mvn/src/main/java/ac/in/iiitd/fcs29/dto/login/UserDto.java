/*
* Class name
*	UserDto
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
* 	02-06-2023
*
* Last updated By
* 	Sharad Jain
*
* Last updated Date
* 	02-06-2023
*
* Description
* 	This class is user data transfer.
*/

package ac.in.iiitd.fcs29.dto.login;

import ac.in.iiitd.fcs29.dto.OtpDto;
import ac.in.iiitd.fcs29.dto.enums.Role;
import ac.in.iiitd.fcs29.dto.enums.VerificationPurpose;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * 
 * @author sharadjain
 *
 */
@Setter
@Getter
public class UserDto implements Serializable {
	@Serial
	private static final long serialVersionUID = -7439514483989242931L;

    private String email;
    private String firstName;
    private String lastName;
	private String bio;
    private Role role;
	private OtpDto otp;
    private String password;
	private Boolean isVerified = false;
	private Boolean isCredentialsExpired = false;
	private Boolean isAccountLocked = false;

}
