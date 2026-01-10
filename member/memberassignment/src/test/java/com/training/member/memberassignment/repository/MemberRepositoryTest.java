package com.training.member.memberassignment.repository;

import com.training.member.memberassignment.entity.MemberEntity;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;

@DataJpaTest
class MemberRepositoryTest {

    @Autowired
    private TestEntityManager entityManager;

    @Autowired
    private MemberRepository memberRepository;

    private MemberEntity testMember;

    @BeforeEach
    void setUp() {
        testMember = MemberEntity.builder()
                .email("john.doe@example.com")
                .passwordHash("$2a$10$hashedPasswordValue")
                .build();
    }

    @Test
    @DisplayName("Should save member successfully")
    void saveMember_Success() {
        MemberEntity savedMember = memberRepository.save(testMember);

        assertNotNull(savedMember);
        assertNotNull(savedMember.getUserId());
        assertEquals(testMember.getEmail(), savedMember.getEmail());
        assertEquals(testMember.getPasswordHash(), savedMember.getPasswordHash());
    }

    @Test
    @DisplayName("Should find member by email when exists")
    void findByEmail_MemberExists() {
        entityManager.persistAndFlush(testMember);

        Optional<MemberEntity> foundMember = memberRepository.findByEmail("john.doe@example.com");

        assertTrue(foundMember.isPresent());
        assertEquals("john.doe@example.com", foundMember.get().getEmail());
    }

    @Test
    @DisplayName("Should return empty when finding by non-existent email")
    void findByEmail_MemberDoesNotExist() {
        Optional<MemberEntity> foundMember = memberRepository.findByEmail("nonexistent@example.com");

        assertFalse(foundMember.isPresent());
    }

    @Test
    @DisplayName("Should return true when email exists")
    void existsByEmail_EmailExists() {
        entityManager.persistAndFlush(testMember);

        boolean exists = memberRepository.existsByEmail("john.doe@example.com");

        assertTrue(exists);
    }

    @Test
    @DisplayName("Should return false when email does not exist")
    void existsByEmail_EmailDoesNotExist() {
        boolean exists = memberRepository.existsByEmail("nonexistent@example.com");

        assertFalse(exists);
    }

    @Test
    @DisplayName("Should generate unique user IDs for different members")
    void saveMember_GeneratesUniqueIds() {
        MemberEntity firstMember = MemberEntity.builder()
                .email("first@example.com")
                .passwordHash("hash1")
                .build();

        MemberEntity secondMember = MemberEntity.builder()
                .email("second@example.com")
                .passwordHash("hash2")
                .build();

        MemberEntity savedFirst = memberRepository.save(firstMember);
        MemberEntity savedSecond = memberRepository.save(secondMember);

        assertNotNull(savedFirst.getUserId());
        assertNotNull(savedSecond.getUserId());
        assertNotEquals(savedFirst.getUserId(), savedSecond.getUserId());
    }

    @Test
    @DisplayName("Should enforce unique email constraint")
    void saveMember_DuplicateEmail_ThrowsException() {
        entityManager.persistAndFlush(testMember);

        MemberEntity duplicateMember = MemberEntity.builder()
                .email("john.doe@example.com")
                .passwordHash("differentHash")
                .build();

        assertThrows(Exception.class, () -> {
            memberRepository.save(duplicateMember);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Should handle email case sensitivity correctly")
    void findByEmail_CaseSensitive() {
        entityManager.persistAndFlush(testMember);

        Optional<MemberEntity> foundLowerCase = memberRepository.findByEmail("john.doe@example.com");
        Optional<MemberEntity> foundUpperCase = memberRepository.findByEmail("JOHN.DOE@EXAMPLE.COM");

        assertTrue(foundLowerCase.isPresent());
        // Email comparison is case-sensitive by default in PostgreSQL
        assertFalse(foundUpperCase.isPresent());
    }

    @Test
    @DisplayName("Should persist password hash correctly")
    void saveMember_PasswordHashPersisted() {
        String expectedHash = "$2a$10$specificHashValue123";
        MemberEntity member = MemberEntity.builder()
                .email("test@example.com")
                .passwordHash(expectedHash)
                .build();

        MemberEntity savedMember = memberRepository.save(member);
        entityManager.flush();
        entityManager.clear();

        Optional<MemberEntity> retrievedMember = memberRepository.findByEmail("test@example.com");

        assertTrue(retrievedMember.isPresent());
        assertEquals(expectedHash, retrievedMember.get().getPasswordHash());
    }

    @Test
    @DisplayName("Should update existing member")
    void updateMember_Success() {
        MemberEntity savedMember = entityManager.persistAndFlush(testMember);
        Long memberId = savedMember.getUserId();

        savedMember.setPasswordHash("$2a$10$newHashedPassword");
        memberRepository.save(savedMember);
        entityManager.flush();
        entityManager.clear();

        Optional<MemberEntity> updatedMember = memberRepository.findById(memberId);

        assertTrue(updatedMember.isPresent());
        assertEquals("$2a$10$newHashedPassword", updatedMember.get().getPasswordHash());
    }

    @Test
    @DisplayName("Should delete member by ID")
    void deleteMember_Success() {
        MemberEntity savedMember = entityManager.persistAndFlush(testMember);
        Long memberId = savedMember.getUserId();

        memberRepository.deleteById(memberId);
        entityManager.flush();

        Optional<MemberEntity> deletedMember = memberRepository.findById(memberId);
        assertFalse(deletedMember.isPresent());
    }

    @Test
    @DisplayName("Should find all members")
    void findAllMembers_Success() {
        MemberEntity member1 = MemberEntity.builder()
                .email("user1@example.com")
                .passwordHash("hash1")
                .build();

        MemberEntity member2 = MemberEntity.builder()
                .email("user2@example.com")
                .passwordHash("hash2")
                .build();

        entityManager.persist(member1);
        entityManager.persist(member2);
        entityManager.flush();

        var allMembers = memberRepository.findAll();

        assertEquals(2, allMembers.size());
    }

    @Test
    @DisplayName("Should handle special characters in email")
    void saveMember_SpecialCharactersInEmail() {
        MemberEntity memberWithSpecialChars = MemberEntity.builder()
                .email("user+tag@sub-domain.example.com")
                .passwordHash("hash")
                .build();

        MemberEntity savedMember = memberRepository.save(memberWithSpecialChars);

        assertNotNull(savedMember.getUserId());
        assertEquals("user+tag@sub-domain.example.com", savedMember.getEmail());
    }

    @Test
    @DisplayName("Should not allow null email")
    void saveMember_NullEmail_ThrowsException() {
        MemberEntity memberWithNullEmail = MemberEntity.builder()
                .email(null)
                .passwordHash("hash")
                .build();

        assertThrows(Exception.class, () -> {
            memberRepository.save(memberWithNullEmail);
            entityManager.flush();
        });
    }

    @Test
    @DisplayName("Should not allow null password hash")
    void saveMember_NullPasswordHash_ThrowsException() {
        MemberEntity memberWithNullPassword = MemberEntity.builder()
                .email("test@example.com")
                .passwordHash(null)
                .build();

        assertThrows(Exception.class, () -> {
            memberRepository.save(memberWithNullPassword);
            entityManager.flush();
        });
    }
}
