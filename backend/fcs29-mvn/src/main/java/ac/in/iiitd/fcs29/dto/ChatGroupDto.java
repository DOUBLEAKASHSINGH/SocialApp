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

package ac.in.iiitd.fcs29.dto;

import ac.in.iiitd.fcs29.dto.user.UserResponseDto;
import lombok.Getter;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.*;

/**
 * @author sharadjain
 */
@Setter
@Getter
public class ChatGroupDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -1234567890123456789L;

    private UUID id;
    private String name;
    private UserResponseDto admin;
    private Set<UserResponseDto> members = new HashSet<>();
    private byte[] groupPicture;
    private String bio;
    private Boolean isGroupLocked = false;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;

}
