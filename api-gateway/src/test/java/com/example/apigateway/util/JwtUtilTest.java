package com.example.apigateway.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.HttpHeaders;
import org.springframework.http.ResponseCookie;
import org.springframework.mock.http.server.reactive.MockServerHttpRequest;

import java.security.Key;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
class JwtUtilTest {

    @Autowired
    private JwtUtil jwtUtil;

    private final String SECRET = "5367566B59703373367639792F423F4528482B4D6251655468576D5A71347437";

    @BeforeEach
    void setUp() {
        assertNotNull(jwtUtil);
    }

    @Test
    void createToken_shouldCreateValidTokenWithCorrectClaims() {
        String userId = "123";

        String token = jwtUtil.createToken(userId);

        assertNotNull(token);
        assertFalse(token.isEmpty());

        Claims claims = jwtUtil.validateToken(token);
        assertEquals(userId, claims.get("user_id", String.class));
        assertEquals(userId, claims.getSubject());
        assertNotNull(claims.getIssuedAt());
        assertNotNull(claims.getExpiration());
    }

    @Test
    void validateToken_shouldReturnClaims_whenTokenIsValid() {
        String token = generateToken("123", List.of("customer"));
        Claims claims = jwtUtil.validateToken(token);

        assertEquals("123", claims.get("user_id", String.class));
        assertEquals(List.of("customer"), claims.get("roles", List.class));
    }

    @Test
    void validateToken_shouldThrow_whenTokenIsInvalid() {
        assertThrows(Exception.class, () -> jwtUtil.validateToken("invalidToken"));
    }

    @Test
    void validateToken_shouldThrow_whenTokenIsExpired() {
        String expiredToken = generateExpiredToken("123", List.of("customer"));
        assertThrows(ExpiredJwtException.class, () -> jwtUtil.validateToken(expiredToken));
    }

    @Test
    void validateToken_shouldThrow_whenTokenHasWrongSignature() {
        String token = generateTokenWithDifferentSecret("123", List.of("customer"));
        assertThrows(Exception.class, () -> jwtUtil.validateToken(token));
    }

    @Test
    void extractToken_shouldExtractFromAuthorizationHeader() {
        String token = "test-token-123";
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/products")
                .header(HttpHeaders.AUTHORIZATION, "Bearer " + token)
                .build();

        Optional<String> extracted = jwtUtil.extractToken(request);

        assertTrue(extracted.isPresent());
        assertEquals(token, extracted.get());
    }

    // @Test
    // void extractToken_shouldExtractFromAuthorizationHeader_caseInsensitive() {
    // String token = "test-token-123";
    // MockServerHttpRequest request = MockServerHttpRequest.get("/api/products")
    // .header("authorization", "bearer " + token)
    // .build();

    // Optional<String> extracted = jwtUtil.extractToken(request);

    // assertTrue(extracted.isPresent());
    // assertEquals(token, extracted.get());
    // }

    @Test
    void extractToken_shouldReturnEmpty_whenAuthorizationHeaderMissing() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/products")
                .build();

        Optional<String> extracted = jwtUtil.extractToken(request);

        assertFalse(extracted.isPresent());
    }

    @Test
    void extractToken_shouldReturnEmpty_whenAuthorizationHeaderNotBearer() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/products")
                .header(HttpHeaders.AUTHORIZATION, "Basic dXNlcjpwYXNz")
                .build();

        Optional<String> extracted = jwtUtil.extractToken(request);

        assertFalse(extracted.isPresent());
    }

    // @Test
    // void extractToken_shouldExtractFromCookie_whenHeaderMissing() {
    // String token = "test-token-123";
    // MockServerHttpRequest request = MockServerHttpRequest.get("/api/products")
    // .cookie(org.springframework.http.HttpCookie.from("jwt", token).build())
    // .build();

    // Optional<String> extracted = jwtUtil.extractToken(request);

    // assertTrue(extracted.isPresent());
    // assertEquals(token, extracted.get());
    // }

    // @Test
    // void extractToken_shouldPreferHeaderOverCookie() {
    // String headerToken = "header-token";
    // String cookieToken = "cookie-token";
    // MockServerHttpRequest request = MockServerHttpRequest.get("/api/products")
    // .header(HttpHeaders.AUTHORIZATION, "Bearer " + headerToken)
    // .cookie(org.springframework.http.HttpCookie.from("jwt", cookieToken).build())
    // .build();

    // Optional<String> extracted = jwtUtil.extractToken(request);

    // assertTrue(extracted.isPresent());
    // assertEquals(headerToken, extracted.get());
    // }

    @Test
    void extractToken_shouldReturnEmpty_whenBothHeaderAndCookieMissing() {
        MockServerHttpRequest request = MockServerHttpRequest.get("/api/products")
                .build();

        Optional<String> extracted = jwtUtil.extractToken(request);

        assertFalse(extracted.isPresent());
    }

    @Test
    void createCookie_shouldCreateSecureCookie() {
        String token = "test-token";
        ResponseCookie cookie = jwtUtil.createCookie(token);

        assertNotNull(cookie);
        assertEquals("jwt", cookie.getName());
        assertEquals(token, cookie.getValue());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.isSecure());
        assertEquals("Strict", cookie.getSameSite());
        assertEquals("/", cookie.getPath());
        assertEquals(1800, cookie.getMaxAge().getSeconds()); // 30 minutes
    }

    @Test
    void clearCookie_shouldExpireJwtCookie() {
        ResponseCookie cookie = jwtUtil.clearCookie();

        assertNotNull(cookie);
        assertEquals("jwt", cookie.getName());
        assertEquals("", cookie.getValue());
        assertEquals(0, cookie.getMaxAge().getSeconds());
        assertTrue(cookie.isHttpOnly());
        assertTrue(cookie.isSecure());
        assertEquals("Strict", cookie.getSameSite());
        assertEquals("/", cookie.getPath());
    }

    private String generateToken(String userId, List<String> roles) {
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("user_id", userId);
        claims.put("roles", roles);
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + 1000 * 60 * 30))
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateExpiredToken(String userId, List<String> roles) {
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("user_id", userId);
        claims.put("roles", roles);
        long now = System.currentTimeMillis();
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date(now - 1000 * 60 * 60)) // 1 hour ago
                .setExpiration(new Date(now - 1000 * 60 * 30)) // 30 minutes ago (expired)
                .signWith(getSignKey(), SignatureAlgorithm.HS256)
                .compact();
    }

    private String generateTokenWithDifferentSecret(String userId, List<String> roles) {
        String differentSecret = "1234567890123456789012345678901234567890123456789012345678901234";
        HashMap<String, Object> claims = new HashMap<>();
        claims.put("user_id", userId);
        claims.put("roles", roles);
        long now = System.currentTimeMillis();
        byte[] keyBytes = Decoders.BASE64.decode(differentSecret);
        Key differentKey = Keys.hmacShaKeyFor(keyBytes);
        return Jwts.builder()
                .setClaims(claims)
                .setSubject(userId)
                .setIssuedAt(new Date(now))
                .setExpiration(new Date(now + 1000 * 60 * 30))
                .signWith(differentKey, SignatureAlgorithm.HS256)
                .compact();
    }

    private Key getSignKey() {
        byte[] keyBytes = Decoders.BASE64.decode(SECRET);
        return Keys.hmacShaKeyFor(keyBytes);
    }
}
