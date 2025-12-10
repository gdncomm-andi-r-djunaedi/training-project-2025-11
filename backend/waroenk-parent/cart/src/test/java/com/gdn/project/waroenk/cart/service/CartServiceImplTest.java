package com.gdn.project.waroenk.cart.service;

import com.gdn.project.waroenk.cart.client.CatalogGrpcClient;
import com.gdn.project.waroenk.cart.dto.cart.AddCartItemResult;
import com.gdn.project.waroenk.cart.dto.cart.BulkAddCartItemsResult;
import com.gdn.project.waroenk.cart.entity.Cart;
import com.gdn.project.waroenk.cart.exceptions.ResourceNotFoundException;
import com.gdn.project.waroenk.cart.fixture.TestDataFactory;
import com.gdn.project.waroenk.cart.repository.CartRepository;
import com.gdn.project.waroenk.cart.utility.CacheUtil;
import com.gdn.project.waroenk.catalog.VariantData;
import com.google.protobuf.Struct;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;
import java.util.Optional;

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
@DisplayName("CartServiceImpl Unit Tests")
class CartServiceImplTest {

  @Mock
  private CartRepository cartRepository;

  @Mock
  private CacheUtil<Cart> cartCacheUtil;

  @Mock
  private CacheUtil<String> stringCacheUtil;

  @Mock
  private MongoTemplate mongoTemplate;

  @Mock
  private CatalogGrpcClient catalogClient;

  private CartServiceImpl cartService;

  private static final String USER_ID = "test-user-123";
  private static final String SKU = "SKU-001";
  private static final String SUB_SKU = "SKU-001-VAR-001";

  @BeforeEach
  void setUp() {
    cartService = new CartServiceImpl(
        cartRepository,
        cartCacheUtil,
        stringCacheUtil,
        mongoTemplate,
        catalogClient
    );
    ReflectionTestUtils.setField(cartService, "defaultItemPerPage", 10);
  }

  @Nested
  @DisplayName("getCart Tests")
  class GetCartTests {

    @Test
    @DisplayName("Should return cached cart when available")
    void shouldReturnCachedCart() {
      // Given
      Cart cachedCart = TestDataFactory.createCartForUser(USER_ID);
      String cacheKey = "cart:user:" + USER_ID;

      when(cartCacheUtil.getValue(cacheKey)).thenReturn(cachedCart);

      // When
      Cart result = cartService.getCart(USER_ID);

      // Then
      assertThat(result).isEqualTo(cachedCart);
      verify(cartRepository, never()).findByUserId(anyString());
    }

    @Test
    @DisplayName("Should fetch from database when not in cache")
    void shouldFetchFromDatabase() {
      // Given
      Cart cart = TestDataFactory.createCartForUser(USER_ID);
      String cacheKey = "cart:user:" + USER_ID;

      when(cartCacheUtil.getValue(cacheKey)).thenReturn(null);
      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

      // When
      Cart result = cartService.getCart(USER_ID);

      // Then
      assertThat(result).isEqualTo(cart);
      verify(cartCacheUtil).putValue(eq(cacheKey), eq(cart), anyLong(), any());
    }

    @Test
    @DisplayName("Should create new cart when not found")
    void shouldCreateNewCartWhenNotFound() {
      // Given
      String cacheKey = "cart:user:" + USER_ID;
      Cart newCart = TestDataFactory.createCartForUser(USER_ID);

      when(cartCacheUtil.getValue(cacheKey)).thenReturn(null);
      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.empty());
      when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

      // When
      Cart result = cartService.getCart(USER_ID);

