package ac.in.iiitd.fcs29.dto.user;

import ac.in.iiitd.fcs29.entity.UserDocument;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

import java.io.Serial;
import java.io.Serializable;
import java.util.UUID;

/**
 * DTO for {@link UserDocument}
 */
@Data
@AllArgsConstructor
@NoArgsConstructor
public class UserDocumentRequestDto implements Serializable {
    @Serial
    private static final long serialVersionUID = -7292265358323568610L;

    private UUID docId;
    private String docType;
    private UserResponseDto user;
    private MultipartFile document;
}