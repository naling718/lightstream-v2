package com.naling.lightstream.controller;

import com.naling.lightstream.dto.ClaimResponse;
import com.naling.lightstream.dto.ShareRequest;
import com.naling.lightstream.dto.ShareResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

import java.security.SecureRandom;
import java.util.HashMap;

@RestController
@RequestMapping("/api/v1/text")
public class TextShareController {
    private static final String ALPHABET = "ABCDEFGHJKLMNPQRSTUVWXYZ23456789";
    private final SecureRandom random = new SecureRandom();

    HashMap<String, String> map = new HashMap<>();

    @PostMapping ("/share")
    @ResponseStatus(HttpStatus.CREATED)
    public ShareResponse share(@RequestBody ShareRequest request){
        String text = request.text();
        if (request == null || text.isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The text is null");
        }
        String code = generateUniqueCode();
        map.put(code, request.text());
        return new ShareResponse(code);
    }

    @GetMapping("/claim/{code}")
    public ClaimResponse claim(@PathVariable String code){
        String text = map.get(code);
        if (text == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No text for code " + code);
        }
        return new ClaimResponse(map.get(code));
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