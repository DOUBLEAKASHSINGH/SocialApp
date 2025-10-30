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
import ac.in.iiitd.fcs29.entity.Message;
import ac.in.iiitd.fcs29.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.UUID;

/**
 * This interface defines working of database using JpaRepository interface.
 *
 * @author sharadjain
 */
public interface MessageRepository extends JpaRepository<Message, UUID> {
    @Query("SELECT m FROM Message m WHERE (m.sender = :user1 AND m.receiver = :user2) OR (m.sender = :user2 AND " +
            "m.receiver = :user1) ORDER BY m.createdAt DESC")
    Page<Message> findMessagesBetweenUsers(@Param("user1") User user1, @Param("user2") User user2, Pageable pageable);

    Page<Message> findByGroup(ChatGroup group, Pageable pageable);

    Page<Message> findByGroupOrderByCreatedAtDesc(ChatGroup group, Pageable pageable);

    @Query("""
                SELECT DISTINCT u FROM User u
                WHERE u.email IN (
                    SELECT CASE
                               WHEN m.sender.email <> :userId THEN m.sender.email
                               ELSE m.receiver.email
                           END
                    FROM Message m
                    WHERE m.group IS NULL
                        AND (m.sender.email = :userId OR m.receiver.email = :userId)
                    ORDER BY m.lastUpdatedAt DESC
                )
            """)
    Page<User> findChatUsers(@Param("userId") String userId, Pageable pageable);

    @Query("""
                SELECT DISTINCT g FROM ChatGroup g
                WHERE g.id IN (
                    SELECT m.group.id FROM Message m
                    WHERE m.group IS NOT NULL
                    AND EXISTS (
                        SELECT 1 FROM g.members gm WHERE gm.email = :userId
                    )
                    ORDER BY m.lastUpdatedAt DESC
                )
            """)
    Page<ChatGroup> findChatGroups(@Param("userId") String userId, Pageable pageable);
}

