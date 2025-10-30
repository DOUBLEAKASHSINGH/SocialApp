package ac.in.iiitd.fcs29.configuration;

import jakarta.annotation.PostConstruct;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.core.context.SecurityContextHolder;

/**
 * Configuration class to set up the security context mode for the application.
 * <p>
 * This class sets the {@code SecurityContextHolder} strategy to
 * {@code MODE_INHERITABLETHREADLOCAL} to ensure that the security context is
 * inheritable by child threads. This configuration is applied during the class
 * initialization phase.
 * <p>
 * An inheritable security context ensures that the authentication information
 * present in the parent thread can be accessed by any threads spawned by it. This
 * can be helpful in scenarios where background threads need to maintain the same
 * security context as the parent thread.
 */
@Configuration
public class SecurityContextConfig {

    @PostConstruct
    public void setupSecurityContextMode() {
        // Set the SecurityContext mode to INHERITABLETHREADLOCAL
        SecurityContextHolder.setStrategyName(SecurityContextHolder.MODE_INHERITABLETHREADLOCAL);
    }
}

