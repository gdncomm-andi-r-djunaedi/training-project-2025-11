package com.kailash.member;

import com.kailash.member.dto.*;
import com.kailash.member.entity.Member;
import com.kailash.member.exception.NotFoundException;
import com.kailash.member.repository.MemberRepository;
import com.kailash.member.repository.RefreshTokenRepository;
import com.kailash.member.service.impl.MemberServiceImpl;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.test.util.ReflectionTestUtils;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class MemberApplicationTests {

	@Mock
	private MemberRepository memberRepository;

	@Mock
	private RefreshTokenRepository refreshTokenRepository;

	@InjectMocks
	private MemberServiceImpl service;

	@BeforeEach
	void setup() {
		MockitoAnnotations.openMocks(this);

		// Set private @Value fields without changing serviceimpl
		ReflectionTestUtils.setField(service, "jwtExpiry", 1800L);
		ReflectionTestUtils.setField(service, "jwtSecret", "dummy-secret");
		ReflectionTestUtils.setField(service, "jwtExpirySec", 1800L);
		ReflectionTestUtils.setField(service, "refreshExpirySec", 3600L);
	}


	@Test
	void register_success() {
		RegisterRequest req = new RegisterRequest("a@b.com", "pass123", "Kailash", "999");

		when(memberRepository.findByEmail("a@b.com")).thenReturn(Optional.empty());
		when(memberRepository.save(any(Member.class))).thenAnswer(inv -> {
			Member m = inv.getArgument(0);
			m.setId(UUID.randomUUID());
			return m;
		});

		ApiResponse<MemberResponse> resp = service.register(req);

		assertTrue(resp.isSuccess());
		assertEquals("a@b.com", resp.getData().getEmail());
		assertEquals("Member registered successfully", resp.getMessage());
		verify(memberRepository).save(any(Member.class));
	}

	@Test
	void register_emailAlreadyExists_throwsException() {
		RegisterRequest req = new RegisterRequest("a@b.com", "pass123", "Kailash", "999");

		when(memberRepository.findByEmail("a@b.com"))
				.thenReturn(Optional.of(new Member()));

		assertThrows(IllegalArgumentException.class, () -> service.register(req));
		verify(memberRepository, never()).save(any());
	}


	@Test
	void login_success() {
		LoginRequest req = new LoginRequest("a@b.com", "123");

		Member m = Member.builder()
				.id(UUID.randomUUID())
				.email("a@b.com")
				.passwordHash(new BCryptPasswordEncoder().encode("123"))
				.fullName("Kailash")
				.phone("999")
				.createdAt(Instant.now())
				.build();

		when(memberRepository.findByEmail("a@b.com")).thenReturn(Optional.of(m));

		ApiResponse<MemberResponse> resp = service.login(req);

		assertTrue(resp.isSuccess());
		assertEquals("a@b.com", resp.getData().getEmail());
		assertEquals("Login successful", resp.getMessage());
	}

	@Test
	void login_wrongEmail_throwsNotFound() {
		LoginRequest req = new LoginRequest("abc@b.com", "123");

		when(memberRepository.findByEmail("abc@b.com"))
				.thenReturn(Optional.empty());

		assertThrows(NotFoundException.class, () -> service.login(req));
	}

	@Test
	void login_wrongPassword_throwsNotFound() {
		LoginRequest req = new LoginRequest("a@b.com", "wrong");

		Member m = Member.builder()
				.id(UUID.randomUUID())
				.email("a@b.com")
				.passwordHash(new BCryptPasswordEncoder().encode("123"))
				.build();

		when(memberRepository.findByEmail("a@b.com")).thenReturn(Optional.of(m));

		assertThrows(NotFoundException.class, () -> service.login(req));
	}


	@Test
	void getById_success() {
		UUID id = UUID.randomUUID();

		Member m = Member.builder()
				.id(id)
				.email("a@b.com")
				.fullName("Kailash")
				.phone("999")
				.createdAt(Instant.now())
				.build();

		when(memberRepository.findById(id)).thenReturn(Optional.of(m));

		ApiResponse<MemberResponse> resp = service.getById(id);

		assertTrue(resp.isSuccess());
		assertEquals(id.toString(), resp.getData().getId());
	}

	@Test
	void getById_notFound_throwsException() {
		UUID id = UUID.randomUUID();

		when(memberRepository.findById(id)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class, () -> service.getById(id));
	}

	// -------------------------------------------------------------------------
	// UPDATE
	// -------------------------------------------------------------------------
	@Test
	void update_success() {
		UUID id = UUID.randomUUID();

		Member existing = Member.builder()
				.id(id)
				.email("a@b.com")
				.fullName("Old")
				.phone("111")
				.passwordHash("old")
				.createdAt(Instant.now())
				.build();

		RegisterRequest req = new RegisterRequest("a@b.com", "newpass", "NewName", "222");

		when(memberRepository.findById(id)).thenReturn(Optional.of(existing));
		when(memberRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

		ApiResponse<MemberResponse> resp = service.update(id, req);

		assertTrue(resp.isSuccess());
		assertEquals("NewName", resp.getData().getFullName());
		assertEquals("222", resp.getData().getPhone());
	}

	@Test
	void update_notFound_throwsException() {
		UUID id = UUID.randomUUID();
		RegisterRequest req = new RegisterRequest("a@b.com", "pass", "Name", "999");

		when(memberRepository.findById(id)).thenReturn(Optional.empty());

		assertThrows(NotFoundException.class, () -> service.update(id, req));
	}


	@Test
	void delete_success() {
		UUID id = UUID.randomUUID();

		ApiResponse<Void> resp = service.delete(id);

		assertTrue(resp.isSuccess());
		assertEquals("Member deleted successfully", resp.getMessage());
		verify(memberRepository).deleteById(id);
	}
}
