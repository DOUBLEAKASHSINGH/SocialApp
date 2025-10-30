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

import java.io.Serial;
import java.io.Serializable;

/**
 * @author sharadjain
 */
@Setter
@Getter
public class ProfilePictureResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 8763548237447547234L;

    private String email;
    private byte[] profilePicture;
    private String profilePictureType;
}
