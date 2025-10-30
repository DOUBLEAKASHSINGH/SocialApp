/*
 * Interface name
 *	BlacklistedTokenService
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
 * 	This interface defines working of blacklisted token services.
 */

package ac.in.iiitd.fcs29.service;

import ac.in.iiitd.fcs29.entity.BlacklistedToken;

import java.util.Date;

/**
 * @author sharadjain
 */
public interface BlacklistedTokenService {

    /**
     * Method to check if token is blacklisted.
     *
     * @param token
     * @return
     */
    Boolean isTokenBlacklisted(String token);

    /**
     * Method to add token to blacklist.
     *
     * @param token
     */
    void addToBlacklist(BlacklistedToken token);

    void addToBlacklist(String authToken, Date expirationTime);

    /**
     * Method to clear expired tokens.
     */
    void clearExpiredTokens();

}
