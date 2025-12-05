package com.cart.service.impl;

import com.cart.dto.request.AddItemRequest;
import com.cart.dto.request.UpdateItemQuantityRequest;
import com.cart.entity.Cart;
import com.cart.entity.CartItem;
import com.cart.repository.CartItemRepository;
import com.cart.repository.CartRepository;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.test.util.ReflectionTestUtils;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private CartItemRepository cartItemRepository;

    @Mock
    private EntityManager entityManager;

    @InjectMocks
    private CartServiceImpl cartService;

    private UUID customerId;
    private UUID productId;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);

        // inject mock entityManager into @PersistenceContext
        ReflectionTestUtils.setField(cartService, "entityManager", entityManager);

        customerId = UUID.randomUUID();
        productId = UUID.randomUUID();
    }

    // ---------------------------------------------------------------------
    // getOrCreateCart
    // ---------------------------------------------------------------------
    @Test
    void testGetOrCreateCart_WhenCartExists() {
        Cart cart = new Cart();
        cart.setId(UUID.randomUUID());
        cart.setCustomerId(customerId);

        when(cartRepository.findByCustomerId(customerId))
                .thenReturn(cart);

        Cart result = cartService.getOrCreateCart(customerId);

        assertNotNull(result);
        assertEquals(customerId, result.getCustomerId());
        verify(cartRepository, never()).save(any());
    }

    @Test
    void testGetOrCreateCart_WhenCartDoesNotExist() {
        when(cartRepository.findByCustomerId(customerId))
                .thenReturn(null);

        Cart savedCart = new Cart();
        savedCart.setId(UUID.randomUUID());
        savedCart.setCustomerId(customerId);

        when(cartRepository.save(any()))
                .thenReturn(savedCart);

        Cart result = cartService.getOrCreateCart(customerId);

        assertNotNull(result);
        assertEquals(customerId, result.getCustomerId());
        verify(cartRepository, times(1)).save(any(Cart.class));
    }

    // ---------------------------------------------------------------------
    // addItem()
    // ---------------------------------------------------------------------
    @Test
    void testAddItem_NewItem() {
        Cart cart = new Cart();
        cart.setId(UUID.randomUUID());
        cart.setCustomerId(customerId);

        AddItemRequest request = new AddItemRequest();
        request.setProductId(productId);
        request.setQuantity(2);
        request.setPriceEach(BigDecimal.valueOf(10000));
        request.setProductName("Test Product");

        when(cartRepository.findByCustomerId(customerId)).thenReturn(cart);
        when(cartItemRepository.findByCartIdAndProductId(cart.getId(), productId)).thenReturn(null);

        CartItem savedItem = new CartItem();
        savedItem.setId(UUID.randomUUID());
        savedItem.setProductId(productId);
        savedItem.setPriceEach(request.getPriceEach());
        savedItem.setQuantity(2);

        when(cartItemRepository.save(any())).thenReturn(savedItem);
        when(cartItemRepository.findByCartId(cart.getId()))
                .thenReturn(List.of(savedItem));

        CartItem result = cartService.addItem(customerId, request);

        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        assertEquals(0, result.getPriceEach().compareTo(BigDecimal.valueOf(10000)));

        verify(entityManager, times(1)).refresh(savedItem);
    }

    // ---------------------------------------------------------------------
    // updateItemQuantity
    // ---------------------------------------------------------------------
    @Test
    void testUpdateItemQuantity_Success() {
        Cart cart = new Cart();
        cart.setId(UUID.randomUUID());
        cart.setCustomerId(customerId);

        CartItem item = new CartItem();
        item.setId(UUID.randomUUID());
        item.setCart(cart);
        item.setProductId(productId);
        item.setQuantity(1);
        item.setPriceEach(BigDecimal.valueOf(5000));

        UpdateItemQuantityRequest request = new UpdateItemQuantityRequest();
        request.setProductId(productId);
        request.setQuantity(5);

        when(cartRepository.findByCustomerId(customerId)).thenReturn(cart);
        when(cartItemRepository.findByCartIdAndProductId(cart.getId(), productId))
                .thenReturn(item);
        when(cartItemRepository.save(any())).thenReturn(item);
        when(cartItemRepository.findByCartId(cart.getId()))
                .thenReturn(List.of(item));

        CartItem result = cartService.updateItemQuantity(customerId, request);

        assertNotNull(result);
        assertEquals(5, result.getQuantity());

        verify(entityManager, times(1)).refresh(item);
    }

    @Test
    void testUpdateItemQuantity_ItemNotFound() {
        Cart cart = new Cart();
        cart.setId(UUID.randomUUID());
        cart.setCustomerId(customerId);

        UpdateItemQuantityRequest request = new UpdateItemQuantityRequest();
        request.setProductId(productId);
        request.setQuantity(5);

        when(cartRepository.findByCustomerId(customerId)).thenReturn(cart);
        when(cartItemRepository.findByCartIdAndProductId(cart.getId(), productId))
                .thenReturn(null);

        assertThrows(RuntimeException.class,
                () -> cartService.updateItemQuantity(customerId, request));
    }

    // ---------------------------------------------------------------------
    // removeItem
    // ---------------------------------------------------------------------
    @Test
    void testRemoveItem() {
        Cart cart = new Cart();
        cart.setId(UUID.randomUUID());
        cart.setCustomerId(customerId);

        CartItem item = new CartItem();
        item.setId(UUID.randomUUID());
        item.setCart(cart);
        item.setProductId(productId);
        item.setQuantity(1);
        item.setPriceEach(BigDecimal.valueOf(10000));

        when(cartRepository.findByCustomerId(customerId)).thenReturn(cart);
        when(cartItemRepository.findByCartIdAndProductId(cart.getId(), productId))
                .thenReturn(item);
        when(cartItemRepository.findByCartId(cart.getId()))
                .thenReturn(List.of());

        Cart result = cartService.removeItem(customerId, productId);

        assertNotNull(result);
        verify(cartItemRepository, times(1)).delete(item);
    }

    // ---------------------------------------------------------------------
    // clearCart
    // ---------------------------------------------------------------------
    @Test
    void testClearCart() {
        Cart cart = new Cart();
        cart.setId(UUID.randomUUID());
        cart.setCustomerId(customerId);

        when(cartRepository.findByCustomerId(customerId))
                .thenReturn(cart);

        cartService.clearCart(customerId);

        verify(cartItemRepository).deleteAllByCartId(cart.getId());
    }
}