      // Then
      assertThat(result.getUserId()).isEqualTo(USER_ID);
      verify(cartRepository).save(any(Cart.class));
    }
  }

  @Nested
  @DisplayName("addItemWithValidation Tests")
  class AddItemWithValidationTests {

    @Test
    @DisplayName("Should add item successfully when stock is sufficient")
    void shouldAddItemSuccessfully() {
      // Given
      Cart cart = TestDataFactory.createCartForUser(USER_ID);
      VariantData variantData = createMockVariantData(SKU, SUB_SKU, "Test Product", 50000.0);
      CatalogGrpcClient.VariantWithStock variantWithStock = 
          new CatalogGrpcClient.VariantWithStock(variantData, 100);

      when(cartCacheUtil.getValue(anyString())).thenReturn(null);
      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
      when(catalogClient.getVariantWithStock(SUB_SKU)).thenReturn(Optional.of(variantWithStock));
      when(cartRepository.save(any(Cart.class))).thenReturn(cart);

      // When
      AddCartItemResult result = cartService.addItemWithValidation(USER_ID, SKU, SUB_SKU, 2);

      // Then
      assertThat(result.success()).isTrue();
      assertThat(result.availableStock()).isEqualTo(100);
      verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should return productNotFound when variant doesn't exist")
    void shouldReturnProductNotFound() {
      // Given
      Cart cart = TestDataFactory.createCartForUser(USER_ID);

      when(cartCacheUtil.getValue(anyString())).thenReturn(null);
      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
      when(catalogClient.getVariantWithStock(SUB_SKU)).thenReturn(Optional.empty());

      // When
      AddCartItemResult result = cartService.addItemWithValidation(USER_ID, SKU, SUB_SKU, 2);

      // Then
      assertThat(result.success()).isFalse();
      assertThat(result.message()).contains("Product not found");
    }

    @Test
    @DisplayName("Should return outOfStock when stock is 0")
    void shouldReturnOutOfStock() {
      // Given
      Cart cart = TestDataFactory.createCartForUser(USER_ID);
      VariantData variantData = createMockVariantData(SKU, SUB_SKU, "Test Product", 50000.0);
      CatalogGrpcClient.VariantWithStock variantWithStock = 
          new CatalogGrpcClient.VariantWithStock(variantData, 0);

      when(cartCacheUtil.getValue(anyString())).thenReturn(null);
      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
      when(catalogClient.getVariantWithStock(SUB_SKU)).thenReturn(Optional.of(variantWithStock));

      // When
      AddCartItemResult result = cartService.addItemWithValidation(USER_ID, SKU, SUB_SKU, 2);

      // Then
      assertThat(result.success()).isFalse();
      assertThat(result.message()).contains("out of stock");
    }

    @Test
    @DisplayName("Should return insufficientStock when requested quantity exceeds available")
    void shouldReturnInsufficientStock() {
      // Given
      Cart cart = TestDataFactory.createCartForUser(USER_ID);
      VariantData variantData = createMockVariantData(SKU, SUB_SKU, "Test Product", 50000.0);
      CatalogGrpcClient.VariantWithStock variantWithStock = 
          new CatalogGrpcClient.VariantWithStock(variantData, 5);

      when(cartCacheUtil.getValue(anyString())).thenReturn(null);
      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
      when(catalogClient.getVariantWithStock(SUB_SKU)).thenReturn(Optional.of(variantWithStock));

      // When
      AddCartItemResult result = cartService.addItemWithValidation(USER_ID, SKU, SUB_SKU, 10);

      // Then
      assertThat(result.success()).isFalse();
      assertThat(result.message()).contains("Insufficient stock");
      assertThat(result.availableStock()).isEqualTo(5);
    }

    @Test
    @DisplayName("Should account for existing quantity in cart")
    void shouldAccountForExistingQuantity() {
      // Given
      Cart cart = TestDataFactory.createCartForUser(USER_ID);
      cart.addOrUpdateItem(TestDataFactory.createCartItem(SKU, SUB_SKU, 3, 50000L));
      
      VariantData variantData = createMockVariantData(SKU, SUB_SKU, "Test Product", 50000.0);
      CatalogGrpcClient.VariantWithStock variantWithStock = 
          new CatalogGrpcClient.VariantWithStock(variantData, 5);

      when(cartCacheUtil.getValue(anyString())).thenReturn(null);
      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
      when(catalogClient.getVariantWithStock(SUB_SKU)).thenReturn(Optional.of(variantWithStock));

      // When - Request 3 more, but existing 3 + 3 = 6 > 5 available
      AddCartItemResult result = cartService.addItemWithValidation(USER_ID, SKU, SUB_SKU, 3);

      // Then
      assertThat(result.success()).isFalse();
      assertThat(result.message()).contains("Insufficient stock");
    }
  }

  @Nested
  @DisplayName("bulkAddItemsWithValidation Tests")
  class BulkAddItemsWithValidationTests {

    @Test
    @DisplayName("Should add multiple items successfully")
    void shouldAddMultipleItemsSuccessfully() {
      // Given
      Cart cart = TestDataFactory.createCartForUser(USER_ID);
      String sku1 = "SKU-001", subSku1 = "SKU-001-VAR-001";
      String sku2 = "SKU-002", subSku2 = "SKU-002-VAR-001";

      VariantData variant1 = createMockVariantData(sku1, subSku1, "Product 1", 50000.0);
      VariantData variant2 = createMockVariantData(sku2, subSku2, "Product 2", 30000.0);

      when(cartCacheUtil.getValue(anyString())).thenReturn(null);
      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
      when(catalogClient.getVariantWithStock(subSku1))
          .thenReturn(Optional.of(new CatalogGrpcClient.VariantWithStock(variant1, 100)));
      when(catalogClient.getVariantWithStock(subSku2))
          .thenReturn(Optional.of(new CatalogGrpcClient.VariantWithStock(variant2, 50)));
      when(cartRepository.save(any(Cart.class))).thenReturn(cart);

      List<CartService.CartItemInput> items = List.of(
          new CartService.CartItemInput(sku1, subSku1, 2),
          new CartService.CartItemInput(sku2, subSku2, 3)
      );

      // When
      BulkAddCartItemsResult result = cartService.bulkAddItemsWithValidation(USER_ID, items);

      // Then
      assertThat(result.allSuccess()).isTrue();
      assertThat(result.itemStatuses()).hasSize(2);
      assertThat(result.itemStatuses().stream().filter(BulkAddCartItemsResult.CartItemStatus::success).count()).isEqualTo(2);
    }

    @Test
    @DisplayName("Should handle partial failures")
    void shouldHandlePartialFailures() {
      // Given
      Cart cart = TestDataFactory.createCartForUser(USER_ID);
      String sku1 = "SKU-001", subSku1 = "SKU-001-VAR-001";
      String sku2 = "SKU-002", subSku2 = "SKU-002-VAR-001";

      VariantData variant1 = createMockVariantData(sku1, subSku1, "Product 1", 50000.0);

      when(cartCacheUtil.getValue(anyString())).thenReturn(null);
      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
      when(catalogClient.getVariantWithStock(subSku1))
          .thenReturn(Optional.of(new CatalogGrpcClient.VariantWithStock(variant1, 100)));
      when(catalogClient.getVariantWithStock(subSku2)).thenReturn(Optional.empty()); // Not found
      when(cartRepository.save(any(Cart.class))).thenReturn(cart);

      List<CartService.CartItemInput> items = List.of(
          new CartService.CartItemInput(sku1, subSku1, 2),
          new CartService.CartItemInput(sku2, subSku2, 3)
      );

      // When
      BulkAddCartItemsResult result = cartService.bulkAddItemsWithValidation(USER_ID, items);

      // Then
      assertThat(result.itemStatuses()).hasSize(2);
      assertThat(result.itemStatuses().stream().filter(s -> s.success()).count()).isEqualTo(1);
      assertThat(result.itemStatuses().stream().filter(s -> !s.success()).count()).isEqualTo(1);
    }
  }

  @Nested
  @DisplayName("updateItemQuantityWithValidation Tests")
  class UpdateItemQuantityWithValidationTests {

    @Test
    @DisplayName("Should update quantity successfully")
    void shouldUpdateQuantitySuccessfully() {
      // Given
      Cart cart = TestDataFactory.createCartForUser(USER_ID);
      cart.addOrUpdateItem(TestDataFactory.createCartItem(SKU, SUB_SKU, 2, 50000L));

      VariantData variantData = createMockVariantData(SKU, SUB_SKU, "Test Product", 50000.0);
      CatalogGrpcClient.VariantWithStock variantWithStock = 
          new CatalogGrpcClient.VariantWithStock(variantData, 100);

      when(cartCacheUtil.getValue(anyString())).thenReturn(null);
      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
      when(catalogClient.getVariantWithStock(SUB_SKU)).thenReturn(Optional.of(variantWithStock));
      when(cartRepository.save(any(Cart.class))).thenReturn(cart);

      // When
      AddCartItemResult result = cartService.updateItemQuantityWithValidation(USER_ID, SKU, SUB_SKU, 5);

      // Then
      assertThat(result.success()).isTrue();
      verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should remove item when quantity is 0")
    void shouldRemoveItemWhenQuantityZero() {
      // Given
      Cart cart = TestDataFactory.createCartForUser(USER_ID);
      cart.addOrUpdateItem(TestDataFactory.createCartItem(SKU, SUB_SKU, 2, 50000L));

      when(cartCacheUtil.getValue(anyString())).thenReturn(null);
      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
      when(cartRepository.save(any(Cart.class))).thenReturn(cart);

      // When
      AddCartItemResult result = cartService.updateItemQuantityWithValidation(USER_ID, SKU, SUB_SKU, 0);

      // Then
      assertThat(result.success()).isTrue();
      assertThat(cart.getItems()).isEmpty();
    }
  }

  @Nested
  @DisplayName("removeItem Tests")
  class RemoveItemTests {

    @Test
    @DisplayName("Should remove item successfully")
    void shouldRemoveItemSuccessfully() {
      // Given
      Cart cart = TestDataFactory.createCartForUser(USER_ID);
      cart.addOrUpdateItem(TestDataFactory.createCartItem(SKU, SUB_SKU, 2, 50000L));

      when(cartCacheUtil.getValue(anyString())).thenReturn(null);
      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
      when(cartRepository.save(any(Cart.class))).thenReturn(cart);

      // When
      Cart result = cartService.removeItem(USER_ID, SKU);

      // Then
      assertThat(result.getItems()).isEmpty();
      verify(cartRepository).save(any(Cart.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when item not found")
    void shouldThrowExceptionWhenItemNotFound() {
      // Given
      Cart cart = TestDataFactory.createCartForUser(USER_ID);

      when(cartCacheUtil.getValue(anyString())).thenReturn(null);
      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));

      // When/Then
      assertThatThrownBy(() -> cartService.removeItem(USER_ID, "NON-EXISTENT"))
          .isInstanceOf(ResourceNotFoundException.class)
          .hasMessageContaining("Item with SKU NON-EXISTENT not found in cart");
    }
  }

  @Nested
  @DisplayName("bulkRemoveItems Tests")
  class BulkRemoveItemsTests {

    @Test
    @DisplayName("Should remove multiple items")
    void shouldRemoveMultipleItems() {
      // Given
      Cart cart = TestDataFactory.createCartForUser(USER_ID);
      cart.addOrUpdateItem(TestDataFactory.createCartItem("SKU-001", "SKU-001-VAR-001", 2, 50000L));
      cart.addOrUpdateItem(TestDataFactory.createCartItem("SKU-002", "SKU-002-VAR-001", 3, 30000L));
      cart.addOrUpdateItem(TestDataFactory.createCartItem("SKU-003", "SKU-003-VAR-001", 1, 20000L));

      when(cartCacheUtil.getValue(anyString())).thenReturn(null);
      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
      when(cartRepository.save(any(Cart.class))).thenReturn(cart);

      // When
      Cart result = cartService.bulkRemoveItems(USER_ID, List.of("SKU-001", "SKU-002"));

      // Then
      assertThat(result.getItems()).hasSize(1);
      assertThat(result.getItems().get(0).getSku()).isEqualTo("SKU-003");
    }
  }

  @Nested
  @DisplayName("clearCart Tests")
  class ClearCartTests {

    @Test
    @DisplayName("Should clear all items from cart")
    void shouldClearAllItems() {
      // Given
      Cart cart = TestDataFactory.createCartWithItems(USER_ID, 3);

      when(cartCacheUtil.getValue(anyString())).thenReturn(null);
      when(cartRepository.findByUserId(USER_ID)).thenReturn(Optional.of(cart));
      when(cartRepository.save(any(Cart.class))).thenReturn(cart);

      // When
      boolean result = cartService.clearCart(USER_ID);

      // Then
      assertThat(result).isTrue();
      assertThat(cart.getItems()).isEmpty();
      verify(cartCacheUtil).removeValue("cart:user:" + USER_ID);
    }
  }

  // ============================================================
  // Helper Methods
  // ============================================================

  private VariantData createMockVariantData(String sku, String subSku, String title, double price) {
    return VariantData.newBuilder()
        .setSku(sku)
        .setSubSku(subSku)
        .setTitle(title)
        .setPrice(price)
        .setThumbnail("https://example.com/images/" + sku + ".jpg")
        .setAttributes(Struct.getDefaultInstance())
        .build();
  }
}


