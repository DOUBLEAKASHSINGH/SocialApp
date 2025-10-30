package ac.in.iiitd.fcs29.service;

public interface LoginAttemptService {
    /**
     * Records a failed login attempt for the specified username.
     *
     * @param username the username for which the login attempt failed
     */
    void loginFailed(String username);

    /**
     * Checks if the specified username is blocked due to too many failed login attempts.
     *
     * @param username the username to check
     * @return true if the username is blocked, false otherwise
     */
    boolean isBlocked(String username);

    /**
     * Records a successful login attempt for the specified username, resetting the failed attempts count.
     *
     * @param username the username for which the login attempt succeeded
     */
    void loginSucceeded(String username);
}
