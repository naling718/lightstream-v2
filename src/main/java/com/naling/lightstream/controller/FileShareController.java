package com.naling.lightstream.controller;

import com.naling.lightstream.dto.ShareResponse;
import com.naling.lightstream.exception.ClaimNotFoundException;
import com.naling.lightstream.exception.EmptyFileException;
import com.naling.lightstream.model.FileMetadata;
import com.naling.lightstream.service.FileShareService;
import com.naling.lightstream.service.FileStorageService;
import org.springframework.http.*;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.InputStream;
import java.nio.charset.StandardCharsets;

@RestController
@RequestMapping("/api/v1/file")
public class FileShareController {
    private final FileShareService fileShareService;
    private final FileStorageService fileStorageService;

    public FileShareController(FileShareService fileShareService, FileStorageService fileStorageService){
        this.fileShareService = fileShareService;
        this.fileStorageService = fileStorageService;
    }

    @PostMapping("/share")
    @ResponseStatus(HttpStatus.CREATED)
    public ShareResponse share(@RequestParam("file") MultipartFile file) {
        if (file.isEmpty()) {
            throw new EmptyFileException();
        }
        return new ShareResponse(fileShareService.share(file));
    }

    @PostMapping("/claim/{code}")
    public ResponseEntity<StreamingResponseBody> claim(@PathVariable String code) {
        FileMetadata metadata = fileShareService.claim(code);
        if (metadata == null) {
            throw new ClaimNotFoundException(code);
        }

        StreamingResponseBody body = outputStream -> {
            try (InputStream in = fileStorageService.download(metadata.objectKey())) {
                in.transferTo(outputStream);
            }
            fileStorageService.delete(metadata.objectKey());
        };

        ContentDisposition disposition = ContentDisposition.attachment()
                .filename(metadata.fileName(), StandardCharsets.UTF_8)
                .build();

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, disposition.toString())
                .contentType(MediaType.parseMediaType(metadata.contentType()))
                .contentLength(metadata.size())
                .body(body);
    }

}


