package com.gdn.project.waroenk.cart.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.gdn.project.waroenk.cart.client.CatalogGrpcClient;
import com.gdn.project.waroenk.cart.client.MemberGrpcClient;
import com.gdn.project.waroenk.cart.dto.checkout.FinalizeCheckoutResult;
import com.gdn.project.waroenk.cart.dto.checkout.PayCheckoutResult;
import com.gdn.project.waroenk.cart.dto.checkout.PrepareCheckoutResult;
import com.gdn.project.waroenk.cart.entity.AddressSnapshot;
import com.gdn.project.waroenk.cart.entity.Cart;
import com.gdn.project.waroenk.cart.entity.Checkout;
import com.gdn.project.waroenk.cart.exceptions.AuthorizationException;
import com.gdn.project.waroenk.cart.exceptions.ResourceNotFoundException;
import com.gdn.project.waroenk.cart.exceptions.ValidationException;
import com.gdn.project.waroenk.cart.fixture.TestDataFactory;
import com.gdn.project.waroenk.cart.repository.CheckoutRepository;
import com.gdn.project.waroenk.cart.utility.CacheUtil;
import com.gdn.project.waroenk.catalog.BulkAcquireStockResponse;
import com.gdn.project.waroenk.catalog.BulkLockStockResponse;
import com.gdn.project.waroenk.catalog.BulkReleaseStockResponse;
import com.gdn.project.waroenk.catalog.StockOperationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@DisplayName("CheckoutServiceImpl Unit Tests")
class CheckoutServiceImplTest {

  @Mock
  private CheckoutRepository checkoutRepository;

  @Mock
  private CartService cartService;

  @Mock
  private SystemParameterService systemParameterService;

  @Mock
  private CatalogGrpcClient catalogClient;

  @Mock
  private MemberGrpcClient memberClient;

  @Mock
  private CacheUtil<String> cacheUtil;

  @Mock
  private MongoTemplate mongoTemplate;

  private CheckoutServiceImpl checkoutService;
  private ObjectMapper objectMapper;

  private static final String USER_ID = "test-user-123";
  private static final String OTHER_USER_ID = "other-user-456";
  private static final String CHECKOUT_ID = "chk-abc12345";

  @BeforeEach
  void setUp() {
    objectMapper = new ObjectMapper();
    objectMapper.findAndRegisterModules();
    
    checkoutService = new CheckoutServiceImpl(
        checkoutRepository,
        cartService,
        systemParameterService,
        catalogClient,
        memberClient,
        cacheUtil,
        objectMapper,
        mongoTemplate
    );
    ReflectionTestUtils.setField(checkoutService, "defaultCheckoutTtl", 900);
    ReflectionTestUtils.setField(checkoutService, "useRedis", false);
    ReflectionTestUtils.setField(checkoutService, "defaultItemPerPage", 10);
  }

  @Nested
  @DisplayName("prepareCheckout Tests")
  class PrepareCheckoutTests {

    @Test
    @DisplayName("Should return emptyCart for empty cart")
    void shouldReturnEmptyCartForEmptyCart() {
      // Given
      Cart emptyCart = TestDataFactory.createEmptyCart();
      when(cartService.getCart(USER_ID)).thenReturn(emptyCart);

      // When
      PrepareCheckoutResult result = checkoutService.prepareCheckout(USER_ID);

      // Then
      assertThat(result.success()).isFalse();
      assertThat(result.message()).contains("Cart is empty");
    }

    @Test
    @DisplayName("Should return existing checkout if active one exists")
    void shouldReturnExistingCheckout() {
      // Given
      Cart cart = TestDataFactory.createCartWithItems(USER_ID, 2);
      Checkout existingCheckout = TestDataFactory.createCheckoutWithItems(USER_ID, 2);

      when(cartService.getCart(USER_ID)).thenReturn(cart);
      when(checkoutRepository.findByUserIdAndStatus(USER_ID, "WAITING"))
          .thenReturn(Optional.of(existingCheckout));

      // When
      PrepareCheckoutResult result = checkoutService.prepareCheckout(USER_ID);

      // Then
      assertThat(result.success()).isTrue();
      assertThat(result.message()).contains("Existing checkout found");
      assertThat(result.checkout()).isEqualTo(existingCheckout);
    }

