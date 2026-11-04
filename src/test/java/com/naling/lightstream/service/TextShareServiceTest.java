package com.naling.lightstream.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class TextShareServiceTest {
    private TextShareService service;

    @Mock
    private ClaimCodeService claimCodeService;

    @Mock
    private HistoryService historyService;

    @BeforeEach
    void setUp(){
        service = new TextShareService(15, claimCodeService, historyService);
    }

    @Test
    void shareReservesCodeWithTextPrefix() {
        when(claimCodeService.reserve("text:", "hello", Duration.ofMinutes(15))).thenReturn("AB3K");
        String code = service.share(1L, "hello");
        assertEquals("AB3K", code);
    }


    @Test
    void claimReturnsStoredText(){
        when(claimCodeService.consume("text:", "AB3K")).thenReturn("hello");
        // Act
        String text = service.claim("AB3K");
        // Assert
        assertEquals("hello", text);;
    }

    @Test
    void claimUnknownCodeReturnsNull(){
        when(claimCodeService.consume("text:", "XXXX")).thenReturn(null);
        // Act
        String text = service.claim("XXXX");
        // Assert
        assertNull(text);
    }

    @Test
    void shareRecordsHistory() {
        when(claimCodeService.reserve("text:", "hello", Duration.ofMinutes(15))).thenReturn("AB3K");

        service.share(1L, "hello");

        verify(historyService).recordText(1L, "AB3K", "hello");
    }

    @Test
    void shareReleasesCodeWhenHistoryFails() {
        when(claimCodeService.reserve("text:", "hello", Duration.ofMinutes(15))).thenReturn("AB3K");
        doThrow(new DataAccessResourceFailureException("database down"))
                .when(historyService).recordText(1L, "AB3K", "hello");

        assertThrows(DataAccessResourceFailureException.class, () -> service.share(1L, "hello"));
        verify(claimCodeService).consume("text:", "AB3K");
    }



}
