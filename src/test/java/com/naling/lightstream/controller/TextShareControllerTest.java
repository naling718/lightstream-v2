package com.naling.lightstream.controller;

import com.naling.lightstream.service.TextShareService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.webmvc.test.autoconfigure.WebMvcTest;
import org.springframework.dao.QueryTimeoutException;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.http.MediaType;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@WebMvcTest(TextShareController.class)
class TextShareControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockitoBean
    private TextShareService textShareService;

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
        // Arrange: the (mocked) service returns a known code
        when(textShareService.share("hello")).thenReturn("AB3K");

        // Act + Assert: send a request, check status and JSON
        mockMvc.perform(post("/api/v1/text/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"hello\"}"))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.claimCode").value("AB3K"));
    }

    @Test
    void unknownCodeReturns404() throws Exception {
        when(textShareService.claim("ZZZZ")).thenReturn(null);

        mockMvc.perform(post("/api/v1/text/claim/ZZZZ"))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("No text for code ZZZZ"));
    }

    @Test
    void claimReturns200() throws Exception{
        when(textShareService.claim("AB3K")).thenReturn("hello");

        mockMvc.perform(post("/api/v1/text/claim/AB3K"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("hello"));
    }

    @Test
    void blankTextReturns400() throws Exception{
        mockMvc.perform(post("/api/v1/text/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"\"}"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Text must not be blank"));
    }

    @Test
    void malformedJsonReturns400() throws Exception{
        mockMvc.perform(post("/api/v1/text/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":"))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message").value("Malformed JSON request"));
    }

    @Test
    void getClaimReturns405() throws Exception{
        mockMvc.perform(get("/api/v1/text/claim/AB3K"))
                .andExpect(status().isMethodNotAllowed());
    }


    @Test
    void tooManyClaimsReturns429() throws Exception{
        when(valueOps.increment(anyString())).thenReturn(11L);

        mockMvc.perform(post("/api/v1/text/claim/AB3K"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "300"));
    }

    @Test
    void tooManySharesReturns429() throws Exception{
        when(valueOps.increment(anyString())).thenReturn(31L);
        mockMvc.perform(post("/api/v1/text/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"hello\"}"))
                .andExpect(status().isTooManyRequests())
                .andExpect(header().string("Retry-After", "300"));
    }


    @Test
    void redisDownReturns503() throws Exception{
        when(textShareService.share("hello")).thenThrow(new QueryTimeoutException("timeout"));

        mockMvc.perform(post("/api/v1/text/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"hello\"}"))
                .andExpect(status().isServiceUnavailable())
                .andExpect(jsonPath("$.message").value("Storage is temporarily unavailable"));
    }

}

