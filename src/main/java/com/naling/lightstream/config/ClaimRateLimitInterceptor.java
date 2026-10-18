package com.naling.lightstream.config;

import com.naling.lightstream.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

@Component
public class ClaimRateLimitInterceptor implements HandlerInterceptor {
    private static final String KEY_PREFIX = "ratelimit:claim:";
    private static final int MAX_ATTEMPTS = 10;
    private static final Duration WINDOW = Duration.ofMinutes(5);

    private final StringRedisTemplate stringRedisTemplate;
    public ClaimRateLimitInterceptor(StringRedisTemplate stringRedisTemplate){
        this.stringRedisTemplate = stringRedisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String key = KEY_PREFIX + request.getRemoteAddr();
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1){
            stringRedisTemplate.expire(key, WINDOW);
        }
        if (count != null && count > MAX_ATTEMPTS){
            throw new RateLimitExceededException();
        }
        return true;
    }

}
