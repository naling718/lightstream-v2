package com.naling.lightstream.controller;

import com.naling.lightstream.service.TextShareService;
import com.naling.lightstream.dto.ClaimResponse;
import com.naling.lightstream.dto.ShareRequest;
import com.naling.lightstream.dto.ShareResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.server.ResponseStatusException;

@RestController
@RequestMapping("/api/v1/text")
public class TextShareController {
    private final TextShareService textShareService;

    public TextShareController(TextShareService textShareService){
        this.textShareService = textShareService;
    }

    @PostMapping("/share")
    @ResponseStatus(HttpStatus.CREATED)
    public ShareResponse share(@RequestBody ShareRequest request){
        if (request.text() == null || request.text().isBlank()){
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "The text must be not empty.");
        }
        String code = textShareService.share(request.text());
        return new ShareResponse(code);
    }

    @PostMapping("/claim/{code}")
    public ClaimResponse claim(@PathVariable String code){
        String text = textShareService.claim(code);
        if (text == null){
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "No text for code " + code);
        }
        return new ClaimResponse(text);
    }
}