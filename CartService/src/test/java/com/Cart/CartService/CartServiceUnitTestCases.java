package com.Cart.CartService;

import com.Cart.CartService.dto.AddItemRequestDTO;
import com.Cart.CartService.dto.CartResponseDTO;
import com.Cart.CartService.dto.ProductResponseDTO;
import com.Cart.CartService.entity.Cart;
import com.Cart.CartService.entity.CartItem;
import com.Cart.CartService.exception.ProductServiceExceptions;
import com.Cart.CartService.repository.CartRepository;
import com.Cart.CartService.service.impl.CartServiceImpl;
import com.Cart.CartService.service.impl.ProductClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;

import java.util.*;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)

public class CartServiceUnitTestCases {
    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private CartServiceImpl cartService;

    private Cart existingCart;

    @BeforeEach
    void setup() {
        existingCart = new Cart();
        existingCart.setCartId("cart-123");
        existingCart.setMemberId("member1");
        existingCart.setItems(new ArrayList<>());
    }

    // ADD ITEM TESTS

    @Test
    void testAddItem_ProductExists_NewCartCreated() {
        AddItemRequestDTO request = new AddItemRequestDTO("prod1", 2);

        ProductResponseDTO productResponse = new ProductResponseDTO(
                "prod1","pp-001", "Laptop", 5000.0, "Electronics", "Nice laptop"
        );

        when(productClient.getProductById("prod1")).thenReturn(productResponse);
        when(cartRepository.findByMemberId("member1")).thenReturn(null);
        when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CartResponseDTO response = cartService.addItem("member1", request);

        assertNotNull(response);
        assertEquals("member1", response.getMemberId());
        assertEquals(1, response.getItems().size());
        assertEquals("prod1", response.getItems().get(0).getProductId());
        assertEquals(2, response.getItems().get(0).getQuantity());
    }

    @Test
    void testAddItem_ProductNotFound_ThrowsException() {
        AddItemRequestDTO request = new AddItemRequestDTO("invalid", 1);

        when(productClient.getProductById("invalid"))
                .thenThrow(new ProductServiceExceptions("Not found", HttpStatus.NOT_FOUND));

        ProductServiceExceptions ex = assertThrows(
                ProductServiceExceptions.class,
                () -> cartService.addItem("member1", request)
        );

        assertEquals("Not found", ex.getMessage());
    }

    @Test
    void testAddItem_CartExists_UpdateQuantity() {
        CartItem item = new CartItem("item1", "prod1", 1);
        existingCart.getItems().add(item);

        AddItemRequestDTO request = new AddItemRequestDTO("prod1", 3);

        ProductResponseDTO product = new ProductResponseDTO(
                "prod1","prod1-0001", "Phone", 2000.0, "Electronics", "Great phone"
        );

        when(productClient.getProductById("prod1")).thenReturn(product);
        when(cartRepository.findByMemberId("member1")).thenReturn(existingCart);
        when(cartRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CartResponseDTO response = cartService.addItem("member1", request);

        assertEquals(1, response.getItems().size());
        assertEquals(4, response.getItems().get(0).getQuantity());
    }
    
    // DELETE ITEM TESTS


    @Test
    void testDeleteProduct_ItemRemovedSuccessfully() {
        CartItem item = new CartItem("item1", "prod1", 1);
        existingCart.getItems().add(item);

        when(cartRepository.findByMemberId("member1")).thenReturn(existingCart);

        boolean result = cartService.deleteProductFromCart("member1", "item1");

        assertTrue(result);
        assertEquals(0, existingCart.getItems().size());
        verify(cartRepository, times(1)).save(existingCart);
    }

    @Test
    void testDeleteProduct_ItemNotFound_ReturnsFalse() {
        when(cartRepository.findByMemberId("member1")).thenReturn(existingCart);

        boolean result = cartService.deleteProductFromCart("member1", "invalid");

        assertFalse(result);
    }

    @Test
    void testDeleteProduct_CartNotFound_ReturnsFalse() {
        when(cartRepository.findByMemberId("member1")).thenReturn(null);

        boolean result = cartService.deleteProductFromCart("member1", "item1");

        assertFalse(result);
    }

    @Test
    void testDeleteProduct_NullMemberId_ReturnsFalse() {
        boolean result = cartService.deleteProductFromCart(null, "item1");
        assertFalse(result);
    }

    @Test
    void testDeleteProduct_NullItemId_ReturnsFalse() {
        boolean result = cartService.deleteProductFromCart("member1", null);
        assertFalse(result);
    }

 
    // GET CART TESTS
 

    @Test
    void testGetCartByMemberId_CartExists() {
        CartItem item = new CartItem("item1", "prod1", 1);
        existingCart.getItems().add(item);

        ProductResponseDTO product = new ProductResponseDTO(
                "prod1","del-0001", "Laptop", 5000.0, "Electronics", "Good laptop"
        );

        when(cartRepository.findByMemberId("member1")).thenReturn(existingCart);
        when(productClient.getProductById("prod1")).thenReturn(product);

        CartResponseDTO response = cartService.getCartByMemberId("member1");

        assertNotNull(response);
        assertEquals("member1", response.getMemberId());
        assertEquals(1, response.getItems().size());
        assertEquals("Laptop", response.getItems().get(0).getProductName());
    }

    @Test
    void testGetCartByMemberId_NoCart_ReturnsEmpty() {
        when(cartRepository.findByMemberId("member1")).thenReturn(null);

        CartResponseDTO response = cartService.getCartByMemberId("member1");

        assertNotNull(response);
        assertEquals("member1", response.getMemberId());
        assertEquals(0, response.getItems().size());
    }

    @Test
    void testGetCartByMemberId_BlankMemberId_ThrowsException() {
        ProductServiceExceptions ex = assertThrows(
                ProductServiceExceptions.class,
                () -> cartService.getCartByMemberId(" ")
        );

        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatus());
    }
}
