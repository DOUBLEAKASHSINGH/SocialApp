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

import ac.in.iiitd.fcs29.entity.ChatGroup;
import ac.in.iiitd.fcs29.entity.User;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * This interface defines working of database using JpaRepository interface.
 *
 * @author sharadjain
 */
public interface ChatGroupRepository extends JpaRepository<ChatGroup, UUID> {
    Optional<ChatGroup> findByAdmin(User admin);

    @Query("SELECT cg FROM ChatGroup cg JOIN cg.members m WHERE m = :user")
    Page<ChatGroup> findByUser(@Param("user") User user, Pageable pageable);

    Page<ChatGroup> findByNameContainsIgnoreCase(@NotNull @Size(max = 100) String name, Pageable pageable);
}

