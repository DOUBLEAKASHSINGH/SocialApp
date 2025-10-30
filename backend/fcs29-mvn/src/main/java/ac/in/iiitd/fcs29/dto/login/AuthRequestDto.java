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

import jakarta.validation.constraints.NotNull;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * @author sharadjain
 */
@Setter
@Getter
public class AuthRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -1234567890123456789L;

    private String email;
    private String password;

    public AuthRequestDto(String email, String password) {
        super();
        this.email = email;
        this.password = password;
    }

    public AuthRequestDto() {
        super();
    }

}
