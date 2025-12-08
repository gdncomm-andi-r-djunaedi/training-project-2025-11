package MemberService.MemberService.controller;

import MemberService.MemberService.common.ApiResponse;
import MemberService.MemberService.dto.LoginDto;
import MemberService.MemberService.dto.RegisterDto;
import MemberService.MemberService.entity.Member;
import MemberService.MemberService.security.JWTUtil;
import MemberService.MemberService.service.MemberService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;

@ExtendWith(MockitoExtension.class)
public class MemberControllerTest {

    @Mock
    private MemberService memberService;

    @Mock
    private JWTUtil jwtUtil;

    @InjectMocks
    private MemberController memberController;

    private RegisterDto registerDto;
    private LoginDto loginDto;

    @BeforeEach
    void setUp() {
        registerDto = new RegisterDto();
        registerDto.setFullName("testuser");
        registerDto.setPassword("password");
        registerDto.setEmail("email@gmail.com");

        loginDto = new LoginDto();
        loginDto.setEmail("email@gmail.com");
        loginDto.setPassword("password");
    }

    @Test
    void testRegister() {
        Member fakeMember = new Member();
        fakeMember.setId("dfgchv");
        fakeMember.setFullName("testuser");
        fakeMember.setEmail("testuser@example.com");

        Mockito.when(memberService.register(any(RegisterDto.class))).thenReturn(fakeMember);

        ResponseEntity<ApiResponse<?>> response = memberController.register(registerDto);

        assertEquals(200, response.getStatusCodeValue());
        Member returnedMember = (Member) response.getBody().getData();
        assertNotNull(returnedMember);
        assertEquals("testuser", returnedMember.getFullName());
        assertEquals("testuser@example.com", returnedMember.getEmail());
    }


    @Test
    void testLogin() {
        Mockito.when(memberService.login(any(LoginDto.class))).thenReturn("fake-jwt-token");

        ResponseEntity<ApiResponse<?>> response = memberController.login(loginDto);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("fake-jwt-token", response.getBody().getData());
    }

    @Test
    void testGetProfile() {
        Member fakeMember = new Member();
        fakeMember.setId("dfgchv");
        fakeMember.setFullName("testuser");
        fakeMember.setEmail("testuser@example.com");
        String userId = "123";
        Mockito.when(memberService.getProfile(userId)).thenReturn(fakeMember);

        ResponseEntity<ApiResponse<?>> response = memberController.getProfile(userId);

        assertEquals(200, response.getStatusCodeValue());
    }
}

