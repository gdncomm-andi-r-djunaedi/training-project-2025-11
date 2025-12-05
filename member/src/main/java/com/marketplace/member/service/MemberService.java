package com.marketplace.member.service;

import com.marketplace.common.exception.BadRequestException;
import com.marketplace.common.exception.ResourceNotFoundException;
import com.marketplace.common.exception.UnauthorizedException;
import com.marketplace.common.security.JwtTokenProvider;
import com.marketplace.member.dto.LoginRequest;
import com.marketplace.member.dto.LoginResponse;
import com.marketplace.member.dto.MemberResponse;
import com.marketplace.member.dto.RegisterRequest;
import com.marketplace.member.entity.Member;
import com.marketplace.member.mapper.MemberMapper;
import com.marketplace.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.io.Serializable;
import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Slf4j
@Service
@RequiredArgsConstructor
public class MemberService {

    private static final String TOKEN_BLACKLIST_PREFIX = "token:blacklist:";
    private static final String MEMBER_CACHE_PREFIX = "member:";
    private static final String MEMBER_TOKEN_PREFIX = "member:token:";

    private final MemberRepository memberRepository;
    private final MemberMapper memberMapper;
    private final PasswordEncoder passwordEncoder;
    private final JwtTokenProvider jwtTokenProvider;
    private final RedisTemplate<String, Object> redisTemplate;

    @Transactional
    public MemberResponse register(RegisterRequest request) {
        log.info("Registering new member with email: {}", request.getEmail());

        // Check if email already exists
        if (memberRepository.existsByEmail(request.getEmail())) {
            throw new BadRequestException("Email already registered");
        }

        // Create member entity
        Member member = memberMapper.toEntity(request);
        member.setPassword(passwordEncoder.encode(request.getPassword()));

        // Save member
        member = memberRepository.save(member);
        log.info("Member registered successfully with id: {}", member.getId());

        // Cache member
        cacheMember(member);

        return memberMapper.toResponse(member);
    }

    public LoginResponse login(LoginRequest request) {
        log.info("Login attempt for email: {}", request.getEmail());

        // Find member by email
        Member member = memberRepository.findByEmail(request.getEmail())
                .orElseThrow(() -> new UnauthorizedException("Invalid email or password"));

        // Verify password using Spring Security's PasswordEncoder
        if (!passwordEncoder.matches(request.getPassword(), member.getPassword())) {
            throw new UnauthorizedException("Invalid email or password");
        }

        // Check if member is active
        if (!member.getActive()) {
            throw new UnauthorizedException("Account is deactivated");
        }

        // Check for existing valid tokens
        TokenCache existingTokens = getExistingValidTokens(member.getId());
        
        if (existingTokens != null) {
            log.info("Returning existing valid tokens for member: {}", member.getId());
            
            // Calculate remaining validity
            long remainingValidity = jwtTokenProvider.getRemainingValidityInMillis(existingTokens.accessToken);
            
            return LoginResponse.builder()
                    .accessToken(existingTokens.accessToken)
                    .refreshToken(existingTokens.refreshToken)
                    .tokenType("Bearer")
                    .expiresIn(remainingValidity / 1000)
                    .member(memberMapper.toResponse(member))
                    .build();
        }

        // Generate new tokens
        String accessToken = jwtTokenProvider.generateAccessToken(member.getId(), member.getEmail());
        String refreshToken = jwtTokenProvider.generateRefreshToken(member.getId(), member.getEmail());

        // Store tokens in Redis with access token validity
        storeTokens(member.getId(), accessToken, refreshToken);

        // Cache member
        cacheMember(member);

        log.info("Login successful with new tokens for member: {}", member.getId());

        return LoginResponse.builder()
                .accessToken(accessToken)
                .refreshToken(refreshToken)
                .tokenType("Bearer")
                .expiresIn(jwtTokenProvider.getAccessTokenValidity() / 1000)
                .member(memberMapper.toResponse(member))
                .build();
    }

