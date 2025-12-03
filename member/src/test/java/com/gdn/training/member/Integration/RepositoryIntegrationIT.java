package com.gdn.training.member.Integration;

import com.gdn.training.member.infrastructure.entity.MemberEntity;
import com.gdn.training.member.infrastructure.repository.CachingMemberRepositoryImpl;
import com.gdn.training.member.infrastructure.repository.SpringDataJpaMemberRepository;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.BeforeEach;

import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.context.DynamicPropertySource;
import org.springframework.test.context.DynamicPropertyRegistry;

import org.testcontainers.junit.jupiter.Testcontainers;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.containers.GenericContainer;

import java.util.UUID;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@Testcontainers
public class RepositoryIntegrationIT {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("memberdb")
            .withUsername("postgres")
            .withPassword("postgres");

    @Container
    static GenericContainer<?> redis = new GenericContainer<>("redis:7-alpine")
            .withExposedPorts(6379);

    @DynamicPropertySource
    static void props(DynamicPropertyRegistry r) {
        r.add("DB_HOST", postgres::getHost);
        r.add("DB_PORT", () -> postgres.getFirstMappedPort());
        r.add("DB_NAME", () -> postgres.getDatabaseName());
        r.add("DB_USER", () -> postgres.getUsername());
        r.add("DB_PASSWORD", () -> postgres.getPassword());
        r.add("spring.redis.host", () -> redis.getHost());
        r.add("spring.redis.port", () -> redis.getFirstMappedPort());
    }

    @Autowired
    private SpringDataJpaMemberRepository jpa;

    @Autowired
    private CachingMemberRepositoryImpl cachingRepo;

    @Test
    public void testSaveAndFetch() {
        UUID id = UUID.randomUUID();

        MemberEntity e = new MemberEntity(
                id, "Integration User", "int@example.com", "pwd",
                "+62000", null, LocalDateTime.now(), LocalDateTime.now());
        jpa.save(e);

        var found = cachingRepo.findByEmail("int@example.com");
        assertTrue(found.isPresent());
        assertEquals("Integration User", found.get().getFullName());
    }
}
