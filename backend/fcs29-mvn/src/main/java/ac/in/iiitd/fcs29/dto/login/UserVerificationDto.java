/*
* Class name
*	RequestDto
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
* 	This class is for product review request data transfer.
*/

package ac.in.iiitd.fcs29.dto.login;

import ac.in.iiitd.fcs29.dto.enums.VerificationPurpose;
import ac.in.iiitd.fcs29.dto.user.UserResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * This class is for product review request data transfer.
 * 
 * @author sharadjain
 *
 */
@Getter
@Setter
public class UserVerificationDto implements Serializable {
	@Serial
	private static final long serialVersionUID = 7972233595896780816L;

	private UUID id;
	private UserResponseDto user;
	private String verificationCode;
	private VerificationPurpose purpose;
	private Boolean isExpired;
	private LocalDateTime createdAt;
	private LocalDateTime expireAt;

}
