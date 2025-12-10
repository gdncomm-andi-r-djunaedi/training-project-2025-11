package com.gdn.project.waroenk.member.integration;

import com.gdn.project.waroenk.member.constant.Gender;
import com.gdn.project.waroenk.member.entity.Address;
import com.gdn.project.waroenk.member.entity.User;
import com.gdn.project.waroenk.member.exceptions.ResourceNotFoundException;
import com.gdn.project.waroenk.member.exceptions.ValidationException;
import com.gdn.project.waroenk.member.repository.AddressRepository;
import com.gdn.project.waroenk.member.repository.UserRepository;
import com.gdn.project.waroenk.member.service.AddressService;
import com.gdn.project.waroenk.member.utility.CacheUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
@DisplayName("AddressService Integration Tests")
class AddressServiceIntegrationTest {

  @Autowired
  private AddressService addressService;

  @Autowired
  private AddressRepository addressRepository;

  @Autowired
  private UserRepository userRepository;

  @MockitoBean
  private CacheUtil<Address> addressCacheUtil;

  @MockitoBean
  private CacheUtil<String> stringCacheUtil;

  private User testUser;
  private Address testAddress;

  @BeforeEach
  void setUp() {
    // Mock cache to always return null (simulate cache miss)
    when(addressCacheUtil.getValue(anyString())).thenReturn(null);
    when(stringCacheUtil.getValue(anyString())).thenReturn(null);

    // Create test user
    testUser = User.builder()
        .fullName("Address Test User")
        .email("address.test@example.com")
        .phoneNumber("+6285555555555")
        .gender(Gender.MALE)
        .dob(LocalDate.of(1990, 1, 1))
        .passwordHash("$2a$10$hashedPassword")
        .build();
    testUser = userRepository.save(testUser);

    // Create test address
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
    testAddress = addressRepository.save(testAddress);
  }

  @Nested
  @DisplayName("findAddressById Integration Tests")
  class FindAddressByIdIntegrationTests {

    @Test
    @DisplayName("Should find address by ID from database")
    void shouldFindAddressByIdFromDatabase() {
      // When
      Address result = addressService.findAddressById(testAddress.getId().toString());

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getLabel()).isEqualTo("Home");
      assertThat(result.getCity()).isEqualTo("Jakarta Selatan");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for non-existent address")
    void shouldThrowExceptionForNonExistentAddress() {
      // Given
      String nonExistentId = UUID.randomUUID().toString();

      // When/Then
      assertThatThrownBy(() -> addressService.findAddressById(nonExistentId))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Address with id: " + nonExistentId + " not found");
    }
  }

  @Nested
  @DisplayName("findUserAddressByLabel Integration Tests")
  class FindUserAddressByLabelIntegrationTests {

