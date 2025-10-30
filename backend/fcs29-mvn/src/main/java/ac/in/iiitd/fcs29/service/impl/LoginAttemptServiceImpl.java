package ac.in.iiitd.fcs29.service.impl;

import ac.in.iiitd.fcs29.constant.Constants;
import ac.in.iiitd.fcs29.service.LoginAttemptService;
import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import org.springframework.stereotype.Service;

import java.util.concurrent.TimeUnit;

@Service
public class LoginAttemptServiceImpl implements LoginAttemptService {
    private static final int MAX_ATTEMPTS = Constants.MAX_LOGIN_ATTEMPTS;
    private static final int LOGIN_WAIT_TIME = Constants.LOGIN_WAIT_TIME;
    private final Cache<String, Integer> loginAttemptsCache;

    public LoginAttemptServiceImpl() {
        this.loginAttemptsCache = Caffeine.newBuilder()
                .expireAfterWrite(LOGIN_WAIT_TIME, TimeUnit.MINUTES) // Attempts reset after 15 minutes
                .build();
    }

    @Override
    public void loginFailed(String username) {
        Integer attempts = loginAttemptsCache.getIfPresent(username);
        if (attempts == null) {
            attempts = 0;
        }
        loginAttemptsCache.put(username, attempts + 1);
        System.out.println("Login attempt Failed - " + loginAttemptsCache.getIfPresent(username));
    }

    @Override
    public boolean isBlocked(String username) {
        System.out.println("isBlocked - " + loginAttemptsCache.getIfPresent(username));
        Integer attempts = loginAttemptsCache.getIfPresent(username);
        return attempts != null && attempts >= MAX_ATTEMPTS;
    }

    @Override
    public void loginSucceeded(String username) {
        loginAttemptsCache.invalidate(username); // Reset on successful login
    }
}

