/*
 * Class name
 *	BlacklistedTokenServiceImpl
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
 * 	This is a service class for Blacklisted Token services.
 */

package ac.in.iiitd.fcs29.service.impl;

import ac.in.iiitd.fcs29.entity.BlacklistedToken;
import ac.in.iiitd.fcs29.repository.BlacklistedTokenRepository;
import ac.in.iiitd.fcs29.service.BlacklistedTokenService;
import jakarta.transaction.Transactional;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.Date;

/**
 * @author sharadjain
 */
@Service
public class BlacklistedTokenServiceImpl implements BlacklistedTokenService {

    private final BlacklistedTokenRepository blacklistedTokenRepo;

    public BlacklistedTokenServiceImpl(BlacklistedTokenRepository blacklistedTokenRepo) {
        this.blacklistedTokenRepo = blacklistedTokenRepo;
    }

    @Override
    public Boolean isTokenBlacklisted(String token) {
        return blacklistedTokenRepo.existsById(token);
    }

    @Override
    public void addToBlacklist(BlacklistedToken token) {
        blacklistedTokenRepo.saveAndFlush(token);
    }

    @Override
    public void addToBlacklist(String authToken, Date expirationTime) {
        BlacklistedToken logoutToken = new BlacklistedToken();
        logoutToken.setToken(authToken);
        logoutToken.setExpirationTime(expirationTime);
        this.addToBlacklist(logoutToken);
    }

    /**
     * Periodically removes expired tokens from the blacklist repository.
     * <p>
     * This method is scheduled to run hourly, as defined by the cron expression
     * "0 0 * * * ?". It invokes the repository method to delete all tokens
     * whose expiration time has passed as per the current timestamp.
     * <p>
     * Note: The scheduling and transactional annotations ensure that the
     * method runs within a transactional context and follows the defined schedule.
     */
    @Override
    @Scheduled(cron = "0 0 * * * ?")
//    @Scheduled(cron = "0 * * * * *")
    @Transactional
    public void clearExpiredTokens() {
        System.out.println("Clearing expired tokens.");
        blacklistedTokenRepo.deleteExpiredTokens();
    }
}
