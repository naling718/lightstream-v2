package com.naling.lightstream.service;

import org.springframework.stereotype.Service;
import java.security.SecureRandom;
import java.util.HashMap;

@Service
public class TextShareService {
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom random = new SecureRandom();

    HashMap<String, String> map = new HashMap<>();

    public String share(String text){
        String code = generateUniqueCode();
        map.put(code, text);
        return code;
    }

    public String claim(String code){
        return map.get(code);
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
        while(map.containsKey(code)){
            code = generateCode();
        }
        return code;
    }
}
