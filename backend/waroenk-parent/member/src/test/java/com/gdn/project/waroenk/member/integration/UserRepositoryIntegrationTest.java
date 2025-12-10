package com.gdn.project.waroenk.member.integration;

import com.gdn.project.waroenk.member.constant.Gender;
import com.gdn.project.waroenk.member.entity.User;
import com.gdn.project.waroenk.member.fixture.TestDataFactory;
import com.gdn.project.waroenk.member.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("UserRepository Integration Tests")
class UserRepositoryIntegrationTest {

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TestEntityManager entityManager;

  private User testUser;

  @BeforeEach
  void setUp() {
    // Create and persist a test user
    testUser = User.builder()
        .fullName("John Doe")
        .email("john.doe@example.com")
        .phoneNumber("+6281234567890")
        .gender(Gender.MALE)
        .dob(LocalDate.of(1990, 5, 15))
        .passwordHash("$2a$10$hashedPassword")
        .build();
    testUser = entityManager.persistAndFlush(testUser);
    entityManager.clear();
  }

  @Nested
  @DisplayName("findById Tests")
  class FindByIdTests {

    @Test
    @DisplayName("Should find user by ID")
    void shouldFindUserById() {
      // When
      Optional<User> result = userRepository.findById(testUser.getId());

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().getFullName()).isEqualTo("John Doe");
      assertThat(result.get().getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("Should return empty when user not found")
    void shouldReturnEmptyWhenUserNotFound() {
      // When
      Optional<User> result = userRepository.findById(UUID.randomUUID());

      // Then
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("findByPhoneNumberOrEmail Tests")
  class FindByPhoneNumberOrEmailTests {

    @Test
    @DisplayName("Should find user by email")
    void shouldFindUserByEmail() {
      // When
      Optional<User> result = userRepository.findByPhoneNumberOrEmail(
          "nonexistent", "john.doe@example.com");

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().getEmail()).isEqualTo("john.doe@example.com");
    }

    @Test
    @DisplayName("Should find user by phone number")
    void shouldFindUserByPhoneNumber() {
      // When
      Optional<User> result = userRepository.findByPhoneNumberOrEmail(
          "+6281234567890", "nonexistent@email.com");

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().getPhoneNumber()).isEqualTo("+6281234567890");
    }

    @Test
    @DisplayName("Should return empty when neither phone nor email matches")
    void shouldReturnEmptyWhenNoMatch() {
      // When
      Optional<User> result = userRepository.findByPhoneNumberOrEmail(
          "nonexistent", "nonexistent@email.com");

      // Then
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("save Tests")
  class SaveTests {

    @Test
    @DisplayName("Should save new user successfully")
    void shouldSaveNewUserSuccessfully() {
      // Given
      User newUser = User.builder()
          .fullName("Jane Smith")
          .email("jane.smith@example.com")
          .phoneNumber("+6289876543210")
          .gender(Gender.FEMALE)
          .dob(LocalDate.of(1995, 8, 20))
          .passwordHash("$2a$10$anotherHashedPassword")
          .build();

      // When
      User savedUser = userRepository.save(newUser);
      entityManager.flush();

      // Then
      assertThat(savedUser.getId()).isNotNull();
      assertThat(savedUser.getFullName()).isEqualTo("Jane Smith");
      assertThat(savedUser.getCreatedAt()).isNotNull();
      assertThat(savedUser.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should update existing user successfully")
    void shouldUpdateExistingUserSuccessfully() {
      // Given
      testUser.setFullName("John Updated");

      // When
      User updatedUser = userRepository.save(testUser);
      entityManager.flush();

      // Then
      assertThat(updatedUser.getFullName()).isEqualTo("John Updated");
    }
  }

  @Nested
  @DisplayName("delete Tests")
  class DeleteTests {

    @Test
    @DisplayName("Should delete user successfully")
    void shouldDeleteUserSuccessfully() {
      // Given
      UUID userId = testUser.getId();

      // When
      userRepository.delete(testUser);
      entityManager.flush();

      // Then
      Optional<User> result = userRepository.findById(userId);
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("findAll Tests")
  class FindAllTests {

    @Test
    @DisplayName("Should find all users")
    void shouldFindAllUsers() {
      // Given - testUser already exists
      User anotherUser = User.builder()
          .fullName("Another User")
          .email("another@example.com")
          .phoneNumber("+6287654321098")
          .gender(Gender.OTHER)
          .passwordHash("$2a$10$hash")
          .build();
      entityManager.persistAndFlush(anotherUser);

      // When
      var users = userRepository.findAll();

      // Then
      assertThat(users).hasSize(2);
    }
  }
}


