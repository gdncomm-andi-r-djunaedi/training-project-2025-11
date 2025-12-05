package com.gdn.faurihakim.member;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.context.ApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.notNullValue;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@DisplayName("Member Service Integration Tests")
class   MemberIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ApplicationContext applicationContext;

    @Test
    @DisplayName("Should load application context successfully")
    void testContextLoads() {
        // Assert that the context loads correctly
        assertThat(applicationContext).isNotNull();
    }

    @Test
    @DisplayName("Should create member successfully")
    void testCreateMember_Success() throws Exception {
        // Arrange
        String uniqueEmail = "integration" + System.currentTimeMillis() + "@test.com";
        String requestBody = String.format("""
                {
                    "email": "%s",
                    "password": "password123",
                    "fullName": "Integration Test"
                }
                """, uniqueEmail);

        // Act & Assert
        mockMvc.perform(post("/api/members")
                .contentType(MediaType.APPLICATION_JSON)
                .content(requestBody))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.memberId", notNullValue()))
                .andExpect(jsonPath("$.data.email").value(uniqueEmail))
                .andExpect(jsonPath("$.data.fullName").value("Integration Test"));
    }
}
