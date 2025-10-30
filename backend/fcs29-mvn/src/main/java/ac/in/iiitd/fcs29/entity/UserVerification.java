/*
 * Class name
 *	Review
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
 * 	10-06-2023
 *
 * Description
 * 	This class is entity for product review.
 */

package ac.in.iiitd.fcs29.entity;

import ac.in.iiitd.fcs29.dto.enums.VerificationPurpose;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.CreationTimestamp;

import java.io.Serial;
import java.io.Serializable;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * This class is entity for user verification.
 *
 * @author sharadjain
 */
@Entity
@Setter
@Getter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "user_verifications", indexes = {
        @Index(name = "idx_userverification_user_id", columnList = "user_id")
})
public class UserVerification implements Serializable {
    @Serial
    private static final long serialVersionUID = -4799698854026511488L;

    // columns for entity.
    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties({"relations", "groups"})
    private User user;

    @Size(min = 2, max = 500)
    private String verificationCode;

    @NotNull
    @Enumerated(EnumType.STRING)
    private VerificationPurpose purpose;

    @Column(columnDefinition = "BOOLEAN DEFAULT FALSE")
    private Boolean isExpired = false;

    @CreationTimestamp
    private LocalDateTime createdAt;

    @NotNull
    private LocalDateTime expireAt;

}