    @Test
    @DisplayName("Should find address by user ID and label")
    void shouldFindAddressByUserIdAndLabel() {
      // When
      Address result = addressService.findUserAddressByLabel(
          testUser.getId().toString(), "Home");

      // Then
      assertThat(result).isNotNull();
      assertThat(result.getLabel()).isEqualTo("Home");
      assertThat(result.getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for non-existent label")
    void shouldThrowExceptionForNonExistentLabel() {
      // When/Then
      assertThatThrownBy(() -> addressService.findUserAddressByLabel(
          testUser.getId().toString(), "NonExistent"))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Address with label: NonExistent not found");
    }
  }

  @Nested
  @DisplayName("createAddress Integration Tests")
  class CreateAddressIntegrationTests {

    @Test
    @DisplayName("Should create new address successfully")
    void shouldCreateNewAddressSuccessfully() {
      // Given
      Address newAddress = Address.builder()
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
      Address result = addressService.createAddress(
          testUser.getId().toString(), newAddress, false);

      // Then
      assertThat(result.getId()).isNotNull();
      assertThat(result.getLabel()).isEqualTo("Office");
      assertThat(result.getUser().getId()).isEqualTo(testUser.getId());
    }

    @Test
    @DisplayName("Should update existing address with same label")
    void shouldUpdateExistingAddressWithSameLabel() {
      // Given
      Address updateAddress = Address.builder()
          .label("Home") // Same label as testAddress
          .country("Indonesia")
          .province("Jawa Barat")
          .city("Bandung")
          .district("Coblong")
          .subdistrict("Lebak Siliwangi")
          .postalCode("40132")
          .street("Jl. Dipatiukur No. 35")
          .details("Updated details")
          .build();

      // When
      Address result = addressService.createAddress(
          testUser.getId().toString(), updateAddress, false);

      // Then
      assertThat(result.getId()).isEqualTo(testAddress.getId()); // Same ID (updated)
      assertThat(result.getCity()).isEqualTo("Bandung"); // Updated city
      assertThat(result.getDetails()).isEqualTo("Updated details");
    }

    @Test
    @DisplayName("Should set as default when flag is true")
    void shouldSetAsDefaultWhenFlagIsTrue() {
      // Given
      Address newAddress = Address.builder()
          .label("Warehouse")
          .country("Indonesia")
          .province("Banten")
          .city("Tangerang")
          .district("Cipondoh")
          .subdistrict("Cipondoh Indah")
          .postalCode("15148")
          .street("Jl. Raya Cipondoh No. 10")
          .build();

      // When
      Address result = addressService.createAddress(
          testUser.getId().toString(), newAddress, true);

      // Then
      User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
      assertThat(updatedUser.getDefaultAddress()).isNotNull();
      assertThat(updatedUser.getDefaultAddress().getId()).isEqualTo(result.getId());
    }

    @Test
    @DisplayName("Should auto-set as default for first address")
    void shouldAutoSetAsDefaultForFirstAddress() {
      // Given - create a new user without any addresses
      User newUser = User.builder()
          .fullName("New User No Address")
          .email("newuser@example.com")
          .phoneNumber("+6286666666666")
          .gender(Gender.FEMALE)
          .passwordHash("$2a$10$hash")
          .build();
      newUser = userRepository.save(newUser);

      Address firstAddress = Address.builder()
          .label("First Home")
          .country("Indonesia")
          .province("DKI Jakarta")
          .city("Jakarta Barat")
          .district("Kebon Jeruk")
          .subdistrict("Kedoya Utara")
          .postalCode("11520")
          .street("Jl. Kedoya Raya No. 1")
          .build();

      // When
      Address result = addressService.createAddress(
          newUser.getId().toString(), firstAddress, false);

      // Then
      User updatedUser = userRepository.findById(newUser.getId()).orElseThrow();
      assertThat(updatedUser.getDefaultAddress()).isNotNull();
      assertThat(updatedUser.getDefaultAddress().getId()).isEqualTo(result.getId());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for non-existent user")
    void shouldThrowExceptionForNonExistentUser() {
      // Given
      String nonExistentUserId = UUID.randomUUID().toString();
      Address newAddress = Address.builder()
          .label("Test")
          .country("Indonesia")
          .province("DKI Jakarta")
          .city("Jakarta")
          .district("District")
          .subdistrict("Subdistrict")
          .postalCode("12345")
          .street("Street")
          .build();

      // When/Then
      assertThatThrownBy(() -> addressService.createAddress(
          nonExistentUserId, newAddress, false))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("User with id: " + nonExistentUserId + " not found");
    }
  }

  @Nested
  @DisplayName("setDefaultAddress Integration Tests")
  class SetDefaultAddressIntegrationTests {

    @Test
    @DisplayName("Should set default address successfully")
    void shouldSetDefaultAddressSuccessfully() {
      // When
      boolean result = addressService.setDefaultAddress(
          testUser.getId().toString(), testAddress.getId().toString());

      // Then
      assertThat(result).isTrue();
      User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
      assertThat(updatedUser.getDefaultAddress().getId()).isEqualTo(testAddress.getId());
    }

    @Test
    @DisplayName("Should throw ValidationException when address doesn't belong to user")
    void shouldThrowExceptionWhenAddressDoesntBelongToUser() {
      // Given - create another user
      User anotherUser = User.builder()
          .fullName("Another User")
          .email("another@example.com")
          .phoneNumber("+6287777777777")
          .gender(Gender.MALE)
          .passwordHash("$2a$10$hash")
          .build();
      anotherUser = userRepository.save(anotherUser);

      // When/Then - try to set testAddress as default for anotherUser
      String anotherUserId = anotherUser.getId().toString();
      String testAddressId = testAddress.getId().toString();
      
      assertThatThrownBy(() -> addressService.setDefaultAddress(anotherUserId, testAddressId))
          .isInstanceOf(ValidationException.class)
          .hasMessage("Address does not belong to this user");
    }
  }

  @Nested
  @DisplayName("deleteUserAddress Integration Tests")
  class DeleteUserAddressIntegrationTests {

    @Test
    @DisplayName("Should delete address successfully")
    void shouldDeleteAddressSuccessfully() {
      // Given
      UUID addressId = testAddress.getId();

      // When
      boolean result = addressService.deleteUserAddress(addressId.toString());

      // Then
      assertThat(result).isTrue();
      assertThat(addressRepository.findById(addressId)).isEmpty();
    }

    @Test
    @DisplayName("Should clear default address reference when deleting default address")
    void shouldClearDefaultAddressWhenDeletingDefault() {
      // Given - set testAddress as default
      testUser.setDefaultAddress(testAddress);
      userRepository.save(testUser);

      // When
      boolean result = addressService.deleteUserAddress(testAddress.getId().toString());

      // Then
      assertThat(result).isTrue();
      User updatedUser = userRepository.findById(testUser.getId()).orElseThrow();
      assertThat(updatedUser.getDefaultAddress()).isNull();
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException for non-existent address")
    void shouldThrowExceptionForNonExistentAddress() {
      // Given
      String nonExistentId = UUID.randomUUID().toString();

      // When/Then
      assertThatThrownBy(() -> addressService.deleteUserAddress(nonExistentId))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Address with id: " + nonExistentId + " not found");
    }
  }

  @Nested
  @DisplayName("getDefaultAddressId Integration Tests")
  class GetDefaultAddressIdIntegrationTests {

    @Test
    @DisplayName("Should return default address ID")
    void shouldReturnDefaultAddressId() {
      // Given - set testAddress as default
      testUser.setDefaultAddress(testAddress);
      userRepository.save(testUser);

      // When
      UUID result = addressService.getDefaultAddressId(testUser.getId().toString());

      // Then
      assertThat(result).isEqualTo(testAddress.getId());
    }

    @Test
    @DisplayName("Should return null when no default address")
    void shouldReturnNullWhenNoDefaultAddress() {
      // Given - testUser has no default address

      // When
      UUID result = addressService.getDefaultAddressId(testUser.getId().toString());

      // Then
      assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null for non-existent user")
    void shouldReturnNullForNonExistentUser() {
      // When
      UUID result = addressService.getDefaultAddressId(UUID.randomUUID().toString());

      // Then
      assertThat(result).isNull();
    }
  }
}


