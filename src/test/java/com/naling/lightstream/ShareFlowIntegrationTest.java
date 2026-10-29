package com.naling.lightstream;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import software.amazon.awssdk.services.s3.S3Client;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.asyncDispatch;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class ShareFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private S3Client s3Client;

    @Value("${lightstream.storage.bucket}")
    private String bucket;

    @Test
    void textCanBeClaimedOnlyOnce() throws Exception {
        MvcResult shared = mockMvc.perform(post("/api/v1/text/share")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"hello\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String code = JsonPath.read(shared.getResponse().getContentAsString(), "$.claimCode");

        mockMvc.perform(post("/api/v1/text/claim/" + code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("hello"));

        mockMvc.perform(post("/api/v1/text/claim/" + code))
                .andExpect(status().isNotFound());
    }

    @Test
    void fileCanBeClaimedOnlyOnceAndIsDeleted() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "hello.txt", "text/plain", "hello".getBytes());
        MvcResult shared = mockMvc.perform(multipart("/api/v1/file/share").file(file))
                .andExpect(status().isCreated())
                .andReturn();
        String code = JsonPath.read(shared.getResponse().getContentAsString(), "$.claimCode");
        assertEquals(1, s3Client.listObjectsV2(r -> r.bucket(bucket)).keyCount());

        MvcResult claimed = mockMvc.perform(post("/api/v1/file/claim/" + code))
                .andExpect(request().asyncStarted())
                .andReturn();
        mockMvc.perform(asyncDispatch(claimed))
                .andExpect(status().isOk())
                .andExpect(content().string("hello"));

        mockMvc.perform(post("/api/v1/file/claim/" + code))
                .andExpect(status().isNotFound());
        assertEquals(0, s3Client.listObjectsV2(r -> r.bucket(bucket)).keyCount());
    }
}
