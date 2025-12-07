package com.blibli.ecommerceProject.services;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.junit.jupiter.api.Assertions.*;

import com.blibli.ecommerceProject.dto.MemberProfiledto;
import com.blibli.ecommerceProject.dto.MemberValidationRequestdto;
import com.blibli.ecommerceProject.dto.Memberdto;
import com.blibli.ecommerceProject.entity.Member;
import com.blibli.ecommerceProject.repositories.MemberRepository;
import com.blibli.ecommerceProject.services.impl.MemberServiceImpl;
import com.blibli.ecommerceProject.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.beans.BeanUtils;

public class MemberServiceImplTest {

    @InjectMocks
    private MemberServiceImpl memberService;

    @Mock
    private MemberRepository memberRepository;

    @Mock
    private PasswordEncoder passwordEncoder;

    @Mock
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }


    //registerMember()
    @Test
    void testRegisterMember_Success() {
        Memberdto dto = new Memberdto();
        dto.setEmailId("test@example.com");
        dto.setPassword("Password@1234");

        Member memberEntity = new Member();
        BeanUtils.copyProperties(dto, memberEntity);
        memberEntity.setPassword("encodedpass");

        Member savedMember = new Member();
        savedMember.setEmailId("test@example.com");
        savedMember.setPassword("encodedpass");

        when(passwordEncoder.encode("plainpass")).thenReturn("encodedpass");
        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        Memberdto result = memberService.registerMember(dto);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmailId());
        assertNull(result.getPassword());
    }


    //validateCredentials()
    @Test
    void testValidateCredentials_Success() {
        MemberValidationRequestdto req = new MemberValidationRequestdto();
        req.setEmailId("test@example.com");
        req.setPassword("inputPass");

        Member stored = new Member();
        stored.setEmailId("test@example.com");
        stored.setPassword("encodedPass");

        when(memberRepository.findByEmailId("test@example.com")).thenReturn(stored);
        when(passwordEncoder.matches("inputPass", "encodedPass")).thenReturn(true);

        boolean result = memberService.validateCredentials(req);
        assertTrue(result);
    }

    @Test
    void testValidateCredentials_Fail_InvalidPassword() {
        MemberValidationRequestdto req = new MemberValidationRequestdto();
        req.setEmailId("test@example.com");
        req.setPassword("wrongPass");

        Member stored = new Member();
        stored.setEmailId("test@example.com");
        stored.setPassword("encodedPass");

        when(memberRepository.findByEmailId("test@example.com")).thenReturn(stored);
        when(passwordEncoder.matches("wrongPass", "encodedPass")).thenReturn(false);

        boolean result = memberService.validateCredentials(req);
        assertFalse(result);
    }

    @Test
    void testValidateCredentials_Fail_UserNotFound() {
        MemberValidationRequestdto req = new MemberValidationRequestdto();
        req.setEmailId("missing@example.com");
        req.setPassword("any");

        when(memberRepository.findByEmailId("missing@example.com")).thenReturn(null);
        boolean result = memberService.validateCredentials(req);
        assertFalse(result);
    }


    //getUserProfile()
    @Test
    void testGetUserProfile_Success() {
        String token = "Bearer abc.def.ghi";

        when(jwtUtil.getUserNameFromToken("abc.def.ghi"))
                .thenReturn("test@example.com");

        Member member = new Member();
        member.setEmailId("test@example.com");

        when(memberRepository.findByEmailId("test@example.com"))
                .thenReturn(member);
        MemberProfiledto profile =
                new MemberProfiledto("John", "test@example.com", "Karnataka", "9999999932");
        when(memberRepository.findDetailsByEmailId("test@example.com"))
                .thenReturn(profile);

        MemberProfiledto result = memberService.getUserProfile(token);

        assertNotNull(result);
        assertEquals("test@example.com", result.getEmailId());
        assertEquals("John", result.getUsername());
        assertEquals("Karnataka", result.getAddress());
        assertEquals("9999999932", result.getPhoneNo());

        verify(jwtUtil).getUserNameFromToken("abc.def.ghi");
        verify(memberRepository).findByEmailId("test@example.com");
        verify(memberRepository).findDetailsByEmailId("test@example.com");
    }


    @Test
    void testGetUserProfile_Fail_UserNotFound() {
        String token = "Bearer xyz";

        when(jwtUtil.getUserNameFromToken("xyz")).thenReturn("missing@example.com");
        when(memberRepository.findByEmailId("missing@example.com")).thenReturn(null);

        MemberProfiledto result = memberService.getUserProfile(token);
        assertNull(result);
    }

    @Test
    void testGetUserProfile_Fail_ProfileNotFound() {
        String token = "Bearer abc";
        when(jwtUtil.getUserNameFromToken("abc")).thenReturn("test@example.com");

        Member member = new Member();
        member.setEmailId("test@example.com");

        when(memberRepository.findByEmailId("test@example.com")).thenReturn(member);
        when(memberRepository.findDetailsByEmailId("test@example.com")).thenReturn(null);

        MemberProfiledto result = memberService.getUserProfile(token);
        assertNull(result);
    }
}
