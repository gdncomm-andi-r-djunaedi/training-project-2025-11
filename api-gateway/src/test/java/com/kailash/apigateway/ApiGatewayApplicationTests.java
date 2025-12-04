package com.kailash.apigateway;

import com.kailash.apigateway.client.MemberClient;
import com.kailash.apigateway.service.impl.AuthServiceImpl;
import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import com.kailash.apigateway.dto.ApiResponse;
import com.kailash.apigateway.dto.LoginRequest;
import com.kailash.apigateway.dto.MemberResponse;
import com.kailash.apigateway.entity.RefreshToken;
import com.kailash.apigateway.repository.RefreshTokenRepository;
import com.kailash.apigateway.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.http.ResponseEntity;
import org.springframework.test.util.ReflectionTestUtils;

import java.lang.reflect.Field;
import java.time.Instant;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
class ApiGatewayApplicationTests {

	@Test
	void contextLoads() {
	}

	@InjectMocks
	private AuthServiceImpl authService;

	@Mock
	private RefreshTokenRepository refreshRepo;

	@Mock
	private MemberClient memberClient;

	@Mock
	private com.kailash.apigateway.security.JwtUtil jwtUtil;

	@BeforeEach
	void setup() throws Exception {
		MockitoAnnotations.openMocks(this);

		// Set private fields via reflection
		Field jwtExpiryField = AuthServiceImpl.class.getDeclaredField("jwtExpirySec");
		jwtExpiryField.setAccessible(true);
		jwtExpiryField.set(authService, 1800L);

		Field refreshExpiryField = AuthServiceImpl.class.getDeclaredField("refreshExpirySec");
		refreshExpiryField.setAccessible(true);
		refreshExpiryField.set(authService, 3600L);
	}

	// ------------------------------------------
	// LOGIN
	// ------------------------------------------
	@Test
	void login_success() {
		LoginRequest req = new LoginRequest("a@b.com", "123");

		// Use a valid UUID string for member ID
		String memberId = UUID.randomUUID().toString();
		MemberResponse member = new MemberResponse(memberId, "a@b.com", "Kailash", "999");

		// Mock the ApiResponse returned by memberClient
		ApiResponse<MemberResponse> memberApiResp = new ApiResponse<>(member, true, "Login successful");
		ResponseEntity<ApiResponse<MemberResponse>> responseEntity = ResponseEntity.ok(memberApiResp);

		// Mock memberClient
		when(memberClient.login(req)).thenReturn(responseEntity);

		// Mock refreshRepo.save to just return the token
		when(refreshRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

		// Mock jwtUtil.generateToken
		when(jwtUtil.generateToken(eq(memberId), anyMap(), anyLong())).thenReturn("token-123");

		// Call login
		ApiResponse<Map<String, Object>> loginResponse = authService.login(req);

		// Print response for debugging
		System.out.println("printing response" + loginResponse);

		// Assertions
		assertNotNull(loginResponse, "Login response should not be null");
		assertTrue(loginResponse.isSuccess(), "Login should be successful");
		assertNotNull(loginResponse.getData(), "Login data should not be null");
		assertEquals("token-123", loginResponse.getData().get("accessToken"));
		assertNotNull(loginResponse.getData().get("refreshTokenId"));
	}




	@Test
	void login_wrongEmail_returnsFailure() {
		LoginRequest req = new LoginRequest("wrong@b.com", "123");

		// MemberClient returns a valid ApiResponse, but data is null
		ApiResponse<MemberResponse> apiResp = new ApiResponse<>(null, true, "Login successful");
		ResponseEntity<ApiResponse<MemberResponse>> responseEntity = ResponseEntity.ok(apiResp);

		when(memberClient.login(req)).thenReturn(responseEntity);

		ApiResponse<Map<String, Object>> resp = authService.login(req);

		System.out.println("printing response" + resp);

		assertFalse(resp.isSuccess(), "Login should fail");
		assertEquals("Member data not found", resp.getMessage());
		assertNull(resp.getData(), "Data should be null for failed login");
	}



	@Test
	void login_memberDataNull_returnsFailure() {
		LoginRequest req = new LoginRequest("a@b.com", "123");

		ApiResponse<MemberResponse> memberResp = new ApiResponse<>(null, true, "Login successful");
		ResponseEntity<ApiResponse<MemberResponse>> responseEntity = ResponseEntity.ok(memberResp);

		when(memberClient.login(req)).thenReturn(responseEntity);

		ApiResponse<Map<String, Object>> resp = authService.login(req);

		assertFalse(resp.isSuccess());
		assertEquals("Member data not found", resp.getMessage());
	}

	// ------------------------------------------
	// REFRESH
	// ------------------------------------------
	@Test
	void refresh_success() {
		String refreshId = "refresh-1";
		RefreshToken rt = RefreshToken.builder()
				.jti(refreshId)
				.memberId(UUID.randomUUID())
				.issuedAt(Instant.now())
				.expiresAt(Instant.now().plusSeconds(3600))
				.isRevoked(false)
				.build();

		when(refreshRepo.findByJti(refreshId)).thenReturn(Optional.of(rt));
		when(jwtUtil.generateToken(eq(rt.getMemberId().toString()), anyMap(), eq(1800L))).thenReturn("token-456");

		ApiResponse<Map<String, Object>> resp = authService.refresh(refreshId);

		assertTrue(resp.isSuccess());
		assertEquals("token-456", resp.getData().get("accessToken"));
		assertEquals(1800L, resp.getData().get("expiresIn"));
		assertEquals(refreshId, resp.getData().get("refreshTokenId"));
	}

	@Test
	void refresh_invalidToken_returnsFailure() {
		String refreshId = "invalid-token";
		when(refreshRepo.findByJti(refreshId)).thenReturn(Optional.empty());

		ApiResponse<Map<String, Object>> resp = authService.refresh(refreshId);

		assertFalse(resp.isSuccess());
		assertTrue(resp.getMessage().contains("Invalid refresh token"));
	}

	// ------------------------------------------
	// LOGOUT
	// ------------------------------------------
	@Test
	void logout_success() {
		String refreshId = "refresh-1";
		RefreshToken rt = RefreshToken.builder()
				.jti(refreshId)
				.isRevoked(false)
				.build();

		when(refreshRepo.findByJti(refreshId)).thenReturn(Optional.of(rt));
		when(refreshRepo.save(any())).thenAnswer(inv -> inv.getArgument(0));

		ApiResponse<Void> resp = authService.logout(refreshId);

		assertTrue(resp.isSuccess());
		verify(refreshRepo, times(1)).save(any());
		assertTrue(rt.isRevoked());
	}

	@Test
	void logout_tokenNotFound_returnsSuccess() {
		String refreshId = "nonexistent";
		when(refreshRepo.findByJti(refreshId)).thenReturn(Optional.empty());

		ApiResponse<Void> resp = authService.logout(refreshId);

		assertTrue(resp.isSuccess(), "Logout should still succeed even if token not found");
	}

}
