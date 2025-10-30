/*
* Class name
*	ProductDto
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
* 	09-06-2023
*
* Description
* 	This class is for product data transfer.
*/

package ac.in.iiitd.fcs29.dto;

import ac.in.iiitd.fcs29.dto.user.UserResponseDto;
import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * This class is for product data transfer.
 * 
 * @author sharadjain
 *
 */
@Setter
@Getter
public class MessageRequestDto implements Serializable {
	@Serial
	private static final long serialVersionUID = 6879810025653314172L;

	private UUID id;
	private String message;
	private String iv;
	@JsonIgnore
	private MultipartFile file;
	private String contentType;
	private UserResponseDto sender;
	private UserResponseDto receiver;
	private ChatGroupDto group;
	private LocalDateTime createdAt;
	private LocalDateTime lastUpdatedAt;

}
