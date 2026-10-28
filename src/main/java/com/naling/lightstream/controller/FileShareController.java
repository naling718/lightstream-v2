package com.naling.lightstream.controller;

import com.naling.lightstream.dto.ShareResponse;
import com.naling.lightstream.exception.EmptyFileException;
import com.naling.lightstream.service.FileShareService;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

@RestController
@RequestMapping("/api/v1/file")
public class FileShareController {
    private final FileShareService fileShareService;

    public FileShareController(FileShareService fileShareService){
        this.fileShareService = fileShareService;
    }

    @PostMapping("/share")
    @ResponseStatus(HttpStatus.CREATED)
    public ShareResponse share(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new EmptyFileException();
        }
        return new ShareResponse(fileShareService.share(file));
    }
}
