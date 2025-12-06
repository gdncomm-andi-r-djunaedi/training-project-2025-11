package com.gdn.training.member.unit;

import com.gdn.training.member.application.usecase.impl.GetMemberProfileUseCaseImpl;
import com.gdn.training.member.domain.port.out.MemberRepository;
import com.gdn.training.member.domain.model.Member;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class GetMemberProfileUseCaseImplTest {
    private MemberRepository repo;
    private GetMemberProfileUseCaseImpl useCase;

    @BeforeEach
    public void setup() {
        repo = Mockito.mock(MemberRepository.class);
        useCase = new GetMemberProfileUseCaseImpl(repo);
    }

    @Test
    public void returnsProfileWhenExists() {
        UUID id = UUID.randomUUID();
        Member m = new Member(id, "Name", "e@example.com", "h", "+62", "avatar.png", LocalDateTime.now(),
                LocalDateTime.now());
        when(repo.findById(id)).thenReturn(Optional.of(m));

        var resp = useCase.getProfile(id);
        assertNotNull(resp);
        assertEquals("Name", resp.fullName());
        assertEquals("e@example.com", resp.email());
    }

    @Test
    public void returnsNullWhenNotFound() {
        UUID id = UUID.randomUUID();
        when(repo.findById(id)).thenReturn(Optional.empty());
        var resp = useCase.getProfile(id);
        assertNull(resp);
    }
}
