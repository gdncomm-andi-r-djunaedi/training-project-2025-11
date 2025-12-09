package com.gdn.project.waroenk.member.integration;

import com.gdn.project.waroenk.member.constant.Gender;
import com.gdn.project.waroenk.member.entity.Address;
import com.gdn.project.waroenk.member.entity.User;
import com.gdn.project.waroenk.member.repository.AddressRepository;
import com.gdn.project.waroenk.member.repository.UserRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.boot.test.autoconfigure.orm.jpa.TestEntityManager;
import org.springframework.test.context.ActiveProfiles;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@ActiveProfiles("test")
@DisplayName("AddressRepository Integration Tests")
class AddressRepositoryIntegrationTest {

  @Autowired
  private AddressRepository addressRepository;

  @Autowired
  private UserRepository userRepository;

  @Autowired
  private TestEntityManager entityManager;

  private User testUser;
  private Address testAddress;

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

    // Create and persist a test address
    testAddress = Address.builder()
        .user(testUser)
        .label("Home")
        .country("Indonesia")
        .province("DKI Jakarta")
        .city("Jakarta Selatan")
        .district("Kebayoran Baru")
        .subdistrict("Senayan")
        .postalCode("12190")
        .street("Jl. Jendral Sudirman No. 123")
        .details("Gedung A, Lantai 5")
        .latitude(BigDecimal.valueOf(-6.2088))
        .longitude(BigDecimal.valueOf(106.8456))
        .build();
    testAddress = entityManager.persistAndFlush(testAddress);
    entityManager.clear();
  }

  @Nested
  @DisplayName("findById Tests")
  class FindByIdTests {

    @Test
    @DisplayName("Should find address by ID")
    void shouldFindAddressById() {
      // When
      Optional<Address> result = addressRepository.findById(testAddress.getId());

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().getLabel()).isEqualTo("Home");
      assertThat(result.get().getCity()).isEqualTo("Jakarta Selatan");
    }

    @Test
    @DisplayName("Should return empty when address not found")
    void shouldReturnEmptyWhenAddressNotFound() {
      // When
      Optional<Address> result = addressRepository.findById(UUID.randomUUID());

      // Then
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("findByLabel Tests")
  class FindByLabelTests {

    @Test
    @DisplayName("Should find address by label")
    void shouldFindAddressByLabel() {
      // When
      Optional<Address> result = addressRepository.findByLabel("Home");

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().getLabel()).isEqualTo("Home");
    }

    @Test
    @DisplayName("Should return empty when label not found")
    void shouldReturnEmptyWhenLabelNotFound() {
      // When
      Optional<Address> result = addressRepository.findByLabel("NonExistent");

      // Then
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("findByUserIdAndLabel Tests")
  class FindByUserIdAndLabelTests {

    @Test
    @DisplayName("Should find address by user ID and label")
    void shouldFindAddressByUserIdAndLabel() {
      // When
      Optional<Address> result = addressRepository.findByUserIdAndLabel(
          testUser.getId(), "Home");

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().getLabel()).isEqualTo("Home");
      assertThat(result.get().getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("Should return empty when user ID doesn't match")
    void shouldReturnEmptyWhenUserIdDoesntMatch() {
      // When
      Optional<Address> result = addressRepository.findByUserIdAndLabel(
          UUID.randomUUID(), "Home");

      // Then
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should return empty when label doesn't match")
    void shouldReturnEmptyWhenLabelDoesntMatch() {
      // When
      Optional<Address> result = addressRepository.findByUserIdAndLabel(
          testUser.getId(), "Office");

      // Then
      assertThat(result).isEmpty();
    }

    @Test
    @DisplayName("Should find address with case-insensitive label")
    void shouldFindAddressWithCaseInsensitiveLabel() {
      // When
      Optional<Address> result = addressRepository.findByUserIdAndLabel(
          testUser.getId(), "HOME");

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().getLabel()).isEqualTo("Home");
    }
  }

  @Nested
  @DisplayName("save Tests")
  class SaveTests {

    @Test
    @DisplayName("Should save new address successfully")
    void shouldSaveNewAddressSuccessfully() {
      // Given
      Address newAddress = Address.builder()
          .user(testUser)
          .label("Office")
          .country("Indonesia")
          .province("DKI Jakarta")
          .city("Jakarta Pusat")
          .district("Gambir")
          .subdistrict("Petojo Selatan")
          .postalCode("10110")
          .street("Jl. MH Thamrin No. 1")
          .details("Menara BCA, Lantai 20")
          .latitude(BigDecimal.valueOf(-6.1944))
          .longitude(BigDecimal.valueOf(106.8229))
          .build();

      // When
      Address savedAddress = addressRepository.save(newAddress);
      entityManager.flush();

      // Then
      assertThat(savedAddress.getId()).isNotNull();
      assertThat(savedAddress.getLabel()).isEqualTo("Office");
      assertThat(savedAddress.getCreatedAt()).isNotNull();
      assertThat(savedAddress.getUpdatedAt()).isNotNull();
    }

    @Test
    @DisplayName("Should update existing address successfully")
    void shouldUpdateExistingAddressSuccessfully() {
      // Given
      testAddress.setStreet("Jl. Sudirman No. 456 Updated");

      // When
      Address updatedAddress = addressRepository.save(testAddress);
      entityManager.flush();

      // Then
      assertThat(updatedAddress.getStreet()).isEqualTo("Jl. Sudirman No. 456 Updated");
    }
  }

  @Nested
  @DisplayName("delete Tests")
  class DeleteTests {

    @Test
    @DisplayName("Should delete address successfully")
    void shouldDeleteAddressSuccessfully() {
      // Given
      UUID addressId = testAddress.getId();

      // When
      addressRepository.delete(testAddress);
      entityManager.flush();

      // Then
      Optional<Address> result = addressRepository.findById(addressId);
      assertThat(result).isEmpty();
    }
  }

  @Nested
  @DisplayName("Multiple Addresses Tests")
  class MultipleAddressesTests {

    @Test
    @DisplayName("Should find all addresses for a user")
    void shouldFindAllAddressesForUser() {
      // Given - testAddress already exists
      Address officeAddress = Address.builder()
          .user(testUser)
          .label("Office")
          .country("Indonesia")
          .province("DKI Jakarta")
          .city("Jakarta Pusat")
          .district("Gambir")
          .subdistrict("Petojo Selatan")
          .postalCode("10110")
          .street("Jl. MH Thamrin No. 1")
          .build();
      entityManager.persistAndFlush(officeAddress);

      // When
      var addresses = addressRepository.findAll();

      // Then
      assertThat(addresses).hasSize(2);
    }

    @Test
    @DisplayName("Should not find addresses from other users")
    void shouldNotFindAddressesFromOtherUsers() {
      // Given - create another user with address
      User anotherUser = User.builder()
          .fullName("Jane Smith")
          .email("jane@example.com")
          .phoneNumber("+6289876543210")
          .gender(Gender.FEMALE)
          .passwordHash("$2a$10$hash")
          .build();
      anotherUser = entityManager.persistAndFlush(anotherUser);

      Address anotherAddress = Address.builder()
          .user(anotherUser)
          .label("Home")
          .country("Indonesia")
          .province("Jawa Barat")
          .city("Bandung")
          .district("Coblong")
          .subdistrict("Lebak Siliwangi")
          .postalCode("40132")
          .street("Jl. Dipatiukur No. 35")
          .build();
      entityManager.persistAndFlush(anotherAddress);

      // When - search for testUser's "Home" address
      Optional<Address> result = addressRepository.findByUserIdAndLabel(
          testUser.getId(), "Home");

      // Then
      assertThat(result).isPresent();
      assertThat(result.get().getUser().getId()).isEqualTo(testUser.getId());
      assertThat(result.get().getCity()).isEqualTo("Jakarta Selatan");
    }
  }
}

