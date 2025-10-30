package ac.in.iiitd.fcs29.configuration.filter;

import ac.in.iiitd.fcs29.constant.Constants;
import ac.in.iiitd.fcs29.service.JwtService;
import io.jsonwebtoken.ExpiredJwtException;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Collection;
import java.util.List;

/**
 * @author sharadjain
 */
@Component
public class JwtAuthFilter extends OncePerRequestFilter {

    private static final String BEARER_PREFIX = Constants.Security.BEARER_PREFIX;  // "Bearer "
    private static final String BEARER_HEADER = Constants.Security.BEARER_HEADER;  // "Authorization"
    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;
    private static final Logger logger = LoggerFactory.getLogger("ac.in.iiitd.fcs29");

    public JwtAuthFilter(JwtService jwtService, UserDetailsService userDetailsService) {
        this.jwtService = jwtService;
        this.userDetailsService = userDetailsService;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {
        final String authHeader = request.getHeader(BEARER_HEADER);
//        request.getHeaderNames().asIterator().forEachRemaining(name -> {
//            System.out.print("Header Names: " + name);
//            System.out.println(": " + request.getHeader(name));
//        });
        final String jwt;
        final String username;
//        System.out.println("doFilter enter");
        logger.info("doFilter enter");

        if (authHeader == null || !authHeader.startsWith(BEARER_PREFIX)) {
//            System.out.println("doFilterInternal - No header " + authHeader);
            logger.info("doFilterInternal - No header");
            filterChain.doFilter(request, response);
            return;
        }
        jwt = authHeader.substring(BEARER_PREFIX.length()); // length = 7
        try {
            // if user is not null and not authenticated already.
            // SecurityContextHolder holds auth status.
            if (SecurityContextHolder.getContext().getAuthentication() == null) {
                username = jwtService.extractUsername(jwt);
                if (username != null) {
//                    System.out.println("doFilterInternal - auth: " + authHeader);
                    logger.info("doFilterInternal - auth");
                    UserDetails userDetails = userDetailsService.loadUserByUsername(username);

                    if (jwtService.isTokenValid(jwt, userDetails)) {
                        // If the token is an OTP token, only allow access to /user/login-otp
//                        if (jwtService.isOtpTokenValid(jwt, userDetails) &&
//                                !request.getRequestURI().startsWith("/user/login-otp")) {
//                            response.setContentType("application/json");
//                            response.getWriter().write("{\"error\":\"OTP token can only be used for login\"}");
//                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
//                            return;
//                        }
                        Collection<? extends GrantedAuthority> authorities = userDetails.getAuthorities();
                        if (jwtService.isOtpTokenValid(jwt, userDetails)) {
                            // Override the user's role with a temporary OTP role
                            authorities = List.of(new SimpleGrantedAuthority("ROLE_OTP"));
                        }
                        // This token is needed to update SecurityContextHolder.
                        UsernamePasswordAuthenticationToken upaToken =
                                new UsernamePasswordAuthenticationToken(userDetails, null, authorities);
                        upaToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                        // Update security context.
                        SecurityContextHolder.getContext().setAuthentication(upaToken);
                    }
                } else {
//                    System.out.println("doFilterInternal - Invalid token: " + jwt);
                    logger.error("doFilterInternal - Invalid token");
                }
            }
//            filterChain.doFilter(request, response);
        } catch (ExpiredJwtException e) {
//            System.out.println("JwtAuthFilter - token expired: " + e.getMessage());
            logger.error("JwtAuthFilter - token expired: {}", e.getMessage());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Token Expired\"}");
            response.setStatus(HttpServletResponse.SC_REQUEST_TIMEOUT);
            return;
        } catch (Exception e) {
//            System.out.println("JwtAuthFilter - Invalid token: " + e.getMessage());
            logger.error("JwtAuthFilter - Invalid token: {}", e.getMessage());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\":\"Unauthorised access\"}");
            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
            return;
        }
        filterChain.doFilter(request, response);
    }

}
