package com.naling.lightstream.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.time.Duration;

@Service
public class TextShareService {
    private static final String KEY_PREFIX = "text:";
    private final long ttlMinutes;
    private final ClaimCodeService claimCodeService;

    public TextShareService( @Value("${lightstream.text.ttl-minutes}") long ttlMinutes,
                            ClaimCodeService claimCodeService){
        this.ttlMinutes = ttlMinutes;
        this.claimCodeService = claimCodeService;
    }

    public String share(String text) {
        return claimCodeService.reserve(KEY_PREFIX, text, Duration.ofMinutes(ttlMinutes));
    }

    public String claim(String code){
        return claimCodeService.consume(KEY_PREFIX, code);
    }
}
