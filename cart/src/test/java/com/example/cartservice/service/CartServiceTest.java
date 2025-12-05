package com.example.cartservice.service;

import com.example.cartservice.client.ProductClient;
import com.example.cartservice.dto.AddToCartRequest;
import com.example.cartservice.dto.CartDTO;
import com.example.cartservice.dto.ProductDTO;
import com.example.cartservice.entity.Cart;
import com.example.cartservice.entity.CartItem;
import com.example.cartservice.repository.CartRepository;
import feign.FeignException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CartServiceTest {

    @Mock
    private CartRepository repository;

    @Mock
    private ProductClient productClient;

    @InjectMocks
    private CartService cartService;

    @Test
    void getCart_shouldReturnEnrichedCart() {
        Long userId = 1L;
        CartItem item = new CartItem("p1", 2);
        Cart cart = new Cart(userId, new ArrayList<>());
        cart.getItems().add(item);

        ProductDTO product = new ProductDTO("p1", "Product 1", "Description", BigDecimal.valueOf(99.99));

        when(repository.findById(userId)).thenReturn(Optional.of(cart));
        when(productClient.getProduct("p1")).thenReturn(product);

        CartDTO result = cartService.getCart(userId);

        assertEquals(userId, result.getUserId());
        assertEquals(1, result.getItems().size());
        assertEquals("p1", result.getItems().get(0).getProductId());
        assertEquals(2, result.getItems().get(0).getQuantity());
        assertEquals("Product 1", result.getItems().get(0).getProductName());
        assertEquals(BigDecimal.valueOf(99.99), result.getItems().get(0).getPrice());
    }

    @Test
    void getCart_shouldHandleProductNotFound() {
        Long userId = 1L;
        CartItem item = new CartItem("p1", 2);
        Cart cart = new Cart(userId, new ArrayList<>());
        cart.getItems().add(item);

        when(repository.findById(userId)).thenReturn(Optional.of(cart));
        when(productClient.getProduct("p1")).thenThrow(FeignException.NotFound.class);

        CartDTO result = cartService.getCart(userId);

        assertEquals(1, result.getItems().size());
        assertEquals("Product Not Available", result.getItems().get(0).getProductName());
    }

    @Test
    void addToCart_shouldAddNewItem() {
        Long userId = 1L;
        Cart cart = new Cart(userId, new ArrayList<>());
        when(repository.findById(userId)).thenReturn(Optional.of(cart));
        when(repository.save(any(Cart.class))).thenAnswer(i -> i.getArguments()[0]);

        AddToCartRequest request = new AddToCartRequest("p1", 1);
        Cart result = cartService.addToCart(userId, request);

        assertEquals(1, result.getItems().size());
        assertEquals("p1", result.getItems().get(0).getProductId());
        assertEquals(1, result.getItems().get(0).getQuantity());
    }

    @Test
    void addToCart_shouldIncrementQuantity_whenItemExists() {
        Long userId = 1L;
        CartItem existingItem = new CartItem("p1", 1);
        Cart cart = new Cart(userId, new ArrayList<>());
        cart.getItems().add(existingItem);

        when(repository.findById(userId)).thenReturn(Optional.of(cart));
        when(repository.save(any(Cart.class))).thenAnswer(i -> i.getArguments()[0]);

        AddToCartRequest request = new AddToCartRequest("p1", 2);
        Cart result = cartService.addToCart(userId, request);

        assertEquals(1, result.getItems().size());
        assertEquals(3, result.getItems().get(0).getQuantity());
    }

    @Test
    void removeFromCart_shouldRemoveItem() {
        Long userId = 1L;
        CartItem existingItem = new CartItem("p1", 1);
        Cart cart = new Cart(userId, new ArrayList<>());
        cart.getItems().add(existingItem);

        when(repository.findById(userId)).thenReturn(Optional.of(cart));
        when(repository.save(any(Cart.class))).thenAnswer(i -> i.getArguments()[0]);

        Cart result = cartService.removeFromCart(userId, "p1");

        assertEquals(0, result.getItems().size());
    }
}
