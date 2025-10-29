package com.electricistacat.backend.config;

import io.github.bucket4j.Bandwidth;
import io.github.bucket4j.Bucket;
import io.github.bucket4j.Refill;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.time.Duration;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

@Component
public class RateLimitingFilter extends OncePerRequestFilter {

    private final Map<String, Bucket> cache = new ConcurrentHashMap<>();
    private final boolean enabled;
    private final int capacity;
    private final int refillTokens;
    private final int refillPeriodSeconds;

    public RateLimitingFilter(
            @Value("${rate-limiting.enabled:true}") boolean enabled,
            @Value("${rate-limiting.capacity:100}") int capacity,
            @Value("${rate-limiting.refill-tokens:100}") int refillTokens,
            @Value("${rate-limiting.refill-period-seconds:60}") int refillPeriodSeconds) {
        this.enabled = enabled;
        this.capacity = capacity;
        this.refillTokens = refillTokens;
        this.refillPeriodSeconds = refillPeriodSeconds;
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        if (!enabled) {
            filterChain.doFilter(request, response);
            return;
        }
        String key = request.getRemoteAddr();
        Bucket bucket = cache.computeIfAbsent(key, this::newBucket);
        if (bucket.tryConsume(1)) {
            filterChain.doFilter(request, response);
        } else {
            response.setStatus(HttpServletResponse.SC_TOO_MANY_REQUESTS);
            response.getWriter().write("Rate limit exceeded");
        }
    }

    private Bucket newBucket(String key) {
        Bandwidth bandwidth = Bandwidth.classic(capacity, Refill.intervally(refillTokens, Duration.ofSeconds(refillPeriodSeconds)));
        return Bucket.builder().addLimit(bandwidth).build();
    }
}
