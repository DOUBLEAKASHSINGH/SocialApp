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
import ac.in.iiitd.fcs29.entity.embeddable.UserPairId;
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
@Table(name = "user_pair")
public class UserPairKeys implements Serializable {
    @Serial
    private static final long serialVersionUID = 5939262579954816514L;

    @EmbeddedId
    private UserPairId id;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("user1Id")
    @JoinColumn(name = "user1_id")
    private User user1;

    @ManyToOne(fetch = FetchType.LAZY)
    @MapsId("user2Id")
    @JoinColumn(name = "user2_id")
    private User user2;

    @Column(length = 512)
    @Convert(converter = EncryptionConverter.class)
    private String sharedKey;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @UpdateTimestamp
    private LocalDateTime lastUpdatedAt;
}
