/*
 * Class name
 *	Product
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
 * 	29-05-2023
 *
 * Last updated By
 * 	Sharad Jain
 *
 * Last updated Date
 * 	11-06-2023
 *
 * Description
 * 	This class is entity for product.
 */

package ac.in.iiitd.fcs29.entity;

import ac.in.iiitd.fcs29.constant.Constants;
import ac.in.iiitd.fcs29.entity.converter.EncryptionConverter;
import ac.in.iiitd.fcs29.entity.converter.FileEncryptionConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * This class is entity for messages.
 *
 * @author sharadjain
 */
@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "message", indexes = {
        @Index(name = "idx_message_sender_id", columnList = "sender_id"),
        @Index(name = "idx_message_receiver_id", columnList = "receiver_id")
})
public class Message implements Serializable {
    @Serial
    private static final long serialVersionUID = 5939289559950911614L;

    // columns for entity.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "message")
    @Size(max = 1000)
    @Convert(converter = EncryptionConverter.class)
    private String message;

    @Column(name = "iv")
    private String iv;

    // `BLOB`: Holds up to 64 KB.
    // `MEDIUMBLOB`: Holds up to 16 MB.
    // `LONGBLOB`: Holds up to 4 GB.
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "file", columnDefinition = "MEDIUMBLOB")
    @JsonIgnore
    @Convert(converter = FileEncryptionConverter.class)
    private byte[] file = new byte[0];

    @Column(name = "content_type")
    @Convert(converter = EncryptionConverter.class)
    private String contentType = Constants.MESSAGE_CONTENT_TYPE;

    @ManyToOne
    @JoinColumn(name = "sender_id", nullable = false)
    @JsonIgnoreProperties({"groups", "relations"})
    private User sender;

    @ManyToOne
    @JoinColumn(name = "receiver_id", nullable = true)
    @JsonIgnoreProperties({"groups", "relations"})
    private User receiver;

    @ManyToOne
    @JoinColumn(name = "group_id")
    @JsonIgnoreProperties({"admin", "members"})
    private ChatGroup group;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime lastUpdatedAt;
}
