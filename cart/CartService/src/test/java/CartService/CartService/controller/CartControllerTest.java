package CartService.CartService.controller;

import CartService.CartService.common.ApiResponse;
import CartService.CartService.dto.CartRequestDto;
import CartService.CartService.dto.CartResponseDto;
import CartService.CartService.service.CartService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.junit.jupiter.api.Assertions.*;

class CartControllerTest {

    @Mock
    private CartService cartService;

    @InjectMocks
    private CartController cartController;

    private CartRequestDto cartRequestDto;
    private CartResponseDto cartResponseDto;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        cartRequestDto = new CartRequestDto();
        cartResponseDto = new CartResponseDto();
        cartResponseDto.setUserId("101");
    }


    @Test
    void testAddToCart() {
        when(cartService.addToCart(any(), any())).thenReturn(cartResponseDto);

        ApiResponse<CartResponseDto> response =
                cartController.addToCart("101", cartRequestDto);

        assertNotNull(response);
        assertEquals("101", response.getData().getUserId());
        assertTrue(response.getSuccess());
    }


    @Test
    void testViewCart() {
        when(cartService.viewCart("101")).thenReturn(cartResponseDto);

        ApiResponse<CartResponseDto> response =
                cartController.viewCart("101");

        assertNotNull(response);
        assertEquals("101", response.getData().getUserId());
        assertTrue(response.getSuccess());
    }


    @Test
    void testRemoveItem() {
        when(cartService.removeItem("101", "P001"))
                .thenReturn(cartResponseDto);

        ApiResponse<CartResponseDto> response =
                cartController.removeItem("101", "P001");

        assertNotNull(response);
        assertEquals("101", response.getData().getUserId());
    }


    @Test
    void testClearCart() {
        when(cartService.clearCart("101")).thenReturn(cartResponseDto);

        ApiResponse<CartResponseDto> response =
                cartController.clearCart("101");

        assertNotNull(response);
        assertEquals("101", response.getData().getUserId());
    }
}

