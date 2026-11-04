package com.naling.lightstream.service;

import com.naling.lightstream.model.FileMetadata;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.dao.DataAccessResourceFailureException;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.mock.web.MockMultipartFile;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

import java.time.Duration;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileShareServiceTest {
    private FileShareService service;
    private final ObjectMapper objectMapper = JsonMapper.builder().build();

    @Mock
    private FileStorageService fileStorageService;

    @Mock
    private ClaimCodeService claimCodeService;

    @Mock
    private HistoryService historyService;

    @BeforeEach
    void setUp() {
        service = new FileShareService(fileStorageService, claimCodeService, objectMapper,15, historyService);
    }

    @Test
    void shareUploadsAndReservesCode() {
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", "text/plain", "hello".getBytes());
        when(fileStorageService.upload(file)).thenReturn("obj-1");
        when(claimCodeService.reserve(eq("file:"), anyString(), eq(Duration.ofMinutes(15)))).thenReturn("AB3K");

        assertEquals("AB3K", service.share(1L, file));

        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(claimCodeService).reserve(eq("file:"), json.capture(), eq(Duration.ofMinutes(15)));
        assertEquals(new FileMetadata("obj-1", "hello.txt", "text/plain", 5),
                objectMapper.readValue(json.getValue(), FileMetadata.class));
    }

    @Test
    void shareDeletesUploadWhenReserveFails() {
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", "text/plain", "hello".getBytes());
        when(fileStorageService.upload(file)).thenReturn("obj-1");
        when(claimCodeService.reserve(anyString(), anyString(), any())).thenThrow(new QueryTimeoutException("timeout"));

        assertThrows(QueryTimeoutException.class, () -> service.share(1L, file));
        verify(fileStorageService).delete("obj-1");
    }

    @Test
    void shareFallsBackWhenNameAndTypeMissing() {
        MockMultipartFile file = new MockMultipartFile("file", null, null, "hello".getBytes());
        when(fileStorageService.upload(file)).thenReturn("obj-1");
        when(claimCodeService.reserve(anyString(), anyString(), any())).thenReturn("AB3K");

        service.share(1L, file);

        ArgumentCaptor<String> json = ArgumentCaptor.forClass(String.class);
        verify(claimCodeService).reserve(anyString(), json.capture(), any());
        FileMetadata metadata = objectMapper.readValue(json.getValue(), FileMetadata.class);
        assertEquals("file", metadata.fileName());
        assertEquals("application/octet-stream", metadata.contentType());
    }

    @Test
    void claimReturnsMetadata() {
        when(claimCodeService.consume("file:", "AB3K"))
                .thenReturn("{\"objectKey\":\"obj-1\",\"fileName\":\"hello.txt\",\"contentType\":\"text/plain\",\"size\":5}");

        assertEquals(new FileMetadata("obj-1", "hello.txt", "text/plain", 5), service.claim("AB3K"));
    }

    @Test
    void claimUnknownCodeReturnsNull() {
        when(claimCodeService.consume("file:", "XXXX")).thenReturn(null);

        assertNull(service.claim("XXXX"));
    }

    @Test
    void shareRecordsHistory() {
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", "text/plain", "hello".getBytes());
        when(fileStorageService.upload(file)).thenReturn("obj-1");
        when(claimCodeService.reserve(anyString(), anyString(), any())).thenReturn("AB3K");

        service.share(1L, file);

        verify(historyService).recordFile(1L, "AB3K", new FileMetadata("obj-1", "hello.txt", "text/plain", 5));
    }

    @Test
    void shareCleansUpWhenHistoryFails() {
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", "text/plain", "hello".getBytes());
        when(fileStorageService.upload(file)).thenReturn("obj-1");
        when(claimCodeService.reserve(anyString(), anyString(), any())).thenReturn("AB3K");
        doThrow(new DataAccessResourceFailureException("database down"))
                .when(historyService).recordFile(any(), any(), any());

        assertThrows(DataAccessResourceFailureException.class, () -> service.share(1L, file));
        verify(claimCodeService).consume("file:", "AB3K");
        verify(fileStorageService).delete("obj-1");
    }

}