    public void logout(String token) {
        log.info("Processing logout request");

        if (token != null && jwtTokenProvider.validateToken(token)) {
            // Get member ID from token to clear stored tokens
            UUID memberId = jwtTokenProvider.getMemberIdFromToken(token);
            
            // Clear stored tokens for this member
            clearStoredTokens(memberId);
            
            // Get remaining time until token expiry
            long remainingValidity = jwtTokenProvider.getRemainingValidityInMillis(token);

            // Add token to blacklist with remaining validity time
            String key = TOKEN_BLACKLIST_PREFIX + token;
            redisTemplate.opsForValue().set(key, "blacklisted", remainingValidity, TimeUnit.MILLISECONDS);
            log.info("Token blacklisted and stored tokens cleared for member: {}", memberId);
        }
    }

    @Transactional(readOnly = true)
    public MemberResponse getMemberById(UUID id) {
        // Try to get from cache first
        String cacheKey = MEMBER_CACHE_PREFIX + id;
        MemberResponse cached = (MemberResponse) redisTemplate.opsForValue().get(cacheKey);
        
        if (cached != null) {
            log.debug("Member {} found in cache", id);
            return cached;
        }

        // Get from database
        Member member = memberRepository.findById(id)
                .orElseThrow(() -> ResourceNotFoundException.of("Member", id));

        MemberResponse response = memberMapper.toResponse(member);
        
        // Cache the response
        redisTemplate.opsForValue().set(cacheKey, response, 1, TimeUnit.HOURS);
        
        return response;
    }

    @Transactional(readOnly = true)
    public MemberResponse getMemberByEmail(String email) {
        Member member = memberRepository.findByEmail(email)
                .orElseThrow(() -> ResourceNotFoundException.of("Member", email));
        return memberMapper.toResponse(member);
    }

    private void cacheMember(Member member) {
        String cacheKey = MEMBER_CACHE_PREFIX + member.getId();
        MemberResponse response = memberMapper.toResponse(member);
        redisTemplate.opsForValue().set(cacheKey, response, 1, TimeUnit.HOURS);
    }

    /**
     * Get existing valid tokens from Redis.
     * Returns null if tokens don't exist, are blacklisted, or are invalid.
     */
    private TokenCache getExistingValidTokens(UUID memberId) {
        String tokenCacheKey = MEMBER_TOKEN_PREFIX + memberId;
        TokenCache tokenCache = (TokenCache) redisTemplate.opsForValue().get(tokenCacheKey);
        
        if (tokenCache == null) {
            log.debug("No cached tokens found for member: {}", memberId);
            return null;
        }
        
        // Check if access token is blacklisted
        String blacklistKey = TOKEN_BLACKLIST_PREFIX + tokenCache.accessToken;
        if (Boolean.TRUE.equals(redisTemplate.hasKey(blacklistKey))) {
            log.debug("Cached access token is blacklisted for member: {}", memberId);
            clearStoredTokens(memberId);
            return null;
        }
        
        // Validate the access token
        if (!jwtTokenProvider.validateToken(tokenCache.accessToken)) {
            log.debug("Cached access token is invalid/expired for member: {}", memberId);
            clearStoredTokens(memberId);
            return null;
        }
        
        log.debug("Found valid cached tokens for member: {}", memberId);
        return tokenCache;
    }

    /**
     * Store tokens in Redis with TTL matching access token validity.
     */
    private void storeTokens(UUID memberId, String accessToken, String refreshToken) {
        String tokenCacheKey = MEMBER_TOKEN_PREFIX + memberId;
        TokenCache tokenCache = new TokenCache(accessToken, refreshToken);
        
        // Store with TTL slightly longer than access token validity
        long ttl = jwtTokenProvider.getAccessTokenValidity();
        redisTemplate.opsForValue().set(tokenCacheKey, tokenCache, ttl, TimeUnit.MILLISECONDS);
        
        log.debug("Tokens stored in cache for member: {}, TTL: {} ms", memberId, ttl);
    }

    /**
     * Clear stored tokens for a member (called on logout).
     */
    private void clearStoredTokens(UUID memberId) {
        String tokenCacheKey = MEMBER_TOKEN_PREFIX + memberId;
        redisTemplate.delete(tokenCacheKey);
        log.debug("Stored tokens cleared for member: {}", memberId);
    }

    /**
     * Inner class to store access and refresh tokens together.
     */
    public static class TokenCache implements Serializable {
        private static final long serialVersionUID = 1L;
        
        public String accessToken;
        public String refreshToken;
        
        public TokenCache() {}
        
        public TokenCache(String accessToken, String refreshToken) {
            this.accessToken = accessToken;
            this.refreshToken = refreshToken;
        }
    }
}
