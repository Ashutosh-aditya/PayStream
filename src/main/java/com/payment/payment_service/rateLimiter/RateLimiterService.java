package com.payment.payment_service.rateLimiter;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.time.Duration;

@Slf4j
@Service
@RequiredArgsConstructor
public class RateLimiterService {

    private final RedisTemplate<String, String> redisTemplate;

    private static final int MAX_REQUESTS = 2;       // max requests
    private static final Duration WINDOW = Duration.ofMinutes(1); // per minute

    public boolean isAllowed(String username) {
        String key = "rate_limit:" + username;

        // Increment the counter for this user
        Long count = redisTemplate.opsForValue().increment(key);

        if (count == 1) {
            // First request — set expiry window
            redisTemplate.expire(key, WINDOW);
        }

        log.info("Rate limit check: user={}, count={}/{}", username, count, MAX_REQUESTS);

        if (count > MAX_REQUESTS) {
            log.warn("Rate limit exceeded for user={}", username);
            return false;
        }

        return true;
    }

    public long getRemainingRequests(String username) {
        String key = "rate_limit:" + username;
        String value = redisTemplate.opsForValue().get(key);
        if (value == null) return MAX_REQUESTS;
        return Math.max(0, MAX_REQUESTS - Long.parseLong(value));
    }
}