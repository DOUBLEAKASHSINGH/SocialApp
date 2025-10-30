/*
 * Interface name
 *	UserRepository
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
 * 	31-05-2023
 *
 * Description
 * 	This interface defines working of database using JpaRepository interface.
 */

package ac.in.iiitd.fcs29.repository;

import ac.in.iiitd.fcs29.entity.User;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

/**
 * This interface defines working of database using JpaRepository interface.
 *
 * @author sharadjain
 */
public interface UserRepository extends JpaRepository<User, String> {
    @Query("SELECT DISTINCT u FROM User u WHERE " +
            "(:firstName IS NULL OR LOWER(u.firstName) LIKE LOWER(CONCAT('%', :firstName, '%'))) OR " +
            "(:lastName IS NULL OR LOWER(u.lastName) LIKE LOWER(CONCAT('%', :lastName, '%'))) OR " +
            "(:email IS NULL OR LOWER(u.email) LIKE LOWER(CONCAT('%', :email, '%')))")
    Page<User> searchUsers(@Param("firstName") String firstName, @Param("lastName") String lastName,
                           @Param("email") String email, Pageable pageable);

    Page<User> findByEmailIsNotIgnoreCase(@Size(min = 2, max = 320) String email, Pageable pageable);
}

