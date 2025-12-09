package com.gdn.project.waroenk.member.service;

import com.gdn.project.waroenk.member.FilterAddressRequest;
import com.gdn.project.waroenk.member.SortBy;
import com.gdn.project.waroenk.member.entity.Address;
import com.gdn.project.waroenk.member.entity.User;
import com.gdn.project.waroenk.member.exceptions.ResourceNotFoundException;
import com.gdn.project.waroenk.member.exceptions.ValidationException;
import com.gdn.project.waroenk.member.fixture.TestDataFactory;
import com.gdn.project.waroenk.member.repository.AddressRepository;
import com.gdn.project.waroenk.member.repository.UserRepository;
import com.gdn.project.waroenk.member.utility.CacheUtil;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddressServiceImpl Unit Tests")
class AddressServiceImplTest {

  @Mock
  private AddressRepository addressRepository;

  @Mock
  private UserRepository userRepository;

  @Mock
  private CacheUtil<Address> addressCacheUtil;

  @Mock
  private CacheUtil<String> stringCacheUtil;

  @Mock
  private EntityManager entityManager;

  private AddressServiceImpl addressService;

  @BeforeEach
  void setUp() {
    addressService = new AddressServiceImpl(
        addressRepository,
        userRepository,
        addressCacheUtil,
        stringCacheUtil,
        entityManager
    );
    ReflectionTestUtils.setField(addressService, "defaultItemPerPage", 10);
  }

  @Nested
  @DisplayName("findAddressById Tests")
  class FindAddressByIdTests {

    @Test
    @DisplayName("Should return cached address when available")
    void shouldReturnCachedAddress() {
      // Given
      Address cachedAddress = TestDataFactory.createAddress();
      String addressId = cachedAddress.getId().toString();
      String cacheKey = "address:id:" + addressId;

      when(addressCacheUtil.getValue(cacheKey)).thenReturn(cachedAddress);

      // When
      Address result = addressService.findAddressById(addressId);

      // Then
      assertThat(result).isEqualTo(cachedAddress);
      verify(addressRepository, never()).findById(any());
    }

