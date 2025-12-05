package com.example.cart.service.impl;

import com.example.cart.client.ProductClient;
import com.example.cart.dto.AddToCartRequestDTO;
import com.example.cart.dto.CartResponseDTO;
import com.example.cart.dto.GetBulkProductResponseDTO;
import com.example.cart.entity.Cart;
import com.example.cart.entity.CartItem;
import com.example.cart.exceptions.ResourceNotFoundException;
import com.example.cart.repository.CartRepository;
import com.example.cart.utils.APIResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private CartServiceImpl cartService;

    private String userId;
    private Cart cart;
    private List<GetBulkProductResponseDTO> productDetails;

    @BeforeEach
    void setUp() {
        userId = "user123";
        cart = new Cart(userId);
        productDetails = new ArrayList<>();
    }

    @Test
    void getCart_withValidUserId_returnsCartWithItems() {
        
        CartItem item1 = new CartItem(1L, 2);
        CartItem item2 = new CartItem(2L, 3);
        cart.setItems(Arrays.asList(item1, item2));

        GetBulkProductResponseDTO product1 = GetBulkProductResponseDTO.builder()
                .productId(1L)
                .title("Product 1")
                .price(new BigDecimal("10.00"))
                .imageUrl("image1.jpg")
                .markForDelete(false)
                .build();

        GetBulkProductResponseDTO product2 = GetBulkProductResponseDTO.builder()
                .productId(2L)
                .title("Product 2")
                .price(new BigDecimal("15.00"))
                .imageUrl("image2.jpg")
                .markForDelete(false)
                .build();

        productDetails = Arrays.asList(product1, product2);

        APIResponse<List<GetBulkProductResponseDTO>> apiResponse = new APIResponse<>();
        apiResponse.setData(productDetails);

        when(cartRepository.findById(userId)).thenReturn(Optional.of(cart));
        when(productClient.fetchProductInBulk(anyList())).thenReturn(ResponseEntity.ok(apiResponse));

        CartResponseDTO result = cartService.getCart(userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertEquals(2, result.getItems().size());
        assertEquals(new BigDecimal("65.00"), result.getTotalPrice()); // (10*2) + (15*3) = 65
        verify(cartRepository).findById(userId);
        verify(productClient).fetchProductInBulk(anyList());
    }

    @Test
    void getCart_withEmptyCart_returnsEmptyCartResponse() {
        
        when(cartRepository.findById(userId)).thenReturn(Optional.empty());

        CartResponseDTO result = cartService.getCart(userId);

        assertNotNull(result);
        assertEquals(userId, result.getUserId());
        assertTrue(result.getItems().isEmpty());
        assertEquals(BigDecimal.ZERO, result.getTotalPrice());
        verify(cartRepository).findById(userId);
        verify(productClient, never()).fetchProductInBulk(anyList());
    }

    @Test
    void addToCartOrUpdateQuantity_newProduct_addsProductToCart() {
        
        AddToCartRequestDTO request = new AddToCartRequestDTO(1L, 2);
        Cart emptyCart = new Cart(userId);

        when(cartRepository.findById(userId)).thenReturn(Optional.of(emptyCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(emptyCart);

        String result = cartService.addToCartOrUpdateQuantity(userId, request);

        assertEquals("product added to cart successfully", result);
        assertEquals(1, emptyCart.getItems().size());
        assertEquals(1L, emptyCart.getItems().get(0).getProductId());
        assertEquals(2, emptyCart.getItems().get(0).getQuantity());
        verify(cartRepository).findById(userId);
        verify(cartRepository).save(emptyCart);
    }

    @Test
    void addToCartOrUpdateQuantity_existingProduct_updatesQuantity() {
        
        CartItem existingItem = new CartItem(1L, 2);
        cart.setItems(new ArrayList<>(Collections.singletonList(existingItem)));

        AddToCartRequestDTO request = new AddToCartRequestDTO(1L, 3);

        when(cartRepository.findById(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        String result = cartService.addToCartOrUpdateQuantity(userId, request);

        assertEquals("product added to cart successfully", result);
        assertEquals(1, cart.getItems().size());
        assertEquals(5, cart.getItems().get(0).getQuantity()); // 2 + 3 = 5
        verify(cartRepository).save(cart);
    }

    @Test
    void removeItemFromCart_validProductId_removesItem() {
        
        CartItem item1 = new CartItem(1L, 2);
        CartItem item2 = new CartItem(2L, 3);
        cart.setItems(new ArrayList<>(Arrays.asList(item1, item2)));

        when(cartRepository.findById(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        String result = cartService.removeItemFromCart(userId, 1L);

        assertEquals("product removed from cart successfully", result);
        assertEquals(1, cart.getItems().size());
        assertEquals(2L, cart.getItems().get(0).getProductId());
        verify(cartRepository).save(cart);
    }

    @Test
    void emptyCart_validUserId_clearsAllItems() {
        
        cart.setItems(new ArrayList<>(Arrays.asList(
                new CartItem(1L, 2),
                new CartItem(2L, 3)
        )));

        when(cartRepository.findById(userId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        String result = cartService.emptyCart(userId);

        assertEquals("Cart deleted successfully", result);
        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository).save(cart);
    }

    @Test
    void getCart_withNonExistentProducts_skipsInvalidProducts() {
        
        CartItem item1 = new CartItem(1L, 2);
        CartItem item2 = new CartItem(2L, 3);
        CartItem item3 = new CartItem(3L, 1); // This product won't be in the response
        cart.setItems(Arrays.asList(item1, item2, item3));

        GetBulkProductResponseDTO product1 = GetBulkProductResponseDTO.builder()
                .productId(1L)
                .title("Product 1")
                .price(new BigDecimal("10.00"))
                .imageUrl("image1.jpg")
                .markForDelete(false)
                .build();

        GetBulkProductResponseDTO product2 = GetBulkProductResponseDTO.builder()
                .productId(2L)
                .title("Product 2")
                .price(new BigDecimal("15.00"))
                .imageUrl("image2.jpg")
                .markForDelete(false)
                .build();

        productDetails = Arrays.asList(product1, product2); // Product 3 is missing

        APIResponse<List<GetBulkProductResponseDTO>> apiResponse = new APIResponse<>();
        apiResponse.setData(productDetails);

        when(cartRepository.findById(userId)).thenReturn(Optional.of(cart));
        when(productClient.fetchProductInBulk(anyList())).thenReturn(ResponseEntity.ok(apiResponse));

        CartResponseDTO result = cartService.getCart(userId);

        assertNotNull(result);
        assertEquals(2, result.getItems().size()); // Only 2 products, not 3
        assertEquals(new BigDecimal("65.00"), result.getTotalPrice());
    }

    @Test
    void getCart_withNullPrices_handlesGracefully() {
        
        CartItem item1 = new CartItem(1L, 2);
        cart.setItems(Collections.singletonList(item1));

        GetBulkProductResponseDTO product1 = GetBulkProductResponseDTO.builder()
                .productId(1L)
                .title("Product 1")
                .price(null) // Null price
                .imageUrl("image1.jpg")
                .markForDelete(false)
                .build();

        productDetails = Collections.singletonList(product1);

        APIResponse<List<GetBulkProductResponseDTO>> apiResponse = new APIResponse<>();
        apiResponse.setData(productDetails);

        when(cartRepository.findById(userId)).thenReturn(Optional.of(cart));
        when(productClient.fetchProductInBulk(anyList())).thenReturn(ResponseEntity.ok(apiResponse));

        CartResponseDTO result = cartService.getCart(userId);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(BigDecimal.ZERO, result.getTotalPrice()); // Should handle null price as zero
    }

    @Test
    void fetchProductDetails_withApiFailure_returnsEmptyList() {
        
        CartItem item1 = new CartItem(1L, 2);
        cart.setItems(Collections.singletonList(item1));

        when(cartRepository.findById(userId)).thenReturn(Optional.of(cart));
        when(productClient.fetchProductInBulk(anyList())).thenThrow(new RuntimeException("API Error"));

        CartResponseDTO result = cartService.getCart(userId);

        assertNotNull(result);
        assertTrue(result.getItems().isEmpty()); // Should return empty when API fails
        assertEquals(BigDecimal.ZERO, result.getTotalPrice());
    }

    @Test
    void getCart_withNullItemsList_returnsEmptyCart() {
        
        Cart cartWithNullItems = new Cart(userId);
        cartWithNullItems.setItems(null);

        when(cartRepository.findById(userId)).thenReturn(Optional.of(cartWithNullItems));

        CartResponseDTO result = cartService.getCart(userId);

        assertNotNull(result);
        assertTrue(result.getItems().isEmpty());
        assertEquals(BigDecimal.ZERO, result.getTotalPrice());
    }

    // ==================== EXCEPTION HANDLING ====================

    @Test
    void addToCartOrUpdateQuantity_nullProductId_throwsIllegalArgumentException() {
        
        AddToCartRequestDTO request = new AddToCartRequestDTO(null, 2);
 
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cartService.addToCartOrUpdateQuantity(userId, request)
        );
        assertEquals("Product ID cannot be null", exception.getMessage());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void addToCartOrUpdateQuantity_zeroQuantity_throwsIllegalArgumentException() {
        
        AddToCartRequestDTO request = new AddToCartRequestDTO(1L, 0);
 
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cartService.addToCartOrUpdateQuantity(userId, request)
        );
        assertEquals("Quantity must be greater than 0", exception.getMessage());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void addToCartOrUpdateQuantity_negativeQuantity_throwsIllegalArgumentException() {
        
        AddToCartRequestDTO request = new AddToCartRequestDTO(1L, -5);
 
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cartService.addToCartOrUpdateQuantity(userId, request)
        );
        assertEquals("Quantity must be greater than 0", exception.getMessage());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void removeItemFromCart_nullProductId_throwsIllegalArgumentException() { 
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cartService.removeItemFromCart(userId, null)
        );
        assertEquals("Product ID cannot be null", exception.getMessage());
        verify(cartRepository, never()).findById(any());
    }

    @Test
    void removeItemFromCart_nonExistentCart_throwsResourceNotFoundException() {
        
        when(cartRepository.findById(userId)).thenReturn(Optional.empty());
 
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.removeItemFromCart(userId, 1L)
        );
        assertEquals("Cart not found for user", exception.getMessage());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void removeItemFromCart_emptyCart_throwsResourceNotFoundException() {
        
        cart.setItems(new ArrayList<>());
        when(cartRepository.findById(userId)).thenReturn(Optional.of(cart));
 
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.removeItemFromCart(userId, 1L)
        );
        assertEquals("Cart is empty", exception.getMessage());
    }

    @Test
    void removeItemFromCart_productNotInCart_throwsResourceNotFoundException() {
        
        cart.setItems(new ArrayList<>(Collections.singletonList(new CartItem(1L, 2))));
        when(cartRepository.findById(userId)).thenReturn(Optional.of(cart));
 
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.removeItemFromCart(userId, 999L)
        );
        assertEquals("Product with id 999 not found in cart", exception.getMessage());
    }

    @Test
    void emptyCart_nullUserId_throwsIllegalArgumentException() { 
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cartService.emptyCart(null)
        );
        assertEquals("User ID cannot be null or empty", exception.getMessage());
        verify(cartRepository, never()).findById(any());
    }

    @Test
    void emptyCart_emptyUserId_throwsIllegalArgumentException() { 
        IllegalArgumentException exception = assertThrows(
                IllegalArgumentException.class,
                () -> cartService.emptyCart("   ")
        );
        assertEquals("User ID cannot be null or empty", exception.getMessage());
        verify(cartRepository, never()).findById(any());
    }

    @Test
    void emptyCart_nonExistentCart_throwsResourceNotFoundException() {
        
        when(cartRepository.findById(userId)).thenReturn(Optional.empty());
 
        ResourceNotFoundException exception = assertThrows(
                ResourceNotFoundException.class,
                () -> cartService.emptyCart(userId)
        );
        assertEquals("Cart not found", exception.getMessage());
        verify(cartRepository, never()).save(any());
    }

    @ParameterizedTest
    @ValueSource(ints = {1, 5, 10, 100})
    void addToCartOrUpdateQuantity_variousQuantities_calculatesCorrectly(int quantity) {
        
        AddToCartRequestDTO request = new AddToCartRequestDTO(1L, quantity);
        Cart emptyCart = new Cart(userId);

        when(cartRepository.findById(userId)).thenReturn(Optional.of(emptyCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(emptyCart);

        String result = cartService.addToCartOrUpdateQuantity(userId, request);

        assertEquals("product added to cart successfully", result);
        assertEquals(1, emptyCart.getItems().size());
        assertEquals(quantity, emptyCart.getItems().get(0).getQuantity());
    }

    @ParameterizedTest
    @CsvSource({
            "10.00, 2, 15.00, 3, 65.00",  // (10*2) + (15*3) = 65
            "5.50, 1, 3.25, 2, 12.00",    // (5.50*1) + (3.25*2) = 12
            "100.00, 1, 50.00, 1, 150.00", // (100*1) + (50*1) = 150
            "0.99, 10, 1.99, 5, 19.85"    // (0.99*10) + (1.99*5) = 19.85
    })
    void getCart_multipleProducts_calculatesTotalPriceCorrectly(
            String price1, int qty1, String price2, int qty2, String expectedTotal) {
        
        CartItem item1 = new CartItem(1L, qty1);
        CartItem item2 = new CartItem(2L, qty2);
        cart.setItems(Arrays.asList(item1, item2));

        GetBulkProductResponseDTO product1 = GetBulkProductResponseDTO.builder()
                .productId(1L)
                .title("Product 1")
                .price(new BigDecimal(price1))
                .imageUrl("image1.jpg")
                .markForDelete(false)
                .build();

        GetBulkProductResponseDTO product2 = GetBulkProductResponseDTO.builder()
                .productId(2L)
                .title("Product 2")
                .price(new BigDecimal(price2))
                .imageUrl("image2.jpg")
                .markForDelete(false)
                .build();

        productDetails = Arrays.asList(product1, product2);

        APIResponse<List<GetBulkProductResponseDTO>> apiResponse = new APIResponse<>();
        apiResponse.setData(productDetails);

        when(cartRepository.findById(userId)).thenReturn(Optional.of(cart));
        when(productClient.fetchProductInBulk(anyList())).thenReturn(ResponseEntity.ok(apiResponse));

        CartResponseDTO result = cartService.getCart(userId);

        assertNotNull(result);
        assertEquals(new BigDecimal(expectedTotal), result.getTotalPrice());
    }

    @Test
    void addToCartOrUpdateQuantity_newUserWithNoCart_createsNewCart() {
        
        AddToCartRequestDTO request = new AddToCartRequestDTO(1L, 2);
        Cart newCart = new Cart(userId);

        when(cartRepository.findById(userId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

        String result = cartService.addToCartOrUpdateQuantity(userId, request);

        assertEquals("product added to cart successfully", result);
        verify(cartRepository).save(any(Cart.class));
    }
}
