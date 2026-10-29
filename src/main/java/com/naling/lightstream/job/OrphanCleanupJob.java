package com.naling.lightstream.job;

import com.naling.lightstream.service.FileStorageService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;

@Component
public class OrphanCleanupJob {
    private static final Logger log = LoggerFactory.getLogger(OrphanCleanupJob.class);

    private final FileStorageService fileStorageService;
    private final Duration maxAge;

    public OrphanCleanupJob(FileStorageService fileStorageService, @Value("${lightstream.cleanup.max-age}") Duration maxAge) {
        this.fileStorageService = fileStorageService;
        this.maxAge = maxAge;
    }

    @Scheduled(fixedDelayString = "${lightstream.cleanup.interval}")
    public void deleteOrphanedFiles() {
        Instant cutoff = Instant.now().minus(maxAge);
        int deleted = fileStorageService.deleteObjectsOlderThan(cutoff);
        if (deleted > 0) {
            log.info("Deleted {} orphaned file(s) older than {}", deleted, maxAge);
        }
    }
}

