package com.gdn.training.member.unit;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

import java.time.Duration;
import java.util.Optional;
import java.util.UUID;

import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.data.redis.core.ValueOperations;

import com.gdn.training.member.infrastructure.entity.MemberEntity;
import com.gdn.training.member.infrastructure.repository.CachingMemberRepositoryImpl;
import com.gdn.training.member.infrastructure.repository.SpringDataJpaMemberRepository;

public class CachingMemberRepositoryImplTest {

    @Test
    public void testFindMemberByEmail() {
        SpringDataJpaMemberRepository jpa = Mockito.mock(SpringDataJpaMemberRepository.class);
        RedisTemplate<String, Object> redis = Mockito.mock(RedisTemplate.class);
        ValueOperations<String, Object> valueOps = Mockito.mock(ValueOperations.class);

        when(redis.opsForValue()).thenReturn(valueOps);
        when(valueOps.get(anyString())).thenReturn(null); // simulating cache miss

        // ---- Setup Entity (Infrastructure Layer)
        UUID id = UUID.randomUUID();
        MemberEntity entity = new MemberEntity();
        entity.setId(id);
        entity.setFullName("John Doe");
        entity.setEmail("test@example.com");
        entity.setPasswordHash("hashed-password");
        entity.setPhoneNumber("123456789");

        when(jpa.findByEmail("test@example.com"))
                .thenReturn(Optional.of(entity));

        // ---- Instantiate repo with mocks
        CachingMemberRepositoryImpl repo = new CachingMemberRepositoryImpl(jpa, redis);

        // ---- Execute method
        var result = repo.findByEmail("test@example.com");

        // ---- Verify results
        assertTrue(result.isPresent());
        assertEquals(entity.getId(), result.get().getId());
        assertEquals(entity.getEmail(), result.get().getEmail());
        assertEquals(entity.getFullName(), result.get().getFullName());
        assertEquals(entity.getPasswordHash(), result.get().getPasswordHash());
        assertEquals(entity.getPhoneNumber(), result.get().getPhoneNumber());
    }
}
