package ac.in.iiitd.fcs29.configuration.filter;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class SecurityHeadersFilter extends OncePerRequestFilter {

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request, HttpServletResponse response,
                                    FilterChain filterChain) throws ServletException, IOException {
        // Add security-related headers
        // Enforce HTTPS
        response.setHeader("Strict-Transport-Security", "max-age=31536000; includeSubDomains");
        // Prevent MIME sniffing
        response.setHeader("X-Content-Type-Options", "nosniff");
        // Prevent click-jacking
        response.setHeader("X-Frame-Options", "DENY");
        // Mitigate XSS attacks
        response.setHeader("X-XSS-Protection", "1; mode=block");
        // Restrict script loading sources
        response.setHeader("Content-Security-Policy", "script-src 'self'");

        filterChain.doFilter(request, response);
    }
}

