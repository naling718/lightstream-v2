package com.naling.lightstream;

import com.jayway.jsonpath.JsonPath;
import com.naling.lightstream.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class HistoryIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void historyShowsOwnSharesNewestFirst() throws Exception {
        String alice = registerAndLogin("alice@example.com");
        String bob = registerAndLogin("bob@example.com");

        mockMvc.perform(post("/api/v1/text/share")
                        .header("Authorization", "Bearer " + alice)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"hello\"}"))
                .andExpect(status().isCreated());
        MockMultipartFile file = new MockMultipartFile("file", "notes.txt", "text/plain", "hello".getBytes());
        mockMvc.perform(multipart("/api/v1/file/share").file(file)
                        .header("Authorization", "Bearer " + alice))
                .andExpect(status().isCreated());

        mockMvc.perform(get("/api/v1/me/history").header("Authorization", "Bearer " + alice))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(2))
                .andExpect(jsonPath("$[0].type").value("FILE"))
                .andExpect(jsonPath("$[0].fileName").value("notes.txt"))
                .andExpect(jsonPath("$[0].objectKey").doesNotExist())
                .andExpect(jsonPath("$[1].type").value("TEXT"))
                .andExpect(jsonPath("$[1].text").value("hello"));

        mockMvc.perform(get("/api/v1/me/history").header("Authorization", "Bearer " + bob))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.length()").value(0));
    }

    @Test
    void historyRequiresLogin() throws Exception {
        mockMvc.perform(get("/api/v1/me/history"))
                .andExpect(status().isUnauthorized());
    }

    private String registerAndLogin(String email) throws Exception {
        String credentials = "{\"email\":\"" + email + "\",\"password\":\"password123\"}";
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andExpect(status().isCreated());
        MvcResult login = mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andExpect(status().isOk())
                .andReturn();
        return JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");
    }
}

