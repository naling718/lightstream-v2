package com.naling.lightstream.job;

import com.naling.lightstream.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Duration;
import java.time.Instant;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class OrphanCleanupJobTest {
    private OrphanCleanupJob job;

    @Mock
    private FileStorageService fileStorageService;

    @BeforeEach
    void setUp() {
        job = new OrphanCleanupJob(fileStorageService, Duration.ofMinutes(30));
    }

    @Test
    void deletesObjectsOlderThanMaxAge() {
        ArgumentCaptor<Instant> captor = ArgumentCaptor.forClass(Instant.class);

        Instant before = Instant.now();
        job.deleteOrphanedFiles();
        Instant after = Instant.now();

        verify(fileStorageService).deleteObjectsOlderThan(captor.capture());
        Instant cutoff = captor.getValue();
        assertFalse(cutoff.isBefore(before.minus(Duration.ofMinutes(30))));
        assertFalse(cutoff.isAfter(after.minus(Duration.ofMinutes(30))));
    }

    @Test
    void propagatesStorageFailure() {
        when(fileStorageService.deleteObjectsOlderThan(any()))
                .thenThrow(new RuntimeException("storage down"));

        assertThrows(RuntimeException.class, () -> job.deleteOrphanedFiles());
    }


}
