package com.marketplace.member.integration;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.marketplace.member.config.TestSecurityConfig;
import com.marketplace.member.dto.LoginRequest;
import com.marketplace.member.dto.RegisterRequest;
import com.marketplace.member.repository.MemberRepository;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@Testcontainers
@ActiveProfiles("test")
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Import(TestSecurityConfig.class)
class MemberIntegrationTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.data.redis.host", redis::getHost);
        registry.add("spring.data.redis.port", () -> redis.getMappedPort(6379));
    }

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    private static String accessToken;
    private static String refreshToken;

    @Test
    @Order(1)
    @DisplayName("Should register a new member")
    void shouldRegisterMember() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("integration@test.com")
                .password("password123")
                .firstName("Integration")
                .lastName("Test")
                .build();

        mockMvc.perform(post("/api/members/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("integration@test.com"));
    }

    @Test
    @Order(2)
    @DisplayName("Should fail to register with existing email")
    void shouldFailToRegisterWithExistingEmail() throws Exception {
        RegisterRequest request = RegisterRequest.builder()
                .email("integration@test.com")
                .password("password123")
                .firstName("Integration")
                .lastName("Test")
                .build();

        mockMvc.perform(post("/api/members/register")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.success").value(false));
    }

    @Test
    @Order(3)
    @DisplayName("Should login successfully and get new tokens")
    void shouldLoginSuccessfully() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("integration@test.com")
                .password("password123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.accessToken").exists())
                .andExpect(jsonPath("$.data.refreshToken").exists())
                .andReturn();

        String response = result.getResponse().getContentAsString();
        accessToken = objectMapper.readTree(response).path("data").path("accessToken").asText();
        refreshToken = objectMapper.readTree(response).path("data").path("refreshToken").asText();
    }

    @Test
    @Order(4)
    @DisplayName("Should return same tokens on second login (token reuse)")
    void shouldReturnSameTokensOnSecondLogin() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("integration@test.com")
                .password("password123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        String secondAccessToken = objectMapper.readTree(response).path("data").path("accessToken").asText();
        String secondRefreshToken = objectMapper.readTree(response).path("data").path("refreshToken").asText();

        // Should return same tokens
        assertThat(secondAccessToken).isEqualTo(accessToken);
        assertThat(secondRefreshToken).isEqualTo(refreshToken);
    }

    @Test
    @Order(5)
    @DisplayName("Should get current member profile")
    void shouldGetCurrentMemberProfile() throws Exception {
        mockMvc.perform(get("/api/members/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.data.email").value("integration@test.com"));
    }

    @Test
    @Order(6)
    @DisplayName("Should logout successfully")
    void shouldLogoutSuccessfully() throws Exception {
        mockMvc.perform(post("/api/members/logout")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true));
    }

    @Test
    @Order(7)
    @DisplayName("Should fail to use token after logout")
    void shouldFailToUseTokenAfterLogout() throws Exception {
        mockMvc.perform(get("/api/members/me")
                        .header("Authorization", "Bearer " + accessToken))
                .andExpect(status().isUnauthorized());
    }

    @Test
    @Order(8)
    @DisplayName("Should get new tokens after logout and re-login")
    void shouldGetNewTokensAfterLogoutAndReLogin() throws Exception {
        LoginRequest request = LoginRequest.builder()
                .email("integration@test.com")
                .password("password123")
                .build();

        MvcResult result = mockMvc.perform(post("/api/members/login")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.success").value(true))
                .andReturn();

        String response = result.getResponse().getContentAsString();
        String newAccessToken = objectMapper.readTree(response).path("data").path("accessToken").asText();

        // Should get NEW tokens since previous ones were invalidated
        assertThat(newAccessToken).isNotEqualTo(accessToken);
    }
}
