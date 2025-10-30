package ac.in.iiitd.fcs29.configuration.filter;

import ac.in.iiitd.fcs29.constant.Constants;
import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.NonNull;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Component
public class RateLimiterFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> buckets = new ConcurrentHashMap<>();

    @Override
    protected void doFilterInternal(HttpServletRequest request, @NonNull HttpServletResponse response,
                                    @NonNull FilterChain chain)
            throws ServletException, IOException {

        String key = request.getRemoteAddr() + ":" + getBucketKey(request);

        Bucket bucket = buckets.computeIfAbsent(key, this::newBucket);

        if (bucket.tryConsume(1)) {
            chain.doFilter(request, response);
        } else {
            response.setStatus(HttpStatus.TOO_MANY_REQUESTS.value());
            response.setContentType("application/json");
            response.getWriter().write("{\"error\": \"Rate limit exceeded\"}");
        }
    }

    private String getBucketKey(HttpServletRequest request) {
        return request.getRequestURI().startsWith("/user/image/") ? "img" : "general";
    }

    private Bucket newBucket(String key) {
        int img_req_limit = Constants.MAX_IMG_REQUESTS_PER_TIME;  // 30
        int general_req_limit = Constants.MAX_GENERAL_REQUESTS_PER_TIME;  // 50
        int img_req_time_sec = Constants.IMG_REQUESTS_DURATION_SEC;  // 10
        int general_req_time_min = Constants.GENERAL_REQUESTS_DURATION_MIN;  // 1
        Bandwidth limit;

        if (key.endsWith("img")) {
            limit = Bandwidth.builder()
                    .capacity(img_req_limit)
                    .refillIntervally(img_req_limit, Duration.ofSeconds(img_req_time_sec))
                    .build();
        } else {
            limit = Bandwidth.builder()
                    .capacity(general_req_limit)
                    .refillIntervally(general_req_limit, Duration.ofMinutes(general_req_time_min))
                    .build();
        }

        return Bucket.builder()
                .addLimit(limit)
                .build();
    }
}
