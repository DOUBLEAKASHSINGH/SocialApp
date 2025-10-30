/*
 * Interface name
 *	ReviewRepository
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
 * 	This interface defines working of database using JpaRepository interface.
 */

package ac.in.iiitd.fcs29.repository;

import ac.in.iiitd.fcs29.entity.User;
import ac.in.iiitd.fcs29.entity.UserVerification;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

/**
 * This interface defines working of database using JpaRepository interface.
 *
 * @author sharadjain
 */
public interface UserVerificationRepository extends JpaRepository<UserVerification, UUID> {
    void deleteAllByExpireAtBefore(@NotNull LocalDateTime expireAtBefore);

    List<UserVerification> findByUser(User user);

    Optional<UserVerification> findByUserAndVerificationCode(User user, @Size(min = 2, max = 500) String verificationCode);
}
