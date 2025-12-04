package com.gdn.faurihakim.member.command.impl;

import com.gdn.faurihakim.Member;
import com.gdn.faurihakim.MemberRepository;
import com.gdn.faurihakim.member.command.model.CreateMemberCommandRequest;
import com.gdn.faurihakim.member.web.model.response.CreateMemberWebResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class CreateMemberCommandImplTest {

    @Mock
    private MemberRepository memberRepository;

    @InjectMocks
    private CreateMemberCommandImpl createMemberCommand;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void execute_shouldCreateMember_whenValidRequest() {
        CreateMemberCommandRequest request = CreateMemberCommandRequest.builder()
                .email("test@example.com")
                .fullName("Test User")
                .phoneNumber("08123456789")
                .password("password")
                .build();

        Member savedMember = Member.builder()
                .id("generated-id")
                .email(request.getEmail())
                .fullName(request.getFullName())
                .phoneNumber(request.getPhoneNumber())
                .build();

        when(memberRepository.save(any(Member.class))).thenReturn(savedMember);

        CreateMemberWebResponse response = createMemberCommand.execute(request);

        assertNotNull(response);
        assertEquals(savedMember.getEmail(), response.getEmail());
        assertEquals(savedMember.getFullName(), response.getFullName());
        assertEquals(savedMember.getPhoneNumber(), response.getPhoneNumber());

        ArgumentCaptor<Member> memberCaptor = ArgumentCaptor.forClass(Member.class);
        verify(memberRepository).save(memberCaptor.capture());

        Member capturedMember = memberCaptor.getValue();
        assertNotNull(capturedMember.getMemberId());
        assertEquals(request.getEmail(), capturedMember.getEmail());
        assertEquals(request.getFullName(), capturedMember.getFullName());
        assertEquals(request.getPhoneNumber(), capturedMember.getPhoneNumber());
    }
}
