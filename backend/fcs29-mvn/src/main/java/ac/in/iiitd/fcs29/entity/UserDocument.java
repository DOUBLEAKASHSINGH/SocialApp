package ac.in.iiitd.fcs29.entity;

import ac.in.iiitd.fcs29.entity.converter.EncryptionConverter;
import ac.in.iiitd.fcs29.entity.converter.FileEncryptionConverter;
import jakarta.persistence.*;
import lombok.Getter;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

@Getter
@Setter
@Entity
@Table(name = "user_document", indexes = {
        @Index(name = "idx_userdocument_user_id", columnList = "user_id")
})
public class UserDocument implements Serializable {
    @Serial
    private static final long serialVersionUID = -7292635918323778610L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "doc_id", nullable = false)
    private UUID docId;

    @Column(name = "doc_type", nullable = false)
    @Convert(converter = EncryptionConverter.class)
    private String docType;

    @OneToOne(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "user_id", unique = true, nullable = false)
    private User user;

    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "document", columnDefinition = "MEDIUMBLOB")
    @Convert(converter = FileEncryptionConverter.class)
    private byte[] document = new byte[0];

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime lastUpdatedAt;

}