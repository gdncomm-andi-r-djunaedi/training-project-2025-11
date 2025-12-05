package com.blibli.training.cart.service.impl;

import com.blibli.training.cart.client.MemberClient;
import com.blibli.training.cart.client.ProductClient;
import com.blibli.training.cart.dto.CartResponse;
import com.blibli.training.cart.entity.Cart;
import com.blibli.training.cart.entity.CartItem;
import com.blibli.training.cart.repository.CartRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private MemberClient memberClient;

    @Mock
    private ProductClient productClient;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private CartServiceImpl cartService;

    private String username;
    private String productName;
    private int quantity;
    private double price;
    private Cart cart;

    @BeforeEach
    void setUp() {
        username = "testUser";
        productName = "testProduct";
        quantity = 2;
        price = 100.0;

        cart = Cart.builder()
                .id(1L)
                .username(username)
                .items(new ArrayList<>())
                .build();
    }

    @Test
    void addToBag_Success_NewCart_NewItem() {
        // Mock MemberClient
        when(memberClient.findByUsername(username)).thenReturn(new Object());

        // Mock ProductClient and ObjectMapper
        Object productObj = new Object();
        when(productClient.findByName(productName)).thenReturn(productObj);
        Map<String, Object> productMap = new HashMap<>();
        productMap.put("price", price);
        when(objectMapper.convertValue(productObj, Map.class)).thenReturn(productMap);

        // Mock CartRepository
        when(cartRepository.findByUsername(username)).thenReturn(Optional.empty());
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> {
            Cart savedCart = invocation.getArgument(0);
            savedCart.setId(1L);
            return savedCart;
        });

        CartResponse response = cartService.addToBag(username, productName, quantity);

        assertNotNull(response);
        assertEquals(username, response.getUsername());
        assertEquals(1, response.getItems().size());
        assertEquals(productName, response.getItems().get(0).getProductName());
        assertEquals(quantity, response.getItems().get(0).getQuantity());
        assertEquals(price, response.getItems().get(0).getPrice());

        verify(memberClient).findByUsername(username);
        verify(productClient).findByName(productName);
        verify(cartRepository).findByUsername(username);
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void addToBag_Success_ExistingCart_NewItem() {
        // Mock MemberClient
        when(memberClient.findByUsername(username)).thenReturn(new Object());

        // Mock ProductClient and ObjectMapper
        Object productObj = new Object();
        when(productClient.findByName(productName)).thenReturn(productObj);
        Map<String, Object> productMap = new HashMap<>();
        productMap.put("price", price);
        when(objectMapper.convertValue(productObj, Map.class)).thenReturn(productMap);

        // Mock CartRepository
        when(cartRepository.findByUsername(username)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponse response = cartService.addToBag(username, productName, quantity);

        assertNotNull(response);
        assertEquals(1, response.getItems().size());
        assertEquals(productName, response.getItems().get(0).getProductName());
        assertEquals(quantity, response.getItems().get(0).getQuantity());
    }

    @Test
    void addToBag_Success_ExistingCart_ExistingItem() {
        // Add item to cart
        CartItem existingItem = CartItem.builder()
                .productName(productName)
                .quantity(1)
                .price(price)
                .cart(cart)
                .build();
        cart.getItems().add(existingItem);

        // Mock MemberClient
        when(memberClient.findByUsername(username)).thenReturn(new Object());

        // Mock ProductClient and ObjectMapper
        Object productObj = new Object();
        when(productClient.findByName(productName)).thenReturn(productObj);
        Map<String, Object> productMap = new HashMap<>();
        productMap.put("price", 200.0); // Price changed
        when(objectMapper.convertValue(productObj, Map.class)).thenReturn(productMap);

        // Mock CartRepository
        when(cartRepository.findByUsername(username)).thenReturn(Optional.of(cart));
        when(cartRepository.save(any(Cart.class))).thenReturn(cart);

        CartResponse response = cartService.addToBag(username, productName, quantity);

        assertNotNull(response);
        assertEquals(1, response.getItems().size());
        assertEquals(3, response.getItems().get(0).getQuantity()); // 1 + 2
        assertEquals(200.0, response.getItems().get(0).getPrice()); // Updated price
    }

    @Test
    void addToBag_Fail_UserNotFound() {
        when(memberClient.findByUsername(username)).thenThrow(new RuntimeException("User not found"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.addToBag(username, productName, quantity);
        });

        assertEquals("User not found: " + username, exception.getMessage());
        verify(productClient, never()).findByName(anyString());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void addToBag_Fail_ProductNotFound() {
        when(memberClient.findByUsername(username)).thenReturn(new Object());
        when(productClient.findByName(productName)).thenThrow(new RuntimeException("Product not found"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.addToBag(username, productName, quantity);
        });

        assertEquals("Product not found: " + productName, exception.getMessage());
        verify(cartRepository, never()).save(any(Cart.class));
    }

    @Test
    void getAllCart_Success() {
        // Add item to cart
        CartItem existingItem = CartItem.builder()
                .productName(productName)
                .quantity(1)
                .price(price)
                .cart(cart)
                .build();
        cart.getItems().add(existingItem);

        when(memberClient.findByUsername(username)).thenReturn(new Object());
        when(cartRepository.findByUsername(username)).thenReturn(Optional.of(cart));

        CartResponse response = cartService.getAllCart(username);

        assertNotNull(response);
        assertEquals(username, response.getUsername());
        assertEquals(1, response.getItems().size());
    }

    @Test
    void getAllCart_Fail_UserNotFound() {
        when(memberClient.findByUsername(username)).thenThrow(new RuntimeException("User not found"));

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.getAllCart(username);
        });

        assertEquals("User not found: " + username, exception.getMessage());
    }

    @Test
    void getAllCart_Fail_CartNotFound() {
        when(memberClient.findByUsername(username)).thenReturn(new Object());
        when(cartRepository.findByUsername(username)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            cartService.getAllCart(username);
        });

        assertEquals("User " + username + "has no item in cart", exception.getMessage());
    }
}
