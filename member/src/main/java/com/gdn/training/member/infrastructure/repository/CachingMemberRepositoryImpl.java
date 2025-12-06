package com.gdn.training.member.infrastructure.repository;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

import org.springframework.context.annotation.Primary;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.stereotype.Repository;

import com.gdn.training.member.domain.model.Member;
import com.gdn.training.member.domain.port.out.MemberRepository;
import com.gdn.training.member.infrastructure.entity.MemberEntity;

import jakarta.transaction.Transactional;
import lombok.extern.slf4j.Slf4j;

/**
 * caching repository implementation that implements MemberRepository interface
 */
@Repository
@Primary
@Slf4j
public class CachingMemberRepositoryImpl implements MemberRepository {
    private final SpringDataJpaMemberRepository jpaRepo;

    private final ValueOperations<String, Object> valueOps;
    private final Duration cacheDuration;

    public CachingMemberRepositoryImpl(SpringDataJpaMemberRepository jpaRepo,
            RedisTemplate<String, Object> redisTemplate) {
        this.jpaRepo = jpaRepo;
        this.valueOps = redisTemplate.opsForValue();
        this.cacheDuration = Duration.ofMinutes(10);
    }

    @Override
    @Transactional
    public Member save(Member member) {
        MemberEntity memberEntity = toEntity(member);
        MemberEntity saved = jpaRepo.save(memberEntity);
        Member domain = toDomain(saved);

        // cache minimal profile and email existence
        try {
            valueOps.set(
                    profileKey(domain.getId()),
                    minimalProfilePayload(domain), cacheDuration);
            valueOps.set(
                    emailExistsKey(domain.getEmail()),
                    "1",
                    Duration.ofMinutes(30));
        } catch (Exception e) {

            log.error("Failed to cache member", e);
        }
        return domain;
    }

    @Override
    public Optional<Member> findById(UUID id) {
        try {
            Object cached = valueOps.get(profileKey(id));
            if (cached instanceof String) {
                // cache minimal profile: "id|fullName|email|phone|avatar"
                String[] parts = ((String) cached).split("|");
                Member m = new Member(
                        UUID.fromString(parts[0]),
                        emptyToNull(parts[1]),
                        emptyToNull(parts[2]),
                        emptyToNull(parts[3]),
                        null,
                        emptyToNull(parts[4]),
                        LocalDateTime.now(),
                        LocalDateTime.now());
                return Optional.of(m);
            }
        } catch (Exception e) {
            // TODO: handle exception
            log.error("Failed to find member by id", e);
            return Optional.empty();
        }
        return jpaRepo.findById(id).map(this::toDomain);
    }

    @Override
    public Optional<Member> findByEmail(String email) {
        try {
            Object cached = valueOps.get(emailExistsKey(email));
            if (cached instanceof String) {
                return Optional.of(new Member(
                        UUID.fromString(cached.toString()),
                        null,
                        email,
                        null,
                        null,
                        null,
                        LocalDateTime.now(),
                        LocalDateTime.now()));
            }
        } catch (Exception e) {
            // TODO: handle exception
            log.error("Failed to find member by email", e);
            return Optional.empty();
        }
        return jpaRepo.findByEmail(email).map(this::toDomain);
    }

    @Override
    public long count() {
        return jpaRepo.count();
    }

    private Member toDomain(MemberEntity e) {
        return new Member(
                e.getId(),
                e.getFullName(),
                e.getEmail(),
                e.getPasswordHash(),
                e.getPhoneNumber(),
                e.getAvatarUrl(),
                e.getCreatedAt(),
                e.getUpdatedAt());
    }

    private MemberEntity toEntity(Member m) {
        return new MemberEntity(
                m.getId(),
                m.getFullName(),
                m.getEmail(),
                m.getPasswordHash(),
                m.getPhoneNumber(),
                m.getAvatarUrl(),
                m.getCreatedAt(),
                m.getUpdatedAt());
    }

    private String profileKey(UUID id) {
        return "member:profile:" + id.toString();
    }

    private String emailExistsKey(String email) {
        return "member:email-exists:" + email.toLowerCase();
    }

    private String minimalProfilePayload(Member m) {
        // id|fullName|email|phone|avatar
        return m.getId().toString() + "|" +
                (m.getFullName() == null ? "" : m.getFullName()) + "|" +
                (m.getEmail() == null ? "" : m.getEmail()) + "|" +
                (m.getPhoneNumber() == null ? "" : m.getPhoneNumber()) + "|" +
                (m.getAvatarUrl() == null ? "" : m.getAvatarUrl());
    }

    private String emptyToNull(String s) {
        return (s == null || s.isEmpty()) ? null : s;
    }
}
