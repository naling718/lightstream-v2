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
    private final HistoryService historyService;

    public TextShareService( @Value("${lightstream.text.ttl-minutes}") long ttlMinutes,
                            ClaimCodeService claimCodeService, HistoryService historyService){
        this.ttlMinutes = ttlMinutes;
        this.claimCodeService = claimCodeService;
        this.historyService = historyService;
    }

    public String share(Long userId, String text) {
        String code = claimCodeService.reserve(KEY_PREFIX, text, Duration.ofMinutes(ttlMinutes));
        try {
            historyService.recordText(userId, code, text);
        } catch (RuntimeException e) {
            claimCodeService.consume(KEY_PREFIX, code);
            throw e;
        }
        return code;
    }

    public String claim(String code){
        return claimCodeService.consume(KEY_PREFIX, code);
    }
}
