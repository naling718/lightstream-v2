package com.naling.lightstream;

import com.jayway.jsonpath.JsonPath;
import com.naling.lightstream.user.User;
import com.naling.lightstream.user.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Import(TestcontainersConfiguration.class)
class AuthFlowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void registerStoresBcryptHashAndRejectsDuplicate() throws Exception {
        String body = "{\"email\":\"alice@example.com\",\"password\":\"password123\"}";

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isCreated());

        User user = userRepository.findByEmail("alice@example.com").orElseThrow();
        assertNotEquals("password123", user.getPasswordHash());
        assertTrue(user.getPasswordHash().startsWith("$2a$"));
        assertTrue(passwordEncoder.matches("password123", user.getPasswordHash()));

        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(body))
                .andExpect(status().isConflict());
    }

    @Test
    void loggedInUserCanShareAndAnyoneCanClaim() throws Exception {
        String credentials = "{\"email\":\"bob@example.com\",\"password\":\"password123\"}";
        mockMvc.perform(post("/api/v1/auth/register").contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andExpect(status().isCreated());

        MvcResult login = mockMvc.perform(post("/api/v1/auth/login").contentType(MediaType.APPLICATION_JSON).content(credentials))
                .andExpect(status().isOk())
                .andReturn();
        String token = JsonPath.read(login.getResponse().getContentAsString(), "$.accessToken");

        MvcResult shared = mockMvc.perform(post("/api/v1/text/share")
                        .header("Authorization", "Bearer " + token)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"hello\"}"))
                .andExpect(status().isCreated())
                .andReturn();
        String code = JsonPath.read(shared.getResponse().getContentAsString(), "$.claimCode");

        mockMvc.perform(post("/api/v1/text/claim/" + code))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.text").value("hello"));
    }

    @Test
    void invalidTokenReturns401() throws Exception {
        mockMvc.perform(post("/api/v1/text/share")
                        .header("Authorization", "Bearer not-a-real-token")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("{\"text\":\"hello\"}"))
                .andExpect(status().isUnauthorized())
                .andExpect(jsonPath("$.message").value("Login required"));
    }

}

