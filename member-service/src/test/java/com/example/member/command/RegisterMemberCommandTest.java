package com.example.member.command;

import com.example.member.entity.Member;
import com.example.member.repository.MemberRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.security.crypto.password.PasswordEncoder;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class RegisterMemberCommandTest {

  @Mock
  MemberRepository repo;

  @Mock
  PasswordEncoder encoder;

  @Test
  void execute_savesMemberWithHashedPassword() {
    when(encoder.encode("plain")).thenReturn("hashed");
    when(repo.findByEmail("a@b.com")).thenReturn(Optional.empty());

    RegisterMemberCommand cmd =
        new RegisterMemberCommand(repo, encoder, "a@b.com", "plain", "User");

    cmd.execute();

    ArgumentCaptor<Member> captor = ArgumentCaptor.forClass(Member.class);
    verify(repo).save(captor.capture());
    Member saved = captor.getValue();

    assertThat(saved.getPasswordHash()).isEqualTo("hashed");
    assertThat(saved.getEmail()).isEqualTo("a@b.com");
  }

  @Test
  void execute_duplicateEmail_throws() {
    Member existing = Member.builder()
        .email("a@b.com")
        .build();

    when(repo.findByEmail("a@b.com")).thenReturn(Optional.of(existing));

    RegisterMemberCommand cmd =
        new RegisterMemberCommand(repo, encoder, "a@b.com", "plain", "User");

    assertThrows(IllegalStateException.class, cmd::execute);
  }
}