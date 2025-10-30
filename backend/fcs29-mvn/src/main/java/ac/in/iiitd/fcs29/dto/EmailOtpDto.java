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

package ac.in.iiitd.fcs29.dto;

import ac.in.iiitd.fcs29.dto.enums.VerificationPurpose;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * This class is for otp transfer.
 *
 * @author sharadjain
 */
@Getter
@Setter
public class EmailOtpDto implements Serializable {
    @Serial
    private static final long serialVersionUID = 2022485756866116654L;

    private String email;
    private String otp;
    private VerificationPurpose purpose;
}
