package com.naling.lightstream.service;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;

import java.security.SecureRandom;
import java.time.Duration;

@Service
public class ClaimCodeService {
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private static final int CODE_LENGTH = 4;
    private static final int MAX_ATTEMPTS = 10;

    private final SecureRandom random = new SecureRandom();
    private final StringRedisTemplate redisTemplate;

    public ClaimCodeService(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    public String reserve(String keyPrefix, String value, Duration ttl) {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String code = generateCode();
            Boolean stored = redisTemplate.opsForValue()
                    .setIfAbsent(keyPrefix + code, value, ttl);
            if (Boolean.TRUE.equals(stored)) {
                return code;
            }
        }
        throw new IllegalStateException("Could not generate a unique claim code");
    }

    private String generateCode() {
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < CODE_LENGTH; i++){
            int index = random.nextInt(ALPHABET.length());
            sb.append(ALPHABET.charAt(index));
        }
        return sb.toString();
    }

    public String consume(String keyPrefix, String code) {
        return redisTemplate.opsForValue().getAndDelete(keyPrefix + code);
    }

}

