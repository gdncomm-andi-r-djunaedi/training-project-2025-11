package com.gdn.training.member.unit;

import com.gdn.training.member.domain.port.out.MemberRepository;
import com.gdn.training.member.domain.model.Member;
import com.gdn.training.member.domain.password.PasswordHasher;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.mockito.Mockito;

import com.gdn.training.member.application.dto.request.RegisterMemberRequest;
import com.gdn.training.member.application.usecase.impl.RegisterMemberUseCaseImpl;

public class RegisterMemberUseCaseImplTest {
    private MemberRepository repo;
    private PasswordHasher hasher;
    private RegisterMemberUseCaseImpl useCase;

    @BeforeEach
    public void setUp() {
        repo = Mockito.mock(MemberRepository.class);
        hasher = Mockito.mock(PasswordHasher.class);
        useCase = new RegisterMemberUseCaseImpl(repo, hasher);
    }

    @Test
    public void registerSucceedWhenEmailNotExists() {
        when(repo.findByEmail(anyString()))
                .thenReturn(Optional.empty());

        when(hasher.hash(anyString()))
                .thenReturn("hashed");

        // stub save to return domain obj with same value
        when(repo.save(any(Member.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        var response = useCase.register(new RegisterMemberRequest(
                "fullName",
                "email",
                "password",
                "phoneNumber"));

        assertNotNull(response);
        assertEquals("email", response.email());
        assertEquals("fullName", response.fullName());
        verify(repo, times(1)).save(any(Member.class));
    }

    @Test
    public void registerFailedWhenEmailExists() {
        Member existing = new Member(
                UUID.randomUUID(),
                "fullName",
                "email",
                "password",
                null,
                null,
                LocalDateTime.now(),
                LocalDateTime.now());
        when(repo.findByEmail(anyString()))
                .thenReturn(Optional.of(existing));

        assertThrows(RuntimeException.class, () -> {
            useCase.register(new RegisterMemberRequest(
                    "fullName",
                    "email",
                    "password",
                    null));
        });
        verify(repo, never()).save(any(Member.class));
    }
}
