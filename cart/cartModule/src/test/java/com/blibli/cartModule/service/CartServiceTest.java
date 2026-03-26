package com.blibli.cartModule.service;

import com.blibli.cartModule.client.ProductServiceClient;
import com.blibli.cartModule.dto.AddItemRequestDto;
import com.blibli.cartModule.dto.CartResponseDto;
import com.blibli.cartModule.dto.RemoveItemDto;
import com.blibli.cartModule.entity.Cart;
import com.blibli.cartModule.repository.CartRepository;
import com.blibli.cartModule.service.impl.CartImpl;
import com.blibli.productModule.dto.ApiResponse;
import com.blibli.productModule.dto.ProductDto;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.ResponseEntity;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private CartImpl cartService;

    private Long memberId;
    private AddItemRequestDto addItemRequestDto;
    private RemoveItemDto removeItemDto;
    private Cart cart;
    private ProductDto productDto;

    @BeforeEach
    void setUp() {
        memberId = 1L;

        addItemRequestDto = new AddItemRequestDto();
        addItemRequestDto.setProductId("PROD123");
        addItemRequestDto.setQuantity(2);

        removeItemDto = new RemoveItemDto();
        removeItemDto.setProductId("PROD123");
        removeItemDto.setQuantity(1);

        cart = new Cart();
        cart.setId("CART123");
        cart.setMemberId(memberId);
        cart.setItems(new ArrayList<>());
        cart.setCreatedAt(new Date());
        cart.setUpdatedAt(new Date());

        productDto = new ProductDto();
        productDto.setProductId("PROD123");
        productDto.setName("Test Product");
        productDto.setImageUrl("http://example.com/image.jpg");
        productDto.setPrice(new BigDecimal("100.00"));
    }

    @Test
    void testAddItem_NewCart() {
        Cart newCart = new Cart();
        newCart.setId("CART123");
        newCart.setMemberId(memberId);
        newCart.setItems(new ArrayList<>());
        newCart.setCreatedAt(new Date());
        newCart.setUpdatedAt(new Date());
        Cart.CartItem item = new Cart.CartItem();
        item.setProductId("PROD123");
        item.setQuantity(2);
        newCart.getItems().add(item);

        when(cartRepository.findByMemberId(memberId)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenReturn(newCart);

        ApiResponse<ProductDto> apiResponse = ApiResponse.success(productDto);
        when(productServiceClient.getProductById("PROD123")).thenReturn(ResponseEntity.ok(apiResponse));

        CartResponseDto result = cartService.addItem(memberId, addItemRequestDto);

        assertNotNull(result);
        assertEquals(memberId, result.getMemberId());
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());
        assertEquals("PROD123", result.getItems().get(0).getProductId());
        assertEquals(2, result.getItems().get(0).getQuantity());

        verify(cartRepository, times(1)).findByMemberId(memberId);
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testAddItem_ExistingCart_NewItem() {
        when(cartRepository.findByMemberId(memberId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        ApiResponse<ProductDto> apiResponse = ApiResponse.success(productDto);
        when(productServiceClient.getProductById("PROD123")).thenReturn(ResponseEntity.ok(apiResponse));

        CartResponseDto result = cartService.addItem(memberId, addItemRequestDto);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testAddItem_ExistingCart_UpdateQuantity() {
        Cart.CartItem existingItem = new Cart.CartItem();
        existingItem.setProductId("PROD123");
        existingItem.setQuantity(1);
        cart.getItems().add(existingItem);

        when(cartRepository.findByMemberId(memberId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        ApiResponse<ProductDto> apiResponse = ApiResponse.success(productDto);
        when(productServiceClient.getProductById("PROD123")).thenReturn(ResponseEntity.ok(apiResponse));

        CartResponseDto result = cartService.addItem(memberId, addItemRequestDto);

        assertNotNull(result);
        assertEquals(1, result.getItems().size());
        assertEquals(3, cart.getItems().get(0).getQuantity());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testRemoveItem_RemovePartialQuantity() {
        Cart.CartItem item = new Cart.CartItem();
        item.setProductId("PROD123");
        item.setQuantity(5);
        cart.getItems().add(item);

        removeItemDto.setQuantity(2);

        when(cartRepository.findByMemberId(memberId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        ApiResponse<ProductDto> apiResponse = ApiResponse.success(productDto);
        when(productServiceClient.getProductById("PROD123")).thenReturn(ResponseEntity.ok(apiResponse));

        CartResponseDto result = cartService.removeItem(memberId, removeItemDto);

        assertNotNull(result);
        assertEquals(1, cart.getItems().size());
        assertEquals(3, cart.getItems().get(0).getQuantity());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testRemoveItem_RemoveFullQuantity() {
        Cart.CartItem item = new Cart.CartItem();
        item.setProductId("PROD123");
        item.setQuantity(2);
        cart.getItems().add(item);

        removeItemDto.setQuantity(2);

        when(cartRepository.findByMemberId(memberId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponseDto result = cartService.removeItem(memberId, removeItemDto);

        assertNotNull(result);
        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testRemoveItem_RemoveMoreThanAvailable() {
        Cart.CartItem item = new Cart.CartItem();
        item.setProductId("PROD123");
        item.setQuantity(2);
        cart.getItems().add(item);

        removeItemDto.setQuantity(5); // Remove more than available

        when(cartRepository.findByMemberId(memberId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponseDto result = cartService.removeItem(memberId, removeItemDto);

        assertNotNull(result);
        assertTrue(cart.getItems().isEmpty()); // Item should be removed when quantity <= 0
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testRemoveItem_CartNotFound() {
        when(cartRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            cartService.removeItem(memberId, removeItemDto);
        });

        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void testRemoveItem_ProductNotFound() {
        when(cartRepository.findByMemberId(memberId)).thenReturn(Optional.of(cart));

        assertThrows(RuntimeException.class, () -> {
            cartService.removeItem(memberId, removeItemDto);
        });

        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void testGetCart_Success() {
        Cart.CartItem item = new Cart.CartItem();
        item.setProductId("PROD123");
        item.setQuantity(2);
        cart.getItems().add(item);

        when(cartRepository.findByMemberId(memberId)).thenReturn(Optional.of(cart));

        ApiResponse<ProductDto> apiResponse = ApiResponse.success(productDto);
        when(productServiceClient.getProductById("PROD123")).thenReturn(ResponseEntity.ok(apiResponse));

        CartResponseDto result = cartService.getCart(memberId);

        assertNotNull(result);
        assertEquals(memberId, result.getMemberId());
        assertNotNull(result.getItems());
        assertEquals(1, result.getItems().size());
        assertNotNull(result.getTotalPrice());
    }

    @Test
    void testGetCart_CartNotFound() {
        when(cartRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            cartService.getCart(memberId);
        });
    }

    @Test
    void testClearCart_Success() {
        Cart.CartItem item = new Cart.CartItem();
        item.setProductId("PROD123");
        item.setQuantity(2);
        cart.getItems().add(item);

        when(cartRepository.findByMemberId(memberId)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        cartService.clearCart(memberId);

        assertTrue(cart.getItems().isEmpty());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    @Test
    void testClearCart_CartNotFound() {
        when(cartRepository.findByMemberId(memberId)).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            cartService.clearCart(memberId);
        });

        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void testAddItem_ProductFetchFailure() {
        when(productServiceClient.getProductById("PROD123")).thenThrow(
                new RuntimeException("Product service unavailable"));
        assertThrows(RuntimeException.class, () -> {
            cartService.addItem(memberId, addItemRequestDto);
        });
    }
}