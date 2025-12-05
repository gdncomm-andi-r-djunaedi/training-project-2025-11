package com.blibli.memberModule.integration;

import com.blibli.memberModule.dto.LoginRequestDto;
import com.blibli.memberModule.dto.MemberRequestDto;
import com.blibli.memberModule.entity.Member;
import com.blibli.memberModule.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
@Transactional
class MemberIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    private MemberRequestDto memberRequestDto;
    private LoginRequestDto loginRequestDto;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();

        memberRequestDto = new MemberRequestDto();
        memberRequestDto.setEmail("test@gmail.com");
        memberRequestDto.setPassword("password123");
        memberRequestDto.setName("Test User");
        memberRequestDto.setPhone("1234567890");

        loginRequestDto = new LoginRequestDto();
        loginRequestDto.setEmail("test@gmail.com");
        loginRequestDto.setPassword("password123");
    }

    @Test
    void testRegister_Success() throws Exception {
        String requestBody = objectMapper.writeValueAsString(memberRequestDto);

        mockMvc.perform(
                        post("/api/members/register").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.value.email").value("test@gmail.com"))
                .andExpect(jsonPath("$.value.name").value("Test User"))
                .andExpect(jsonPath("$.value.phone").value("1234567890"))
                .andExpect(jsonPath("$.value.memberId").exists())
                .andExpect(jsonPath("$.errorMessage").doesNotExist());

        Member savedMember = memberRepository.findByEmail("test@gmail.com").orElse(null);
        assertNotNull(savedMember);
        assertEquals("test@gmail.com", savedMember.getEmail());
        assertEquals("Test User", savedMember.getName());
        assertTrue(passwordEncoder.matches("password123", savedMember.getPassword()));
    }

    @Test
    void testRegister_DuplicateEmail() throws Exception {
        Member existingMember = new Member();
        existingMember.setEmail("test@gmail.com");
        existingMember.setPassword(passwordEncoder.encode("password123"));
        existingMember.setName("Existing User");
        existingMember.setPhone("9876543210");
        existingMember.setCreatedAt(LocalDateTime.now());
        existingMember.setUpdatedAt(LocalDateTime.now());
        memberRepository.save(existingMember);

        String requestBody = objectMapper.writeValueAsString(memberRequestDto);

        mockMvc.perform(
                        post("/api/members/register").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").value("Email already exists: test@gmail.com"));
    }

    @Test
    void testRegister_InvalidEmail() throws Exception {
        memberRequestDto.setEmail("invalid-email");

        String requestBody = objectMapper.writeValueAsString(memberRequestDto);

        mockMvc.perform(
                        post("/api/members/register").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void testRegister_ShortPassword() throws Exception {
        memberRequestDto.setPassword("12345");

        String requestBody = objectMapper.writeValueAsString(memberRequestDto);

        mockMvc.perform(
                        post("/api/members/register").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void testLogin_Success() throws Exception {
        Member member = new Member();
        member.setEmail("test@gmail.com");
        member.setPassword(passwordEncoder.encode("password123"));
        member.setName("Test User");
        member.setPhone("1234567890");
        member.setCreatedAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());
        memberRepository.save(member);

        String requestBody = objectMapper.writeValueAsString(loginRequestDto);

        mockMvc.perform(
                        post("/api/members/login").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.value.member.email").value("test@gmail.com"))
                .andExpect(jsonPath("$.value.member.name").value("Test User"))
                .andExpect(jsonPath("$.value.member.memberId").exists())
                .andExpect(jsonPath("$.errorMessage").doesNotExist());
    }

    @Test
    void testLogin_InvalidEmail() throws Exception {
        loginRequestDto.setEmail("nonexistent@gmail.com");

        String requestBody = objectMapper.writeValueAsString(loginRequestDto);

        mockMvc.perform(
                        post("/api/members/login").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").value("Invalid email or password"));
    }

    @Test
    void testLogin_InvalidPassword() throws Exception {
        Member member = new Member();
        member.setEmail("test@gmail.com");
        member.setPassword(passwordEncoder.encode("password123"));
        member.setName("Test User");
        member.setPhone("1234567890");
        member.setCreatedAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());
        memberRepository.save(member);

        loginRequestDto.setPassword("wrongpassword");

        String requestBody = objectMapper.writeValueAsString(loginRequestDto);

        mockMvc.perform(
                        post("/api/members/login").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").value("Invalid email or password"));
    }

    @Test
    void testLogin_InvalidEmailFormat() throws Exception {
        loginRequestDto.setEmail("invalid-email");

        String requestBody = objectMapper.writeValueAsString(loginRequestDto);

        mockMvc.perform(
                        post("/api/members/login").contentType(MediaType.APPLICATION_JSON).content(requestBody))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorCode").value("VALIDATION_ERROR"));
    }

    @Test
    void testLogout_Success() throws Exception {
        Member member = new Member();
        member.setEmail("test@gmail.com");
        member.setPassword(passwordEncoder.encode("password123"));
        member.setName("Test User");
        member.setPhone("1234567890");
        member.setCreatedAt(LocalDateTime.now());
        member.setUpdatedAt(LocalDateTime.now());
        Member savedMember = memberRepository.save(member);

        mockMvc.perform(
                        post("/api/members/logout").param("memberId",
                                String.valueOf(savedMember.getMemberId())))
                .andExpect(status().isOk()).andExpect(jsonPath("$.success").value(true))
                .andExpect(jsonPath("$.value").value("Logout successful"))
                .andExpect(jsonPath("$.errorMessage").doesNotExist());
    }

    @Test
    void testLogout_MemberNotFound() throws Exception {
        mockMvc.perform(post("/api/members/logout").param("memberId", "99999"))
                .andExpect(status().isBadRequest()).andExpect(jsonPath("$.success").value(false))
                .andExpect(jsonPath("$.errorMessage").value("Member not found with id: 99999"));
    }

    @Test
    void testRegister_EmptyRequestBody() throws Exception {
        mockMvc.perform(
                        post("/api/members/register").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }

    @Test
    void testLogin_EmptyRequestBody() throws Exception {
        mockMvc.perform(
                        post("/api/members/login").contentType(MediaType.APPLICATION_JSON).content("{}"))
                .andExpect(status().isBadRequest());
    }
}

