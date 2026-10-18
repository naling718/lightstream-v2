package com.naling.lightstream.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final ClaimRateLimitInterceptor claimRateLimitInterceptor;

    public WebConfig(ClaimRateLimitInterceptor claimRateLimitInterceptor) {
        this.claimRateLimitInterceptor = claimRateLimitInterceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(claimRateLimitInterceptor)
                .addPathPatterns("/api/v1/text/claim/**");
    }
}