    @Test
    @DisplayName("Should create new checkout with successful locks")
    void shouldCreateNewCheckoutWithSuccessfulLocks() {
      // Given
      Cart cart = TestDataFactory.createCartWithItems(USER_ID, 2);
      
      BulkLockStockResponse lockResponse = BulkLockStockResponse.newBuilder()
          .setCheckoutId("new-checkout-id")
          .setAllSuccess(true)
          .setSuccessCount(2)
          .setFailureCount(0)
          .addResults(StockOperationResult.newBuilder()
              .setSubSku(cart.getItems().get(0).getSubSku())
              .setSuccess(true)
              .setCurrentStock(100)
              .build())
          .addResults(StockOperationResult.newBuilder()
              .setSubSku(cart.getItems().get(1).getSubSku())
              .setSuccess(true)
              .setCurrentStock(50)
              .build())
          .build();

      when(cartService.getCart(USER_ID)).thenReturn(cart);
      when(checkoutRepository.findByUserIdAndStatus(USER_ID, "WAITING"))
          .thenReturn(Optional.empty());
      when(systemParameterService.getInt(anyString(), anyInt())).thenReturn(900);
      when(catalogClient.bulkLockStock(anyString(), anyList(), anyInt())).thenReturn(lockResponse);
      when(checkoutRepository.save(any(Checkout.class))).thenAnswer(i -> i.getArgument(0));

      // When
      PrepareCheckoutResult result = checkoutService.prepareCheckout(USER_ID);

      // Then
      assertThat(result.success()).isTrue();
      assertThat(result.message()).contains("successfully");
      verify(checkoutRepository).save(any(Checkout.class));
    }

    @Test
    @DisplayName("Should return noItemsLocked when all locks fail")
    void shouldReturnNoItemsLockedWhenAllFail() {
      // Given
      Cart cart = TestDataFactory.createCartWithItems(USER_ID, 2);
      
      BulkLockStockResponse lockResponse = BulkLockStockResponse.newBuilder()
          .setCheckoutId("new-checkout-id")
          .setAllSuccess(false)
          .setSuccessCount(0)
          .setFailureCount(2)
          .addResults(StockOperationResult.newBuilder()
              .setSubSku(cart.getItems().get(0).getSubSku())
              .setSuccess(false)
              .setMessage("Out of stock")
              .build())
          .addResults(StockOperationResult.newBuilder()
              .setSubSku(cart.getItems().get(1).getSubSku())
              .setSuccess(false)
              .setMessage("Out of stock")
              .build())
          .build();

      when(cartService.getCart(USER_ID)).thenReturn(cart);
      when(checkoutRepository.findByUserIdAndStatus(USER_ID, "WAITING"))
          .thenReturn(Optional.empty());
      when(systemParameterService.getInt(anyString(), anyInt())).thenReturn(900);
      when(catalogClient.bulkLockStock(anyString(), anyList(), anyInt())).thenReturn(lockResponse);

      // When
      PrepareCheckoutResult result = checkoutService.prepareCheckout(USER_ID);

      // Then
      assertThat(result.success()).isFalse();
      assertThat(result.message()).contains("No items could be locked");
    }
  }

  @Nested
  @DisplayName("getCheckout Tests")
  class GetCheckoutTests {

    @Test
    @DisplayName("Should return checkout when user owns it")
    void shouldReturnCheckoutWhenUserOwnsIt() {
      // Given
      Checkout checkout = TestDataFactory.createCheckoutWithItems(USER_ID, 2);
      checkout.setCheckoutId(CHECKOUT_ID);

      when(checkoutRepository.findByCheckoutId(CHECKOUT_ID)).thenReturn(Optional.of(checkout));

      // When
      Checkout result = checkoutService.getCheckout(CHECKOUT_ID, USER_ID);

      // Then
      assertThat(result).isEqualTo(checkout);
    }

