package ac.in.iiitd.fcs29.configuration;

import ac.in.iiitd.fcs29.configuration.filter.JwtAuthFilter;
import ac.in.iiitd.fcs29.configuration.filter.RateLimiterFilter;
import ac.in.iiitd.fcs29.configuration.filter.SecurityHeadersFilter;
import ac.in.iiitd.fcs29.constant.Constants;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

import java.util.List;

@Configuration
@EnableWebSecurity
public class WebSecurityConfig {

    private final JwtAuthFilter jwtAuthFilter;
    private final SecurityHeadersFilter securityHeadersFilter;
    private final RateLimiterFilter rateLimiterFilter;

    private final AuthenticationProvider authenticationProvider;

    public WebSecurityConfig(JwtAuthFilter jwtAuthFilter, SecurityHeadersFilter securityHeadersFilter,
                             RateLimiterFilter rateLimiterFilter, AuthenticationProvider authenticationProvider) {
        this.jwtAuthFilter = jwtAuthFilter;
        this.securityHeadersFilter = securityHeadersFilter;
        this.rateLimiterFilter = rateLimiterFilter;
        this.authenticationProvider = authenticationProvider;
    }

    @Bean
    public CorsFilter corsFilter() {
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        CorsConfiguration corsConfig = new CorsConfiguration();

        corsConfig.setAllowedOrigins(List.of("*"));
        corsConfig.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        corsConfig.setAllowedHeaders(List.of("*"));
        corsConfig.setExposedHeaders(List.of(Constants.Security.BEARER_HEADER));
//        corsConfig.setAllowCredentials(true);

        source.registerCorsConfiguration("/**", corsConfig);
        return new CorsFilter(source);
    }

    /**
     * Bean to configure filter chain.
     *
     * @param http - HttpSecurity
     * @return - SecurityFilterChain
     * @throws Exception - several exceptions can be thrown
     */
    @Bean
    SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                // CSRF not needed for stateless session management.
                .csrf((csrf) -> csrf.disable())
                // CORS configuration to allow request from all sources.
//                .cors(customizer -> customizer.configurationSource(request -> {
//                    var cors = new CorsConfiguration();
//                    cors.setAllowedOrigins(List.of("*"));
//                    cors.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
//                    cors.setAllowedHeaders(List.of("*"));
//                    cors.setAllowedHeaders(List.of("Authorization", "Content-Type", "Strict-Transport-Security", "X" +
//                            "-Frame-Options", "X-Content-Type-Options", "X-XSS-Protection",
//                            "Content-Security-Policy"));
//                    cors.addExposedHeader("Authorization");
//                    return cors;
//                }))
                .authorizeHttpRequests((authorizeHttpRequests) -> authorizeHttpRequests
                        // Grant permission to OPTIONS request.
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        // Grant login and register permission to all.
                        .requestMatchers("/user/login/**", "/user/register/**", "/user/generate-email-otp",
                                "/user/reset-password", "/security/public-key")
                        .permitAll()
                        // "ROLE_" is auto appended at start of role.
                        .requestMatchers("/user/login-otp/**").hasRole("OTP")
                        .requestMatchers("/user/all/**", "/user/id/**", "/user/image/**",
                                "/user/logout/**", "/user/validate/**", "/user/doc/**", "/user/search/**",
                                "/user/edit/**", "/message/**", "/user/generate-otp", "/security/**")
                        .hasAnyRole("UNVERIFIED", "USER", "ADMIN")
                        .requestMatchers("/relations/**", "/group/**")
                        .hasAnyRole("USER", "ADMIN")
                        .requestMatchers("/user/**")
                        .hasRole("ADMIN")
                        .anyRequest()
                        .authenticated()
                )
                // session will not be maintained so each request needs to be authenticated.
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authenticationProvider(authenticationProvider)
                // JwtAuthFilter will be called before UsernamePasswordAuthenticationFilter.
                .addFilterBefore(jwtAuthFilter, UsernamePasswordAuthenticationFilter.class)
                // Add Rate Limiting filter
                .addFilterBefore(rateLimiterFilter, JwtAuthFilter.class)
//                .addFilterBefore(corsFilter(), RateLimiterFilter.class)
                // Add Security Headers filter
                .addFilterBefore(securityHeadersFilter, RateLimiterFilter.class);
//                .addFilterBefore(securityHeadersFilter, JwtAuthFilter.class);
        return http.build();
    }
}