    @Test
    @DisplayName("Should fetch from database and cache when not in cache")
    void shouldFetchFromDatabaseAndCache() {
      // Given
      Address address = TestDataFactory.createAddress();
      String addressId = address.getId().toString();
      String cacheKey = "address:id:" + addressId;

      when(addressCacheUtil.getValue(cacheKey)).thenReturn(null);
      when(addressRepository.findById(address.getId())).thenReturn(Optional.of(address));

      // When
      Address result = addressService.findAddressById(addressId);

      // Then
      assertThat(result).isEqualTo(address);
      verify(addressCacheUtil).putValue(eq(cacheKey), eq(address), anyLong(), any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when address not found")
    void shouldThrowExceptionWhenAddressNotFound() {
      // Given
      String addressId = UUID.randomUUID().toString();
      String cacheKey = "address:id:" + addressId;

      when(addressCacheUtil.getValue(cacheKey)).thenReturn(null);
      when(addressRepository.findById(any())).thenReturn(Optional.empty());

      // When/Then
      assertThatThrownBy(() -> addressService.findAddressById(addressId))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Address with id: " + addressId + " not found");
    }
  }

  @Nested
  @DisplayName("findUserAddressByLabel Tests")
  class FindUserAddressByLabelTests {

    @Test
    @DisplayName("Should return cached address when available")
    void shouldReturnCachedAddress() {
      // Given
      User user = TestDataFactory.createUser();
      Address cachedAddress = TestDataFactory.createAddressForUser(user);
      cachedAddress.setLabel("Home");
      String userId = user.getId().toString();
      String cacheKey = "address:user:" + userId + ":label:home";

      when(addressCacheUtil.getValue(cacheKey)).thenReturn(cachedAddress);

      // When
      Address result = addressService.findUserAddressByLabel(userId, "Home");

      // Then
      assertThat(result).isEqualTo(cachedAddress);
      verify(addressRepository, never()).findByUserIdAndLabel(any(), any());
    }

    @Test
    @DisplayName("Should throw ValidationException when userId is blank")
    void shouldThrowValidationExceptionWhenUserIdBlank() {
      // When/Then
      assertThatThrownBy(() -> addressService.findUserAddressByLabel("", "Home"))
          .isInstanceOf(ValidationException.class)
          .hasMessage("User ID and label are required");
    }

    @Test
    @DisplayName("Should throw ValidationException when label is blank")
    void shouldThrowValidationExceptionWhenLabelBlank() {
      // When/Then
      assertThatThrownBy(() -> addressService.findUserAddressByLabel(UUID.randomUUID().toString(), ""))
          .isInstanceOf(ValidationException.class)
          .hasMessage("User ID and label are required");
    }

    @Test
    @DisplayName("Should fetch from database and cache when not in cache")
    void shouldFetchFromDatabaseAndCache() {
      // Given
      User user = TestDataFactory.createUser();
      Address address = TestDataFactory.createAddressForUser(user);
      address.setLabel("Office");
      String userId = user.getId().toString();
      String cacheKey = "address:user:" + userId + ":label:office";

      when(addressCacheUtil.getValue(cacheKey)).thenReturn(null);
      when(addressRepository.findByUserIdAndLabel(user.getId(), "Office"))
          .thenReturn(Optional.of(address));

      // When
      Address result = addressService.findUserAddressByLabel(userId, "Office");

      // Then
      assertThat(result).isEqualTo(address);
      verify(addressCacheUtil).putValue(eq(cacheKey), eq(address), anyLong(), any());
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when address not found")
    void shouldThrowExceptionWhenAddressNotFound() {
      // Given
      String userId = UUID.randomUUID().toString();
      String label = "NonExistent";
      String cacheKey = "address:user:" + userId + ":label:nonexistent";

      when(addressCacheUtil.getValue(cacheKey)).thenReturn(null);
      when(addressRepository.findByUserIdAndLabel(any(), anyString()))
          .thenReturn(Optional.empty());

      // When/Then
      assertThatThrownBy(() -> addressService.findUserAddressByLabel(userId, label))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Address with label: " + label + " not found for user: " + userId);
    }
  }

  @Nested
  @DisplayName("createAddress Tests")
  class CreateAddressTests {

    @Test
    @DisplayName("Should create new address successfully")
    void shouldCreateNewAddressSuccessfully() {
      // Given
      User user = TestDataFactory.createUser();
      Address newAddress = TestDataFactory.createNewAddress();
      newAddress.setLabel("New Home");
      Address savedAddress = TestDataFactory.createAddressForUser(user);
      savedAddress.setLabel("New Home");

      when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
      when(addressRepository.findByUserIdAndLabel(user.getId(), "New Home"))
          .thenReturn(Optional.empty());
      when(addressRepository.save(any(Address.class))).thenReturn(savedAddress);
      when(userRepository.save(user)).thenReturn(user);

      // When
      Address result = addressService.createAddress(user.getId().toString(), newAddress, false);

      // Then
      assertThat(result).isEqualTo(savedAddress);
      verify(addressRepository).save(any(Address.class));
    }

    @Test
    @DisplayName("Should update existing address with same label")
    void shouldUpdateExistingAddressWithSameLabel() {
      // Given
      User user = TestDataFactory.createUser();
      Address existingAddress = TestDataFactory.createAddressForUser(user);
      existingAddress.setLabel("Home");
      Address updateRequest = TestDataFactory.createNewAddress();
      updateRequest.setLabel("Home");
      updateRequest.setStreet("Updated Street");

      when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
      when(addressRepository.findByUserIdAndLabel(user.getId(), "Home"))
          .thenReturn(Optional.of(existingAddress));
      when(addressRepository.save(existingAddress)).thenReturn(existingAddress);

      // When
      Address result = addressService.createAddress(user.getId().toString(), updateRequest, false);

      // Then
      assertThat(result.getStreet()).isEqualTo("Updated Street");
      verify(addressRepository).save(existingAddress);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
      // Given
      String userId = UUID.randomUUID().toString();
      Address newAddress = TestDataFactory.createNewAddress();

      when(userRepository.findById(any())).thenReturn(Optional.empty());

      // When/Then
      assertThatThrownBy(() -> addressService.createAddress(userId, newAddress, false))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("User with id: " + userId + " not found");
    }

    @Test
    @DisplayName("Should set as default when setAsDefault is true")
    void shouldSetAsDefaultWhenFlagIsTrue() {
      // Given
      User user = TestDataFactory.createUser();
      user.setDefaultAddress(TestDataFactory.createAddress()); // User already has a default
      Address newAddress = TestDataFactory.createNewAddress();
      newAddress.setLabel("Office");
      Address savedAddress = TestDataFactory.createAddressForUser(user);
      savedAddress.setLabel("Office");

      when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
      when(addressRepository.findByUserIdAndLabel(user.getId(), "Office"))
          .thenReturn(Optional.empty());
      when(addressRepository.save(any(Address.class))).thenReturn(savedAddress);
      when(userRepository.save(user)).thenReturn(user);

      // When
      addressService.createAddress(user.getId().toString(), newAddress, true);

      // Then
      verify(userRepository).save(user);
      assertThat(user.getDefaultAddress()).isEqualTo(savedAddress);
    }

    @Test
    @DisplayName("Should auto-set as default for first address")
    void shouldAutoSetAsDefaultForFirstAddress() {
      // Given
      User user = TestDataFactory.createUser();
      user.setDefaultAddress(null); // No default address
      Address newAddress = TestDataFactory.createNewAddress();
      newAddress.setLabel("First Home");
      Address savedAddress = TestDataFactory.createAddressForUser(user);
      savedAddress.setLabel("First Home");

      when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
      when(addressRepository.findByUserIdAndLabel(user.getId(), "First Home"))
          .thenReturn(Optional.empty());
      when(addressRepository.save(any(Address.class))).thenReturn(savedAddress);
      when(userRepository.save(user)).thenReturn(user);

      // When
      addressService.createAddress(user.getId().toString(), newAddress, false);

      // Then
      verify(userRepository).save(user);
      assertThat(user.getDefaultAddress()).isEqualTo(savedAddress);
    }
  }

  @Nested
  @DisplayName("setDefaultAddress Tests")
  class SetDefaultAddressTests {

    @Test
    @DisplayName("Should set default address successfully")
    void shouldSetDefaultAddressSuccessfully() {
      // Given
      User user = TestDataFactory.createUser();
      Address address = TestDataFactory.createAddressForUser(user);
      String userId = user.getId().toString();
      String addressId = address.getId().toString();

      when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
      when(addressRepository.findById(address.getId())).thenReturn(Optional.of(address));
      when(userRepository.save(user)).thenReturn(user);

      // When
      boolean result = addressService.setDefaultAddress(userId, addressId);

      // Then
      assertThat(result).isTrue();
      assertThat(user.getDefaultAddress()).isEqualTo(address);
      verify(userRepository).save(user);
    }

    @Test
    @DisplayName("Should throw ValidationException when userId is blank")
    void shouldThrowValidationExceptionWhenUserIdBlank() {
      // When/Then
      assertThatThrownBy(() -> addressService.setDefaultAddress("", "addressId"))
          .isInstanceOf(ValidationException.class)
          .hasMessage("User ID and address ID are required");
    }

    @Test
    @DisplayName("Should throw ValidationException when addressId is blank")
    void shouldThrowValidationExceptionWhenAddressIdBlank() {
      // When/Then
      assertThatThrownBy(() -> addressService.setDefaultAddress("userId", ""))
          .isInstanceOf(ValidationException.class)
          .hasMessage("User ID and address ID are required");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when user not found")
    void shouldThrowExceptionWhenUserNotFound() {
      // Given
      String userId = UUID.randomUUID().toString();
      String addressId = UUID.randomUUID().toString();

      when(userRepository.findById(any())).thenReturn(Optional.empty());

      // When/Then
      assertThatThrownBy(() -> addressService.setDefaultAddress(userId, addressId))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("User with id: " + userId + " not found");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when address not found")
    void shouldThrowExceptionWhenAddressNotFound() {
      // Given
      User user = TestDataFactory.createUser();
      String userId = user.getId().toString();
      String addressId = UUID.randomUUID().toString();

      when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
      when(addressRepository.findById(any())).thenReturn(Optional.empty());

      // When/Then
      assertThatThrownBy(() -> addressService.setDefaultAddress(userId, addressId))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Address with id: " + addressId + " not found");
    }

    @Test
    @DisplayName("Should throw ValidationException when address doesn't belong to user")
    void shouldThrowExceptionWhenAddressNotBelongsToUser() {
      // Given
      User user = TestDataFactory.createUser();
      User anotherUser = TestDataFactory.createUser();
      Address address = TestDataFactory.createAddressForUser(anotherUser);
      String userId = user.getId().toString();
      String addressId = address.getId().toString();

      when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));
      when(addressRepository.findById(address.getId())).thenReturn(Optional.of(address));

      // When/Then
      assertThatThrownBy(() -> addressService.setDefaultAddress(userId, addressId))
          .isInstanceOf(ValidationException.class)
          .hasMessage("Address does not belong to this user");
    }
  }

  @Nested
  @DisplayName("deleteUserAddress Tests")
  class DeleteUserAddressTests {

    @Test
    @DisplayName("Should delete address successfully")
    void shouldDeleteAddressSuccessfully() {
      // Given
      User user = TestDataFactory.createUser();
      Address address = TestDataFactory.createAddressForUser(user);
      user.setDefaultAddress(null); // Not the default address

      when(addressRepository.findById(address.getId())).thenReturn(Optional.of(address));

      // When
      boolean result = addressService.deleteUserAddress(address.getId().toString());

      // Then
      assertThat(result).isTrue();
      verify(addressRepository).delete(address);
    }

    @Test
    @DisplayName("Should clear default address reference when deleting default address")
    void shouldClearDefaultAddressWhenDeletingDefault() {
      // Given
      User user = TestDataFactory.createUser();
      Address address = TestDataFactory.createAddressForUser(user);
      user.setDefaultAddress(address); // This IS the default address

      when(addressRepository.findById(address.getId())).thenReturn(Optional.of(address));
      when(userRepository.save(user)).thenReturn(user);

      // When
      boolean result = addressService.deleteUserAddress(address.getId().toString());

      // Then
      assertThat(result).isTrue();
      assertThat(user.getDefaultAddress()).isNull();
      verify(userRepository).save(user);
      verify(addressRepository).delete(address);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when address not found")
    void shouldThrowExceptionWhenAddressNotFound() {
      // Given
      String addressId = UUID.randomUUID().toString();

      when(addressRepository.findById(any())).thenReturn(Optional.empty());

      // When/Then
      assertThatThrownBy(() -> addressService.deleteUserAddress(addressId))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Address with id: " + addressId + " not found");
    }
  }

  @Nested
  @DisplayName("getDefaultAddressId Tests")
  class GetDefaultAddressIdTests {

    @Test
    @DisplayName("Should return default address ID when user has default address")
    void shouldReturnDefaultAddressId() {
      // Given
      User user = TestDataFactory.createUser();
      Address defaultAddress = TestDataFactory.createAddressForUser(user);
      user.setDefaultAddress(defaultAddress);

      when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

      // When
      UUID result = addressService.getDefaultAddressId(user.getId().toString());

      // Then
      assertThat(result).isEqualTo(defaultAddress.getId());
    }

    @Test
    @DisplayName("Should return null when user has no default address")
    void shouldReturnNullWhenNoDefaultAddress() {
      // Given
      User user = TestDataFactory.createUser();
      user.setDefaultAddress(null);

      when(userRepository.findById(user.getId())).thenReturn(Optional.of(user));

      // When
      UUID result = addressService.getDefaultAddressId(user.getId().toString());

      // Then
      assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null when userId is blank")
    void shouldReturnNullWhenUserIdBlank() {
      // When
      UUID result = addressService.getDefaultAddressId("");

      // Then
      assertThat(result).isNull();
    }

    @Test
    @DisplayName("Should return null when user not found")
    void shouldReturnNullWhenUserNotFound() {
      // Given
      String userId = UUID.randomUUID().toString();
      when(userRepository.findById(any())).thenReturn(Optional.empty());

      // When
      UUID result = addressService.getDefaultAddressId(userId);

      // Then
      assertThat(result).isNull();
    }
  }

  @Nested
  @DisplayName("filterUserAddress Tests")
  class FilterUserAddressTests {

    @Test
    @DisplayName("Should throw ValidationException when userId is blank")
    void shouldThrowValidationExceptionWhenUserIdBlank() {
      // Given
      FilterAddressRequest request = FilterAddressRequest.newBuilder()
          .setUser("")
          .setSize(10)
          .setSortBy(SortBy.newBuilder().setField("createdAt").setDirection("DESC").build())
          .build();

      // When/Then
      assertThatThrownBy(() -> addressService.filterUserAddress(request))
          .isInstanceOf(ValidationException.class)
          .hasMessage("User ID is required");
    }

    @Test
    @DisplayName("Should throw ValidationException when label filter is too short")
    void shouldThrowValidationExceptionWhenLabelTooShort() {
      // Given
      FilterAddressRequest request = FilterAddressRequest.newBuilder()
          .setUser(UUID.randomUUID().toString())
          .setLabel("ab")
          .setSize(10)
          .setSortBy(SortBy.newBuilder().setField("createdAt").setDirection("DESC").build())
          .build();

      // When/Then
      assertThatThrownBy(() -> addressService.filterUserAddress(request))
          .isInstanceOf(ValidationException.class)
          .hasMessage("Filter query must be at least 3 characters");
    }
  }
}

