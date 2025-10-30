package ac.in.iiitd.fcs29.dto.user;

import ac.in.iiitd.fcs29.entity.UserDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * DTO for {@link UserDocument}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDocumentResponseDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -7298165356823568610L;

    private UUID docId;
    private String docType;
    private UserResponseDto user;
    private byte[] document;
    private LocalDateTime createdAt;
    private LocalDateTime lastUpdatedAt;
}