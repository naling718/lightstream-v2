package com.naling.lightstream.service;

import com.naling.lightstream.model.FileMetadata;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;
import tools.jackson.databind.ObjectMapper;

import java.time.Duration;

@Service
public class FileShareService {
    private static final String KEY_PREFIX = "file:";

    private final FileStorageService fileStorageService;
    private final ClaimCodeService claimCodeService;
    private final ObjectMapper objectMapper;
    private final long ttlMinutes;


    public FileShareService(FileStorageService fileStorageService, ClaimCodeService claimCodeService,
                            ObjectMapper objectMapper, @Value("${lightstream.file.ttl-minutes}") long ttlMinutes){
        this.fileStorageService = fileStorageService;
        this.claimCodeService = claimCodeService;
        this.objectMapper = objectMapper;
        this.ttlMinutes = ttlMinutes;
    }

    public String share(MultipartFile file) {
        String objectKey = fileStorageService.upload(file);
        String fileName = file.getOriginalFilename() != null ? file.getOriginalFilename() : "file";
        String contentType = file.getContentType() != null ? file.getContentType() : "application/octet-stream";
        try {
            FileMetadata metadata = new FileMetadata(
                    objectKey,
                    fileName,
                    contentType,
                    file.getSize());
            String json = objectMapper.writeValueAsString(metadata);
            return claimCodeService.reserve(KEY_PREFIX, json, Duration.ofMinutes(ttlMinutes));
        } catch (RuntimeException e) {
            fileStorageService.delete(objectKey);
            throw e;
        }
    }
}
