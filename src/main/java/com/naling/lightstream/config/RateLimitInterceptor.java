package com.naling.lightstream.config;

import com.naling.lightstream.exception.RateLimitExceededException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.HandlerInterceptor;

import java.time.Duration;

public class RateLimitInterceptor implements HandlerInterceptor {
    private final String keyPrefix;
    private final int maxAttempts;
    private final Duration window;

    private final StringRedisTemplate stringRedisTemplate;
    public RateLimitInterceptor(StringRedisTemplate stringRedisTemplate, String keyPrefix, int maxAttempts, Duration window){
        this.stringRedisTemplate = stringRedisTemplate;
        this.keyPrefix = keyPrefix;
        this.maxAttempts = maxAttempts;
        this.window = window;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) {
        String key = this.keyPrefix + request.getRemoteAddr();
        Long count = stringRedisTemplate.opsForValue().increment(key);
        if (count != null && count == 1){
            stringRedisTemplate.expire(key, this.window);
        }
        if (count != null && count > this.maxAttempts){
            throw new RateLimitExceededException();
        }
        return true;
    }

}
