package com.naling.lightstream.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.time.Duration;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final StringRedisTemplate redisTemplate;

    public WebConfig(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new RateLimitInterceptor(redisTemplate, "ratelimit:claim:", 10, Duration.ofMinutes(5)))
                .addPathPatterns("/api/v1/text/claim/**");
        registry.addInterceptor(new RateLimitInterceptor(redisTemplate, "ratelimit:share:", 30, Duration.ofMinutes(5)))
                .addPathPatterns("/api/v1/text/share", "/api/v1/file/share");
    }
}
