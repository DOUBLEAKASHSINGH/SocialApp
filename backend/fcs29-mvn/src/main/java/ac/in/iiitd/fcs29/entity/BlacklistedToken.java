/*
 * Class name
 *	BlacklistedToken
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
 * 	12-06-2023
 *
 * Last updated By
 * 	Sharad Jain
 *
 * Last updated Date
 * 	12-06-2023
 *
 * Description
 * 	This class is entity for blacklisted tokens.
 */

package ac.in.iiitd.fcs29.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serial;
import java.io.Serializable;
import java.util.Date;

/**
 * This class is entity for blacklist JWT.
 *
 * @author sharadjain
 */
@Entity
@Setter
@Getter
@AllArgsConstructor
@NoArgsConstructor
@Table(name = "blacklisted_token")
public class BlacklistedToken implements Serializable {
    @Serial
    private static final long serialVersionUID = -4896027560794233178L;

    // columns for entity.
    @Id
    @Column(name = "token")
    private String token;

    @Column(name = "expiration_time")
    private Date expirationTime;

}
