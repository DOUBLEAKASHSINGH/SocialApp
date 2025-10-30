/*
 * Class name
 *	Request
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
 * 	This class is entity for product review request.
 */

package ac.in.iiitd.fcs29.entity;

import ac.in.iiitd.fcs29.dto.enums.RelationStatus;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import jakarta.persistence.*;
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
 * This class is entity for product review request.
 *
 * @author sharadjain
 */
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Setter
@Getter
@Table(name = "user_relations", indexes = {
        @Index(name = "idx_userrelations_friend_id", columnList = "friend_id, user_id")
})
public class UserRelations implements Serializable {
    @Serial
    private static final long serialVersionUID = 6537722862796269715L;

    // columns for entity.
    @Id
    @Column(name = "relation_id")
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    @JsonIgnoreProperties("relations")
    private User user;

    @ManyToOne
    @JoinColumn(name = "friend_id", nullable = false)
    @JsonIgnoreProperties("relations")
    private User friend;

    @Enumerated(EnumType.STRING)
    private RelationStatus status = RelationStatus.UNKNOWN;

    @CreationTimestamp
    private LocalDateTime createdAt;
    @UpdateTimestamp
    private LocalDateTime lastUpdatedAt;

}