    @Test
    @DisplayName("Should throw AuthorizationException when user doesn't own checkout")
    void shouldThrowExceptionWhenUserDoesntOwnCheckout() {
      // Given
      Checkout checkout = TestDataFactory.createCheckoutWithItems(USER_ID, 2);
      checkout.setCheckoutId(CHECKOUT_ID);

      when(checkoutRepository.findByCheckoutId(CHECKOUT_ID)).thenReturn(Optional.of(checkout));

      // When/Then
      assertThatThrownBy(() -> checkoutService.getCheckout(CHECKOUT_ID, OTHER_USER_ID))
          .isInstanceOf(AuthorizationException.class)
          .hasMessageContaining("not authorized");
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when checkout not found")
    void shouldThrowExceptionWhenCheckoutNotFound() {
      // Given
      when(checkoutRepository.findByCheckoutId(CHECKOUT_ID)).thenReturn(Optional.empty());

      // When/Then
      assertThatThrownBy(() -> checkoutService.getCheckout(CHECKOUT_ID, USER_ID))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Checkout with ID " + CHECKOUT_ID + " not found");
    }
  }

  @Nested
  @DisplayName("finalizeCheckout Tests")
  class FinalizeCheckoutTests {

    @Test
    @DisplayName("Should finalize checkout successfully with new address")
    void shouldFinalizeSuccessfullyWithNewAddress() {
      // Given
      Checkout checkout = TestDataFactory.createCheckoutWithItems(USER_ID, 2);
      checkout.setCheckoutId(CHECKOUT_ID);
      AddressSnapshot newAddress = TestDataFactory.createAddressSnapshot();

      when(checkoutRepository.findByCheckoutId(CHECKOUT_ID)).thenReturn(Optional.of(checkout));
      when(systemParameterService.getString(anyString(), anyString())).thenReturn("ORD");
      when(checkoutRepository.save(any(Checkout.class))).thenAnswer(i -> i.getArgument(0));

      // When
      FinalizeCheckoutResult result = checkoutService.finalizeCheckout(
          CHECKOUT_ID, USER_ID, null, newAddress);

      // Then
      assertThat(result.success()).isTrue();
      assertThat(result.orderId()).isNotBlank();
      assertThat(result.paymentCode()).isNotBlank();
      assertThat(checkout.getShippingAddress()).isEqualTo(newAddress);
    }

    @Test
    @DisplayName("Should finalize checkout successfully with existing address")
    void shouldFinalizeSuccessfullyWithExistingAddress() {
      // Given
      Checkout checkout = TestDataFactory.createCheckoutWithItems(USER_ID, 2);
      checkout.setCheckoutId(CHECKOUT_ID);
      String addressId = "address-123";
      AddressSnapshot address = TestDataFactory.createAddressSnapshot();

      when(checkoutRepository.findByCheckoutId(CHECKOUT_ID)).thenReturn(Optional.of(checkout));
      when(memberClient.getAddressById(addressId)).thenReturn(Optional.of(address));
      when(systemParameterService.getString(anyString(), anyString())).thenReturn("ORD");
      when(checkoutRepository.save(any(Checkout.class))).thenAnswer(i -> i.getArgument(0));

      // When
      FinalizeCheckoutResult result = checkoutService.finalizeCheckout(
          CHECKOUT_ID, USER_ID, addressId, null);

      // Then
      assertThat(result.success()).isTrue();
      assertThat(checkout.getShippingAddress()).isEqualTo(address);
    }

