package ac.in.iiitd.fcs29.dto;

import ac.in.iiitd.fcs29.dto.user.UserResponseDto;
import lombok.Getter;
import lombok.Setter;

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
public class MessageResponseDto implements Serializable {
	@Serial
	private static final long serialVersionUID = 5939289559950911614L;

	private UUID id;
	private String message;
	private String iv;
	private byte[] file;
	private String contentType;
	private UserResponseDto sender;
	private UserResponseDto receiver;
	private ChatGroupDto group;
	private LocalDateTime createdAt;
	private LocalDateTime lastUpdatedAt;

}
