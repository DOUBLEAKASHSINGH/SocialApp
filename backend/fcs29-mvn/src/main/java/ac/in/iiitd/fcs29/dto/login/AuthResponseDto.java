/*
 * Class name
 *	AuthResponseDto
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
 * 	This class is user login token response.
 */

package ac.in.iiitd.fcs29.dto.login;

import ac.in.iiitd.fcs29.dto.enums.Role;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author sharadjain
 */
@Setter
@Getter
public class AuthResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -3389092795646547635L;

    private String email;
    private String firstName;
    private String lastName;
    private Role role;
    private String token;

    public AuthResponseDto(String email, String firstName, String lastName, Role role, String token) {
        super();
        this.email = email;
        this.firstName = firstName;
        this.lastName = lastName;
        this.role = role;
        this.token = token;
    }

    public AuthResponseDto() {
        super();
    }

}
