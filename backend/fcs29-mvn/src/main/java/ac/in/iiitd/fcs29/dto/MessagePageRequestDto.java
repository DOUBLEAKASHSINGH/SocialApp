/*
* Class name
*	ProductPageRequestDto
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
* 	09-06-2023
*
* Last updated By
* 	Sharad Jain
*
* Last updated Date
* 	09-06-2023
*
* Description
* 	This class is for product page request data transfer.
*/

package ac.in.iiitd.fcs29.dto;

import ac.in.iiitd.fcs29.constant.Constants;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;

/**
 * This class is for message request data transfer.
 * 
 * @author sharadjain
 *
 */
@Setter
@Getter
public class MessagePageRequestDto implements Serializable {
	@Serial
	private static final long serialVersionUID = -6534090441920086653L;

    // user making request
    private String user;
    // target user-ID or group-ID
    private String target;
    private int page;
    @Max(value = Constants.MAX_PAGE_SIZE
            , message = "Page size should be less than or equal to " + Constants.MAX_PAGE_SIZE)
    @Min(value = Constants.MIN_PAGE_SIZE
            , message = "Page size should be greater than or equal to " + Constants.MIN_PAGE_SIZE)
    private int pageSize;

}
