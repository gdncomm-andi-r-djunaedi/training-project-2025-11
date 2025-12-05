package com.elfrida.cart.service;

import com.elfrida.cart.model.Cart;
import com.elfrida.cart.model.CartItem;
import com.elfrida.cart.repository.CartRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @InjectMocks
    private CartServiceImpl cartService;

    @Test
    void addToCart_shouldCreateNewCart_whenNoExistingCart() {
        when(cartRepository.findByMemberId("user@example.com"))
                .thenReturn(Optional.empty());

        Cart saved = new Cart();
        saved.setMemberId("user@example.com");
        saved.setItems(List.of());
        saved.setCreatedAt(Instant.now());
        saved.setUpdatedAt(Instant.now());
        when(cartRepository.save(any(Cart.class))).thenReturn(saved);

        Cart result = cartService.addToCart("user@example.com", "P001", 2);

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);
        verify(cartRepository).save(cartCaptor.capture());

        Cart toSave = cartCaptor.getValue();
        assertThat(toSave.getMemberId()).isEqualTo("user@example.com");
        assertThat(toSave.getItems()).hasSize(1);
        assertThat(toSave.getItems().getFirst().getProductId()).isEqualTo("P001");
        assertThat(toSave.getItems().getFirst().getQuantity()).isEqualTo(2);

        assertThat(result.getMemberId()).isEqualTo("user@example.com");
    }

    @Test
    void addToCart_shouldIncreaseQuantity_whenItemAlreadyExists() {
        CartItem item = new CartItem();
        item.setProductId("P001");
        item.setQuantity(1);
        item.setTotalPrice(BigDecimal.ZERO);

        Cart existing = new Cart();
        existing.setMemberId("user@example.com");
        existing.setItems(List.of(item));
        existing.setCreatedAt(Instant.now());

        when(cartRepository.findByMemberId("user@example.com"))
                .thenReturn(Optional.of(existing));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.addToCart("user@example.com", "P001", 2);

        Cart updated = result;
        assertThat(updated.getItems()).hasSize(1);
        CartItem updatedItem = updated.getItems().getFirst();
        assertThat(updatedItem.getQuantity()).isEqualTo(3);
    }

    @Test
    void addToCart_shouldThrow_whenQuantityInvalid() {
        assertThatThrownBy(() -> cartService.addToCart("user@example.com", "P001", 0))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.BAD_REQUEST);
    }

    @Test
    void getCart_shouldReturnExistingCart() {
        Cart existing = new Cart();
        existing.setMemberId("user@example.com");
        when(cartRepository.findByMemberId("user@example.com"))
                .thenReturn(Optional.of(existing));

        Cart result = cartService.getCart("user@example.com");

        assertThat(result).isSameAs(existing);
    }

    @Test
    void getCart_shouldCreateEmptyCart_whenNotFound() {
        when(cartRepository.findByMemberId("user@example.com"))
                .thenReturn(Optional.empty());

        Cart result = cartService.getCart("user@example.com");

        assertThat(result.getMemberId()).isEqualTo("user@example.com");
        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void removeItem_shouldRemoveProduct_whenExists() {
        CartItem item = new CartItem();
        item.setProductId("P001");
        item.setQuantity(1);
        item.setTotalPrice(BigDecimal.ZERO);

        Cart existing = new Cart();
        existing.setMemberId("user@example.com");
        existing.setItems(new java.util.ArrayList<>(List.of(item)));

        when(cartRepository.findByMemberId("user@example.com"))
                .thenReturn(Optional.of(existing));
        when(cartRepository.save(any(Cart.class))).thenAnswer(invocation -> invocation.getArgument(0));

        Cart result = cartService.removeItem("user@example.com", "P001");

        assertThat(result.getItems()).isEmpty();
    }

    @Test
    void removeItem_shouldThrow_whenCartNotFound() {
        when(cartRepository.findByMemberId("user@example.com"))
                .thenReturn(Optional.empty());

        assertThatThrownBy(() -> cartService.removeItem("user@example.com", "P999"))
                .isInstanceOf(ResponseStatusException.class)
                .extracting("statusCode")
                .isEqualTo(HttpStatus.NOT_FOUND);
    }
}


