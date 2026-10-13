package com.naling.lightstream.service;


import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.time.Duration;

@Service
public class TextShareService {
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom random = new SecureRandom();
    private static final String KEY_PREFIX = "text:";
    private final StringRedisTemplate redisTemplate;
    private final long ttlMinutes;

    public TextShareService(StringRedisTemplate redisTemplate, @Value("${lightstream.text.ttl-minutes}") long ttlMinutes){
        this.redisTemplate = redisTemplate;
        this.ttlMinutes = ttlMinutes;
    }

    public String share(String text){
        String code = generateUniqueCode();
        redisTemplate.opsForValue().set(key(code), text, Duration.ofMinutes(ttlMinutes));
        return code;
    }

    public String claim(String code){
        return redisTemplate.opsForValue().get(key(code));
    }

    private String generateCode(){
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < 4; i++){
            int index = random.nextInt(ALPHABET.length());
            sb.append(ALPHABET.charAt(index));
        }
        return sb.toString();
    }

    private String generateUniqueCode(){
        String code = generateCode();
        while(redisTemplate.hasKey(key(code))){
            code = generateCode();
        }
        return code;
    }

    private String key(String code){
        return KEY_PREFIX + code;
    }


}
