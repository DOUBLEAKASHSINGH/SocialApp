/*
 * Class name
 *	User
 *
 * Version info
 *	JavaSE-17
 *
 * Copyright notice
 *
 * Author info
 *	Name: Sharad Jain
 *	Email-ID: sharad24138@iiitd.ac.in
 *
 * Creation date
 * 	29-01-2025
 *
 * Last updated By
 * 	Sharad Jain
 *
 * Last updated Date
 * 	11-02-2025
 *
 * Description
 * 	This class is entity for user login.
 */

package ac.in.iiitd.fcs29.entity;

import ac.in.iiitd.fcs29.entity.converter.EncryptionConverter;
import ac.in.iiitd.fcs29.entity.converter.FileEncryptionConverter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.*;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.UpdateTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * This class is entity for user login.
 *
 * @author sharadjain
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Table(name = "chat_group")
public class ChatGroup implements Serializable {
    @Serial
    private static final long serialVersionUID = -7235635968325478610L;

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "group_id")
    private UUID id;

    // columns for entity.
    @NotNull
    @Size(max = 100)
    @Column(name = "group_name")
    private String name;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"relations", "groups"})
    private User admin;

    @ManyToMany(cascade = {CascadeType.PERSIST, CascadeType.MERGE}, fetch = FetchType.LAZY)
    @JoinTable(
            name = "group_members",
            joinColumns = @JoinColumn(name = "group_id"),
            inverseJoinColumns = @JoinColumn(name = "user_id")
    )
    private Set<User> members = new HashSet<>();

    // `BLOB`: Holds up to 64 KB.
    // `MEDIUMBLOB`: Holds up to 16 MB.
    // `LONGBLOB`: Holds up to 4 GB.
    @Lob
    @Basic(fetch = FetchType.LAZY)
    @Column(name = "group_picture", columnDefinition = "MEDIUMBLOB")
    @JsonIgnore
    @ToString.Exclude
    @Convert(converter = FileEncryptionConverter.class)
    private byte[] groupPicture = new byte[0];

    @Size(max = 500)
    @Convert(converter = EncryptionConverter.class)
    private String bio;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isGroupLocked = false;

    @Column(name = "shared_key")
    @Size(max = 512)
    @Convert(converter = EncryptionConverter.class)
    private String sharedKey;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime lastUpdatedAt;

}
