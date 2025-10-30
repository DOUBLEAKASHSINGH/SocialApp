package ac.in.iiitd.fcs29.configuration;

import ac.in.iiitd.fcs29.service.LoginAttemptService;
import org.modelmapper.ModelMapper;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.ProviderManager;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Arrays;

@Configuration
public class AppConfig {

    private final UserDetailsService userDetailsService;
    private final LoginAttemptService loginAttemptService;

    public AppConfig(UserDetailsService userDetailsService, LoginAttemptService loginAttemptService) {
        this.userDetailsService = userDetailsService;
        this.loginAttemptService = loginAttemptService;
    }

    /**
     * Method to create password encoder for spring security.
     *
     * @return
     */
    @Bean
    PasswordEncoder passwordEncoder() {
//		System.out.println(new BCryptPasswordEncoder().encode("admin"));
        return new BCryptPasswordEncoder();
//		return NoOpPasswordEncoder.getInstance();
    }

    /**
     * Bean for ModelMapper
     *
     * @return
     */
    @Bean
    ModelMapper modelMapper() {
        return new ModelMapper();
    }

//    @Bean
//    AuthenticationManager authenticationManager(AuthenticationConfiguration authConfig) throws Exception {
//        return authConfig.getAuthenticationManager();
//    }

    /**
     * Bean to create AuthenticationManager.
     */
    @Bean
    AuthenticationManager authenticationManager() {
        return new ProviderManager(Arrays.asList(authenticationProvider()));
    }

    /**
     * Bean for AuthenticationProvider. useful when there are multiple
     * UserDetailsService and PasswordEncoder
     *
     * @return - AuthenticationProvider
     */
    @Bean
    AuthenticationProvider authenticationProvider() {
        return new CustomAuthenticationProvider(userDetailsService, passwordEncoder(), loginAttemptService);
//        DaoAuthenticationProvider authProvider = new DaoAuthenticationProvider();
//        authProvider.setUserDetailsService(userDetailsService);
//        authProvider.setPasswordEncoder(passwordEncoder());
//        return authProvider;
    }
}
