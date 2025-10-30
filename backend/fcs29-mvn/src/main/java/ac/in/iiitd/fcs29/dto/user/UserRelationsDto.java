/*
* Class name
*	ReviewDto
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
* 	10-06-2023
*
* Description
* 	This class is for product review data transfer.
*/

package ac.in.iiitd.fcs29.dto.user;

import ac.in.iiitd.fcs29.dto.enums.RelationStatus;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * This class is for product review data transfer.
 * 
 * @author sharadjain
 *
 */
@Setter
@Getter
public class UserRelationsDto implements Serializable {
	@Serial
	private static final long serialVersionUID = 4108725888742286564L;

	private UUID id;
	private UserResponseDto user;
	private UserResponseDto friend;
	private RelationStatus status;
	private LocalDateTime createdAt;
	private LocalDateTime lastUpdatedAt;

}
