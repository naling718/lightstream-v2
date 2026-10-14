package com.naling.lightstream.service;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
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
    private StringRedisTemplate redisTemplate;

    @Mock
    private ValueOperations<String, String> valueOps;

    @BeforeEach
    void setUp(){
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        service = new TextShareService(redisTemplate, 15);
    }

    @Test
    void shareStoresTextOnceAndReturnsItsCode(){
        // Arrange
        when(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(true);
        // Act
        String code = service.share("hello");
        // Assert
        verify(valueOps, times(1)).setIfAbsent("text:"+code, "hello", Duration.ofMinutes(15));
        assertEquals(4, code.length());
    }

    @Test
    void shareRetriesWhenCodeIsTaken(){
        // Arrange
        when(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(false, true);
        // Act
        String code = service.share("hello");
        // Assert
        verify(valueOps, times(2)).setIfAbsent(anyString(), eq("hello"), eq(Duration.ofMinutes(15)));

        assertNotNull(code);
    }

    @Test
    void shareGivesUpAfterMaxAttempts(){
        // Arrange
        when(valueOps.setIfAbsent(anyString(), anyString(), any(Duration.class))).thenReturn(false);
        // Assert
        assertThrows(IllegalStateException.class, () -> service.share("hello"));
        verify(valueOps, times(10)).setIfAbsent(anyString(), eq("hello"), eq(Duration.ofMinutes(15)));
    }

    @Test
    void claimReturnsStoredText(){
        // Arrange
        when(valueOps.getAndDelete("text:AB3K")).thenReturn("hello");
        // Act
        String text = service.claim("AB3K");
        // Assert
        assertEquals(text, "hello");
    }

    @Test
    void claimUnknownCodeReturnsNull(){
        // Arrange
        when(valueOps.getAndDelete("text:XXXX")).thenReturn(null);
        // Act
        String text = service.claim("XXXX");
        // Assert
        assertNull(text);
    }
}
