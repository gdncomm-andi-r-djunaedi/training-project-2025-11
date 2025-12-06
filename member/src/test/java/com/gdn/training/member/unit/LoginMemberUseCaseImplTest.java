package com.gdn.training.member.unit;

import com.gdn.training.member.application.dto.request.LoginMemberRequest;
import com.gdn.training.member.application.usecase.impl.LoginMemberUseCaseImpl;
import com.gdn.training.member.domain.port.out.MemberRepository;
import com.gdn.training.member.domain.password.PasswordHasher;
import com.gdn.training.member.domain.model.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class LoginMemberUseCaseImplTest {

    private MemberRepository repo;
    private PasswordHasher hasher;
    private LoginMemberUseCaseImpl useCase;

    @BeforeEach
    public void setUp() {
        repo = Mockito.mock(MemberRepository.class);
        hasher = Mockito.mock(PasswordHasher.class);
        useCase = new LoginMemberUseCaseImpl(repo, hasher);
    }

    @Test
    public void loginSucceedWhenEmailAndPasswordMatch() {
        UUID id = UUID.randomUUID();
        Member m = new Member(id, "A", "a@example.com", "hashed", "+62", null, LocalDateTime.now(),
                LocalDateTime.now());
        when(repo.findByEmail("a@example.com")).thenReturn(Optional.of(m));
        when(hasher.matches("pw", "hashed")).thenReturn(true);

        var resp = useCase.login(new LoginMemberRequest("a@example.com", "pw"));
        assertTrue(resp.success());
        assertEquals(id, resp.memberId());
        assertEquals("A", resp.fullName());
    }

    @Test
    public void loginFailsWhenWrongPassword() {
        Member m = new Member(UUID.randomUUID(), "A", "a@example.com", "hashed", null, null, LocalDateTime.now(),
                LocalDateTime.now());
        when(repo.findByEmail("a@example.com")).thenReturn(Optional.of(m));
        when(hasher.matches("wrong", "hashed")).thenReturn(false);

        var resp = useCase.login(new LoginMemberRequest("a@example.com", "wrong"));
        assertFalse(resp.success());
        assertNull(resp.memberId());
    }

    @Test
    public void loginFailsWhenEmailNotFound() {
        when(repo.findByEmail("no@example.com")).thenReturn(Optional.empty());
        var resp = useCase.login(new LoginMemberRequest("no@example.com", "pw"));
        assertFalse(resp.success());
        assertNull(resp.memberId());
    }

}
