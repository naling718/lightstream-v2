package com.naling.lightstream.controller;

import com.naling.lightstream.model.FileMetadata;
import com.naling.lightstream.service.FileShareService;
import com.naling.lightstream.service.FileStorageService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.web.multipart.MaxUploadSizeExceededException;
import software.amazon.awssdk.core.ResponseInputStream;
import software.amazon.awssdk.http.AbortableInputStream;
import software.amazon.awssdk.services.s3.model.GetObjectResponse;

import java.io.ByteArrayInputStream;

import static org.hamcrest.Matchers.containsString;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(FileShareController.class)
class FileShareControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private FileShareService fileShareService;

    @MockitoBean
    private FileStorageService fileStorageService;

    @MockitoBean
    private StringRedisTemplate redisTemplate;

    private ValueOperations<String, String> valueOps;

    @BeforeEach
    @SuppressWarnings("unchecked")
    void setUp() {
        valueOps = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOps);
        when(valueOps.increment(anyString())).thenReturn(1L);
    }

    @Test
    void shareReturns201WithCode() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", "text/plain", "hello".getBytes());
        when(fileShareService.share(any())).thenReturn("AB3K");

        mockMvc.perform(multipart("/api/v1/file/share").file(file))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.claimCode").value("AB3K"));
    }

    @Test
    void emptyFileReturns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "empty.txt", "text/plain", new byte[0]);

        mockMvc.perform(multipart("/api/v1/file/share").file(file))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("File must not be empty"));
        verify(fileShareService, never()).share(any());
    }

    @Test
    void wrongPartNameReturns400() throws Exception {
        MockMultipartFile file = new MockMultipartFile("document", "hello.txt", "text/plain", "hello".getBytes());

        mockMvc.perform(multipart("/api/v1/file/share").file(file))
                .andExpect(status().isBadRequest());
    }

    @Test
    void jsonBodyReturns400() throws Exception {
        mockMvc.perform(post("/api/v1/file/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"hello\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Request must be multipart/form-data with a 'file' part"));
    }

    @Test
    void tooLargeFileReturns413() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "big.bin", "application/octet-stream", "x".getBytes());
        when(fileShareService.share(any())).thenThrow(new MaxUploadSizeExceededException(50 * 1024 * 1024));

        mockMvc.perform(multipart("/api/v1/file/share").file(file))
                .andExpect(status().isContentTooLarge())
                .andExpect(jsonPath("$.message").value("File must be at most 50 MB"));
    }

    @Test
    void redisDownOnShareReturns503() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", "text/plain", "hello".getBytes());
        when(fileShareService.share(any())).thenThrow(new QueryTimeoutException("timeout"));

        mockMvc.perform(multipart("/api/v1/file/share").file(file))
                .andExpect(status().isServiceUnavailable());
    }

    @Test
    void claimStreamsFileAndDeletesIt() throws Exception {
        when(fileShareService.claim("AB3K"))
                .thenReturn(new FileMetadata("obj-1", "hello.txt", "text/plain", 5));
        when(fileStorageService.download("obj-1"))
                .thenReturn(new ResponseInputStream<>(GetObjectResponse.builder().build(),
                        AbortableInputStream.create(new ByteArrayInputStream("hello".getBytes()))));

        MvcResult result = mockMvc.perform(post("/api/v1/file/claim/AB3K"))
                .andExpect(request().asyncStarted())
                .andReturn();

        mockMvc.perform(asyncDispatch(result))
                .andExpect(status().isOk())
                .andExpect(header().string("Content-Disposition", containsString("filename=\"hello.txt\"")))
                .andExpect(content().contentType("text/plain"))
                .andExpect(content().string("hello"));

        verify(fileStorageService).delete("obj-1");
    }

    @Test
    void unknownCodeReturns404() throws Exception {
        when(fileShareService.claim("ZZZZ")).thenReturn(null);

        mockMvc.perform(post("/api/v1/file/claim/ZZZZ"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No share found for code ZZZZ"));
        verify(fileStorageService, never()).download(anyString());
    }

    @Test
    void tooManyClaimsReturns429() throws Exception {
        when(valueOps.increment(anyString())).thenReturn(11L);

        mockMvc.perform(post("/api/v1/file/claim/AB3K"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "300"));
        verify(fileShareService, never()).claim(anyString());
    }
}