    @Test
    @DisplayName("Should return addressRequired when no address provided")
    void shouldReturnAddressRequired() {
      // Given
      Checkout checkout = TestDataFactory.createCheckoutWithItems(USER_ID, 2);
      checkout.setCheckoutId(CHECKOUT_ID);

      when(checkoutRepository.findByCheckoutId(CHECKOUT_ID)).thenReturn(Optional.of(checkout));

      // When
      FinalizeCheckoutResult result = checkoutService.finalizeCheckout(
          CHECKOUT_ID, USER_ID, null, null);

      // Then
      assertThat(result.success()).isFalse();
      assertThat(result.message()).contains("Address is required");
    }

    @Test
    @DisplayName("Should return invalidStatus when checkout is not WAITING")
    void shouldReturnInvalidStatus() {
      // Given
      Checkout checkout = TestDataFactory.createPaidCheckout(USER_ID);
      checkout.setCheckoutId(CHECKOUT_ID);

      when(checkoutRepository.findByCheckoutId(CHECKOUT_ID)).thenReturn(Optional.of(checkout));

      // When
      FinalizeCheckoutResult result = checkoutService.finalizeCheckout(
          CHECKOUT_ID, USER_ID, null, TestDataFactory.createAddressSnapshot());

      // Then
      assertThat(result.success()).isFalse();
      assertThat(result.message()).contains("not in WAITING status");
    }

    @Test
    @DisplayName("Should return expired when checkout has expired")
    void shouldReturnExpired() {
      // Given
      Checkout checkout = TestDataFactory.createExpiredCheckout(USER_ID);
      checkout.setCheckoutId(CHECKOUT_ID);

      when(checkoutRepository.findByCheckoutId(CHECKOUT_ID)).thenReturn(Optional.of(checkout));

      // When
      FinalizeCheckoutResult result = checkoutService.finalizeCheckout(
          CHECKOUT_ID, USER_ID, null, TestDataFactory.createAddressSnapshot());

      // Then
      assertThat(result.success()).isFalse();
      assertThat(result.message()).contains("expired");
    }

    @Test
    @DisplayName("Should return alreadyFinalized when checkout already has orderId")
    void shouldReturnAlreadyFinalized() {
      // Given
      Checkout checkout = TestDataFactory.createFinalizedCheckout(USER_ID);
      checkout.setCheckoutId(CHECKOUT_ID);

      when(checkoutRepository.findByCheckoutId(CHECKOUT_ID)).thenReturn(Optional.of(checkout));

      // When
      FinalizeCheckoutResult result = checkoutService.finalizeCheckout(
          CHECKOUT_ID, USER_ID, null, TestDataFactory.createAddressSnapshot());

      // Then
      assertThat(result.success()).isFalse();
      assertThat(result.message()).contains("already finalized");
    }

    @Test
    @DisplayName("Should throw AuthorizationException when user doesn't own checkout")
    void shouldThrowAuthorizationException() {
      // Given
      Checkout checkout = TestDataFactory.createCheckoutWithItems(USER_ID, 2);
      checkout.setCheckoutId(CHECKOUT_ID);

      when(checkoutRepository.findByCheckoutId(CHECKOUT_ID)).thenReturn(Optional.of(checkout));

      // When/Then
      assertThatThrownBy(() -> checkoutService.finalizeCheckout(
          CHECKOUT_ID, OTHER_USER_ID, null, TestDataFactory.createAddressSnapshot()))
          .isInstanceOf(AuthorizationException.class);
    }
  }

  @Nested
  @DisplayName("payCheckout Tests")
  class PayCheckoutTests {

