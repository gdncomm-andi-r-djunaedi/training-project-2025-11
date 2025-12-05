package com.blublu.cart.service;

import com.blublu.cart.document.CartDocument;
import com.blublu.cart.interfaces.ProductFeignClient;
import com.blublu.cart.model.request.EditQtyRequest;
import com.blublu.cart.model.response.CartResponse;
import com.blublu.cart.model.response.GenericBodyResponse;
import com.blublu.cart.repository.CartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductFeignClient productFeignClient;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void getUserCart_Success() {
        String username = "testUser";
        CartDocument cartDocument = new CartDocument();
        cartDocument.setUsername(username);
        CartDocument.Item item = new CartDocument.Item();
        item.setSkuCode("SKU123");
        item.setQuantity(2);
        cartDocument.setItems(Collections.singletonList(item));

        when(cartRepository.findByUsername(username)).thenReturn(cartDocument);

        CartResponse.ItemResponse itemResponse = CartResponse.ItemResponse.builder()
                .skuCode("SKU123")
                .name("Test Product")
                .price(BigDecimal.valueOf(100))
                .build();

        GenericBodyResponse<CartResponse.ItemResponse> genericResponse = GenericBodyResponse.<CartResponse.ItemResponse>builder()
                .content(Collections.singletonList(itemResponse))
                .build();
        when(productFeignClient.getProductDetail("SKU123")).thenReturn(genericResponse);

        CartResponse response = cartService.getUserCart(username);

        assertNotNull(response);
        assertEquals(username, response.getUsername());
        assertEquals(1, response.getItems().size());
        assertEquals("SKU123", response.getItems().get(0).getSkuCode());
        assertEquals(2, response.getItems().get(0).getQuantity());
        verify(cartRepository, times(1)).findByUsername(username);
        verify(productFeignClient, times(1)).getProductDetail("SKU123");
    }

    @Test
    void getUserCart_CartNotFound() {
        String username = "testUser";
        when(cartRepository.findByUsername(username)).thenReturn(null);

        CartResponse response = cartService.getUserCart(username);

        assertNull(response);
        verify(cartRepository, times(1)).findByUsername(username);
        verify(productFeignClient, never()).getProductDetail(anyString());
    }

    @Test
    void addItemToCart_Success() {
        String username = "testUser";
        CartDocument.Item item = new CartDocument.Item();
        item.setSkuCode("SKU123");
        item.setQuantity(1);

        CartResponse.ItemResponse itemResponse = CartResponse.ItemResponse.builder()
                .skuCode("SKU123")
                .build();
        GenericBodyResponse<CartResponse.ItemResponse> genericResponse = GenericBodyResponse.<CartResponse.ItemResponse>builder()
                .content(Collections.singletonList(itemResponse))
                .build();

        when(productFeignClient.getProductDetail("SKU123")).thenReturn(genericResponse);
        when(cartRepository.addOrUpdateItem(username, item)).thenReturn(true);

        boolean result = cartService.addItemToCart(username, item);

        assertTrue(result);
        verify(productFeignClient, times(1)).getProductDetail("SKU123");
        verify(cartRepository, times(1)).addOrUpdateItem(username, item);
    }

    @Test
    void addItemToCart_ProductNotFound() {
        String username = "testUser";
        CartDocument.Item item = new CartDocument.Item();
        item.setSkuCode("SKU123");
        item.setQuantity(1);

        GenericBodyResponse<CartResponse.ItemResponse> genericResponse = GenericBodyResponse.<CartResponse.ItemResponse>builder()
                .content(Collections.emptyList())
                .build();

        when(productFeignClient.getProductDetail("SKU123")).thenReturn(genericResponse);

        boolean result = cartService.addItemToCart(username, item);

        assertFalse(result);
        verify(productFeignClient, times(1)).getProductDetail("SKU123");
        verify(cartRepository, never()).addOrUpdateItem(anyString(), any());
    }

    @Test
    void editCartItem_Success() {
        String username = "testUser";
        EditQtyRequest request = EditQtyRequest.builder()
                .skuCode("SKU123")
                .newQty(5)
                .build();

        when(cartRepository.editCartItem(username, request)).thenReturn(true);

        boolean result = cartService.editCartItem(username, request);

        assertTrue(result);
        verify(cartRepository, times(1)).editCartItem(username, request);
    }

    @Test
    void removeItemFromCart_Success() {
        String username = "testUser";
        String skuCode = "SKU123";

        when(cartRepository.removeItemFromCart(username, skuCode)).thenReturn(true);

        boolean result = cartService.removeItemFromCart(username, skuCode);

        assertTrue(result);
        verify(cartRepository, times(1)).removeItemFromCart(username, skuCode);
    }

    @Test
    void clearCart_Success() {
        String username = "testUser";
        when(cartRepository.deleteTopByUsername(username)).thenReturn(1L);

        boolean result = cartService.clearCart(username);

        assertTrue(result);
        verify(cartRepository, times(1)).deleteTopByUsername(username);
    }

    @Test
    void clearCart_Failure() {
        String username = "testUser";
        when(cartRepository.deleteTopByUsername(username)).thenReturn(0L);

        boolean result = cartService.clearCart(username);

        assertFalse(result);
        verify(cartRepository, times(1)).deleteTopByUsername(username);
    }
}
