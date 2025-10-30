/*
* Interface name
*	BlacklistedTokenRepository
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
* 	This interface defines working of database using JpaRepository interface.
*/

package ac.in.iiitd.fcs29.repository;

import org.springframework.data.jpa.repository.JpaRepository;

import ac.in.iiitd.fcs29.entity.BlacklistedToken;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.transaction.annotation.Transactional;

/**
 * This interface defines working of database using JpaRepository interface.
 * 
 * @author sharadjain
 *
 */
public interface BlacklistedTokenRepository extends JpaRepository<BlacklistedToken, String> {

    @Modifying
    @Transactional
    @Query("DELETE FROM BlacklistedToken b WHERE b.expirationTime < CURRENT_TIMESTAMP")
    void deleteExpiredTokens();
}