    @Test
    @DisplayName("Should pay checkout successfully")
    void shouldPayCheckoutSuccessfully() {
      // Given
      Checkout checkout = TestDataFactory.createFinalizedCheckout(USER_ID);
      checkout.setCheckoutId(CHECKOUT_ID);

      BulkAcquireStockResponse acquireResponse = BulkAcquireStockResponse.newBuilder()
          .setCheckoutId(CHECKOUT_ID)
          .setAllSuccess(true)
          .setSuccessCount(2)
          .build();

      when(checkoutRepository.findByCheckoutId(CHECKOUT_ID)).thenReturn(Optional.of(checkout));
      when(catalogClient.bulkAcquireStock(anyString(), anyList())).thenReturn(acquireResponse);
      when(checkoutRepository.save(any(Checkout.class))).thenAnswer(i -> i.getArgument(0));

      // When
      PayCheckoutResult result = checkoutService.payCheckout(CHECKOUT_ID, USER_ID);

      // Then
      assertThat(result.success()).isTrue();
      assertThat(checkout.getStatus()).isEqualTo("PAID");
      assertThat(checkout.getPaidAt()).isNotNull();
    }

    @Test
    @DisplayName("Should return notFinalized when checkout has no orderId")
    void shouldReturnNotFinalized() {
      // Given
      Checkout checkout = TestDataFactory.createCheckoutWithItems(USER_ID, 2);
      checkout.setCheckoutId(CHECKOUT_ID);

      when(checkoutRepository.findByCheckoutId(CHECKOUT_ID)).thenReturn(Optional.of(checkout));

      // When
      PayCheckoutResult result = checkoutService.payCheckout(CHECKOUT_ID, USER_ID);

      // Then
      assertThat(result.success()).isFalse();
      assertThat(result.message()).contains("finalized before payment");
    }

    @Test
    @DisplayName("Should return alreadyPaid when checkout is already paid")
    void shouldReturnAlreadyPaid() {
      // Given
      Checkout checkout = TestDataFactory.createPaidCheckout(USER_ID);
      checkout.setCheckoutId(CHECKOUT_ID);

      when(checkoutRepository.findByCheckoutId(CHECKOUT_ID)).thenReturn(Optional.of(checkout));

      // When
      PayCheckoutResult result = checkoutService.payCheckout(CHECKOUT_ID, USER_ID);

      // Then
      assertThat(result.success()).isFalse();
      assertThat(result.message()).contains("already paid");
    }

    @Test
    @DisplayName("Should return inventoryAcquireFailed when acquire fails")
    void shouldReturnInventoryAcquireFailed() {
      // Given
      Checkout checkout = TestDataFactory.createFinalizedCheckout(USER_ID);
      checkout.setCheckoutId(CHECKOUT_ID);

      BulkAcquireStockResponse acquireResponse = BulkAcquireStockResponse.newBuilder()
          .setCheckoutId(CHECKOUT_ID)
          .setAllSuccess(false)
          .setFailureCount(1)
          .build();

      when(checkoutRepository.findByCheckoutId(CHECKOUT_ID)).thenReturn(Optional.of(checkout));
      when(catalogClient.bulkAcquireStock(anyString(), anyList())).thenReturn(acquireResponse);

      // When
      PayCheckoutResult result = checkoutService.payCheckout(CHECKOUT_ID, USER_ID);

      // Then
      assertThat(result.success()).isFalse();
      assertThat(result.message()).contains("Failed to acquire inventory");
    }

    @Test
    @DisplayName("Should throw AuthorizationException when user doesn't own checkout")
    void shouldThrowAuthorizationException() {
      // Given
      Checkout checkout = TestDataFactory.createFinalizedCheckout(USER_ID);
      checkout.setCheckoutId(CHECKOUT_ID);

      when(checkoutRepository.findByCheckoutId(CHECKOUT_ID)).thenReturn(Optional.of(checkout));

      // When/Then
      assertThatThrownBy(() -> checkoutService.payCheckout(CHECKOUT_ID, OTHER_USER_ID))
          .isInstanceOf(AuthorizationException.class);
    }
  }

  @Nested
  @DisplayName("cancelCheckout Tests")
  class CancelCheckoutTests {

