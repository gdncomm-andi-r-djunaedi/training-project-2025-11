package com.gdn.project.waroenk.member.fixture;

import com.gdn.project.waroenk.member.constant.Gender;
import com.gdn.project.waroenk.member.entity.Address;
import com.gdn.project.waroenk.member.entity.Token;
import com.gdn.project.waroenk.member.entity.User;
import net.datafaker.Faker;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

/**
 * Factory class for creating test data using DataFaker.
 * Provides consistent mock data for unit and integration tests.
 */
public class TestDataFactory {

  private static final Faker faker = new Faker();

  /**
   * Creates a User entity with random data.
   */
  public static User createUser() {
    return User.builder()
        .id(UUID.randomUUID())
        .fullName(faker.name().fullName())
        .email(faker.internet().emailAddress())
        .phoneNumber(faker.phoneNumber().cellPhone())
        .gender(faker.options().option(Gender.class))
        .dob(LocalDate.now().minusYears(faker.number().numberBetween(18, 60)))
        .passwordHash("$2a$10$hashedPasswordExample123456789")
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
  }

  /**
   * Creates a User entity with specific email.
   */
  public static User createUserWithEmail(String email) {
    User user = createUser();
    user.setEmail(email);
    return user;
  }

  /**
   * Creates a User entity with specific phone number.
   */
  public static User createUserWithPhone(String phone) {
    User user = createUser();
    user.setPhoneNumber(phone);
    return user;
  }

  /**
   * Creates a User entity with specific ID.
   */
  public static User createUserWithId(UUID id) {
    User user = createUser();
    user.setId(id);
    return user;
  }

  /**
   * Creates a User entity without ID (for new user creation).
   */
  public static User createNewUser() {
    return User.builder()
        .fullName(faker.name().fullName())
        .email(faker.internet().emailAddress())
        .phoneNumber(faker.phoneNumber().cellPhone())
        .gender(faker.options().option(Gender.class))
        .dob(LocalDate.now().minusYears(faker.number().numberBetween(18, 60)))
        .passwordHash("$2a$10$hashedPasswordExample123456789")
        .build();
  }

  /**
   * Creates an Address entity with random data.
   */
  public static Address createAddress() {
    return Address.builder()
        .id(UUID.randomUUID())
        .country("Indonesia")
        .province(faker.address().state())
        .city(faker.address().city())
        .district(faker.address().cityName())
        .subdistrict(faker.address().streetName())
        .postalCode(faker.address().zipCode())
        .street(faker.address().streetAddress())
        .details(faker.address().secondaryAddress())
        .label(faker.options().option("Home", "Office", "Apartment", "Warehouse"))
        .latitude(BigDecimal.valueOf(faker.number().randomDouble(7, -90, 90)))
        .longitude(BigDecimal.valueOf(faker.number().randomDouble(7, -180, 180)))
        .createdAt(LocalDateTime.now())
        .updatedAt(LocalDateTime.now())
        .build();
  }

  /**
   * Creates an Address entity with specific user.
   */
  public static Address createAddressForUser(User user) {
    Address address = createAddress();
    address.setUser(user);
    return address;
  }

  /**
   * Creates an Address entity with specific label.
   */
  public static Address createAddressWithLabel(String label) {
    Address address = createAddress();
    address.setLabel(label);
    return address;
  }

  /**
   * Creates an Address entity without ID (for new address creation).
   */
  public static Address createNewAddress() {
    return Address.builder()
        .country("Indonesia")
        .province(faker.address().state())
        .city(faker.address().city())
        .district(faker.address().cityName())
        .subdistrict(faker.address().streetName())
        .postalCode(faker.address().zipCode())
        .street(faker.address().streetAddress())
        .details(faker.address().secondaryAddress())
        .label(faker.options().option("Home", "Office", "Apartment", "Warehouse"))
        .latitude(BigDecimal.valueOf(faker.number().randomDouble(7, -90, 90)))
        .longitude(BigDecimal.valueOf(faker.number().randomDouble(7, -180, 180)))
        .build();
  }

  /**
   * Creates a Token entity for a user.
   */
  public static Token createToken(User user) {
    return Token.builder()
        .id(UUID.randomUUID())
        .user(user)
        .refreshToken(UUID.randomUUID().toString())
        .expiresAt(LocalDateTime.now().plusHours(24))
        .createdAt(LocalDateTime.now())
        .build();
  }

  /**
   * Creates an expired Token entity.
   */
  public static Token createExpiredToken(User user) {
    return Token.builder()
        .id(UUID.randomUUID())
        .user(user)
        .refreshToken(UUID.randomUUID().toString())
        .expiresAt(LocalDateTime.now().minusHours(1))
        .createdAt(LocalDateTime.now().minusHours(25))
        .build();
  }

  /**
   * Generates a valid password that meets strong password requirements.
   */
  public static String generateValidPassword() {
    return "Test@1234!Secure";
  }

  /**
   * Generates a weak password for validation tests.
   */
  public static String generateWeakPassword() {
    return "weak";
  }

  /**
   * Creates a random email address.
   */
  public static String randomEmail() {
    return faker.internet().emailAddress();
  }

  /**
   * Creates a random phone number.
   */
  public static String randomPhone() {
    return faker.phoneNumber().cellPhone();
  }

  /**
   * Creates a random UUID string.
   */
  public static String randomUuidString() {
    return UUID.randomUUID().toString();
  }
}


