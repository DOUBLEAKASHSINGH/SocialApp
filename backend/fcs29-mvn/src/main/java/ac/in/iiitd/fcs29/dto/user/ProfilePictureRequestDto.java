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

package ac.in.iiitd.fcs29.dto.user;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serial;
import java.io.Serializable;

/**
 * 
 * @author sharadjain
 *
 */
@Setter
@Getter
public class ProfilePictureRequestDto implements Serializable {
	@Serial
	private static final long serialVersionUID = 8723948237498347234L;

    private String email;
	private MultipartFile profilePicture;
}