    @Test
    @DisplayName("Should cancel checkout successfully")
    void shouldCancelCheckoutSuccessfully() {
      // Given
      Checkout checkout = TestDataFactory.createCheckoutWithItems(USER_ID, 2);
      checkout.setCheckoutId(CHECKOUT_ID);

      BulkReleaseStockResponse releaseResponse = BulkReleaseStockResponse.newBuilder()
          .setCheckoutId(CHECKOUT_ID)
          .setAllSuccess(true)
          .build();

      when(checkoutRepository.findByCheckoutId(CHECKOUT_ID)).thenReturn(Optional.of(checkout));
      when(catalogClient.bulkReleaseStock(anyString(), anyList())).thenReturn(releaseResponse);
      when(checkoutRepository.save(any(Checkout.class))).thenAnswer(i -> i.getArgument(0));

      // When
      boolean result = checkoutService.cancelCheckout(CHECKOUT_ID, USER_ID, "User cancelled");

      // Then
      assertThat(result).isTrue();
      assertThat(checkout.getStatus()).isEqualTo("CANCELLED");
      assertThat(checkout.getCancelledAt()).isNotNull();
    }

    @Test
    @DisplayName("Should return true for already cancelled checkout")
    void shouldReturnTrueForAlreadyCancelledCheckout() {
      // Given
      Checkout checkout = TestDataFactory.createCancelledCheckout(USER_ID);
      checkout.setCheckoutId(CHECKOUT_ID);

      when(checkoutRepository.findByCheckoutId(CHECKOUT_ID)).thenReturn(Optional.of(checkout));

      // When
      boolean result = checkoutService.cancelCheckout(CHECKOUT_ID, USER_ID, "Reason");

      // Then
      assertThat(result).isTrue();
      verify(catalogClient, never()).bulkReleaseStock(anyString(), anyList());
    }

    @Test
    @DisplayName("Should throw ValidationException for paid checkout")
    void shouldThrowExceptionForPaidCheckout() {
      // Given
      Checkout checkout = TestDataFactory.createPaidCheckout(USER_ID);
      checkout.setCheckoutId(CHECKOUT_ID);

      when(checkoutRepository.findByCheckoutId(CHECKOUT_ID)).thenReturn(Optional.of(checkout));

      // When/Then
      assertThatThrownBy(() -> checkoutService.cancelCheckout(CHECKOUT_ID, USER_ID, "Reason"))
          .isInstanceOf(ValidationException.class)
          .hasMessageContaining("Cannot cancel a paid checkout");
    }

    @Test
    @DisplayName("Should throw AuthorizationException when user doesn't own checkout")
    void shouldThrowAuthorizationException() {
      // Given
      Checkout checkout = TestDataFactory.createCheckoutWithItems(USER_ID, 2);
      checkout.setCheckoutId(CHECKOUT_ID);

      when(checkoutRepository.findByCheckoutId(CHECKOUT_ID)).thenReturn(Optional.of(checkout));

      // When/Then
      assertThatThrownBy(() -> checkoutService.cancelCheckout(CHECKOUT_ID, OTHER_USER_ID, "Reason"))
          .isInstanceOf(AuthorizationException.class);
    }
  }

  @Nested
  @DisplayName("getActiveCheckoutByUser Tests")
  class GetActiveCheckoutByUserTests {

    @Test
    @DisplayName("Should return active checkout")
    void shouldReturnActiveCheckout() {
      // Given
      Checkout checkout = TestDataFactory.createCheckoutWithItems(USER_ID, 2);

      when(checkoutRepository.findByUserIdAndStatus(USER_ID, "WAITING"))
          .thenReturn(Optional.of(checkout));

      // When
      Checkout result = checkoutService.getActiveCheckoutByUser(USER_ID);

      // Then
      assertThat(result).isEqualTo(checkout);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when no active checkout")
    void shouldThrowExceptionWhenNoActiveCheckout() {
      // Given
      when(checkoutRepository.findByUserIdAndStatus(USER_ID, "WAITING"))
          .thenReturn(Optional.empty());

      // When/Then
      assertThatThrownBy(() -> checkoutService.getActiveCheckoutByUser(USER_ID))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("No active checkout found");
    }
  }
}


