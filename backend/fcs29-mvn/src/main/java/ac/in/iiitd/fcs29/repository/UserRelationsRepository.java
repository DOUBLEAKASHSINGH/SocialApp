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

import ac.in.iiitd.fcs29.dto.enums.RelationStatus;
import ac.in.iiitd.fcs29.entity.User;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import ac.in.iiitd.fcs29.entity.UserRelations;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;
import java.util.UUID;

/**
 * This interface defines working of database using JpaRepository interface.
 * 
 * @author sharadjain
 *
 */
public interface UserRelationsRepository extends JpaRepository<UserRelations, UUID> {

    @Query("SELECT ur FROM UserRelations ur WHERE ur.user = :user OR ur.friend = :user ORDER BY ur.createdAt DESC")
    Page<UserRelations> findRelationsOfUser(@Param("user") User user, Pageable pageable);

    @Query("SELECT ur FROM UserRelations ur WHERE (ur.user = :user OR ur.friend = :user) AND ur.status = :status ORDER BY ur.createdAt DESC")
    Page<UserRelations> findFriendsOfUser(@Param("user") User user, @Param("status") RelationStatus status, Pageable pageable);

    Page<UserRelations> findByUserAndStatusOrderByCreatedAt(User user, RelationStatus status, Pageable pageable);

    Page<UserRelations> findByFriendAndStatusOrderByCreatedAt(User friend, RelationStatus status, Pageable pageable);

    @Query("select count(u) from UserRelations u where (u.user = ?1 and u.friend = ?2) or (u.user = ?2 and u.friend = ?1)")
    long countByUserAndFriend(User user, User friend);

    Optional<UserRelations> findByUserAndFriend(User user, User friend);
}
