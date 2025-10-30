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
 * 
 * @author sharadjain
 *
 */
@Setter
@Getter
public class UserSearchDto implements Serializable {
	@Serial
	private static final long serialVersionUID = -7495114465489235731L;

    private String email;
    private String firstName;
    private String lastName;
	private int page;
	private int pageSize;

}
