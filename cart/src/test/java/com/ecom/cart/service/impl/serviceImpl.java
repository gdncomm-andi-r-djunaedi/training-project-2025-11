package com.ecom.cart.service.impl;

import com.ecom.cart.Dto.ApiResponse;
import com.ecom.cart.Dto.CartDto;
import com.ecom.cart.Dto.ProductDto;
import com.ecom.cart.Entity.Cart;
import com.ecom.cart.Entity.CartItem;
import com.ecom.cart.Repository.CartRepo;
import com.ecom.cart.Service.Impl.CartServiceImpl;
import com.ecom.cart.client.ProductClient;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;

import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;
import java.util.Optional;

import static org.bson.assertions.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.when;

class CartServiceImplTest {

    @InjectMocks
    CartServiceImpl cartService;

    @Mock
    CartRepo cartRepo;

    @Mock
    ProductClient productClient;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);
    }

    @Test
    void testGetCartByUserId() {
        Cart cart = new Cart();
        cart.setUserId("23fj34");
        cart.setItems(new ArrayList<>());
        when(cartRepo.findByUserId("23fj34")).thenReturn(Optional.of(cart));

        CartDto result = cartService.getCartByUserId("23fj34");
        assertEquals("23fj34", result.getUserId());
    }

    @Test
    void testDeleteSkuFromCart() {
        CartItem cartItem = new CartItem();
        List<CartItem> i = new ArrayList<>();
        cartItem.setPrice(34.00);
        cartItem.setQuantity(1);
        cartItem.setProductSku("SKU-1003");
        cartItem.setName("beras");
        cartItem.setImage("beras.jpeg");
        i.add(cartItem);

        Cart cart = new Cart();
        cart.setUserId("23fj34");
        cart.setItems(i);
        when(cartRepo.findByUserId("23fj34")).thenReturn(Optional.of(cart));
        when(cartRepo.save(cart)).thenReturn(cart);

        Boolean isSuccess = cartService.deleteFromCartBySku(cartItem.getProductSku(), cart.getUserId());
        assertTrue(isSuccess);
    }

    @Test
    void testAddSkuToCart(){
        Cart cart = new Cart();
        cart.setId("23fj34");
        cart.setItems(new ArrayList<>());

        ProductDto productDto = new ProductDto();
        productDto.setProductSku("SKU-1002");
        productDto.setName("Beras");
        productDto.setPrice(34.00);

        when(productClient.getProductBySku("SKU-1002")).thenReturn(ApiResponse.success(200, productDto));
        Boolean isSuccess = cartService.addSkuToCart(productDto.getProductSku(), cart.getUserId());
        assertTrue(isSuccess);
    }
}