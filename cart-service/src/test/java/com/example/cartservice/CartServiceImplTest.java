package com.example.cartservice;

import com.example.cartservice.dto.AddToCartRequest;
import com.example.cartservice.dto.AddToCartResponse;
import com.example.cartservice.dto.GetCartResponse;
import com.example.cartservice.dto.GetProductResponse;
import com.example.cartservice.exception.BusinessException;
import com.example.cartservice.model.Cart;
import com.example.cartservice.repository.CartRepository;
import com.example.cartservice.service.CartServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.springframework.data.domain.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;
import org.springframework.http.HttpStatus;

import java.math.BigDecimal;
import java.time.Instant;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class CartServiceImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private RestTemplate restTemplate;

    @InjectMocks
    private CartServiceImpl cartService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
        cartService.setProductServiceUrl("http://localhost:8080");
    }

    @Test
    void testAddToCartNewItem() {
        String memberId = "member1";
        AddToCartRequest request = new AddToCartRequest("product1", 2);

        when(restTemplate.getForObject(anyString(), eq(GetProductResponse.class)))
                .thenReturn(new GetProductResponse("product1", "Test", "D", "CAT", BigDecimal.TEN));

        when(cartRepository.findByMemberIdAndProductId(memberId, "product1")).thenReturn(Optional.empty());

        Cart savedCart = Cart.builder()
                .id("cart1")
                .memberId(memberId)
                .productId("product1")
                .qty(2)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(cartRepository.save(any(Cart.class))).thenReturn(savedCart);

        AddToCartResponse response = cartService.addToCart(memberId, request);

        assertEquals("cart1", response.id());
        assertEquals(memberId, response.memberId());
        assertEquals("product1", response.productId());
        assertEquals(2, response.qty());

        verify(cartRepository).findByMemberIdAndProductId(memberId, "product1");
        verify(cartRepository).save(any(Cart.class));
    }

    @Test
    void testAddToCartExistingItem() {
        String memberId = "member1";
        AddToCartRequest request = new AddToCartRequest("product1", 5);

        when(restTemplate.getForObject(anyString(), eq(GetProductResponse.class)))
                .thenReturn(new GetProductResponse("product1", "Test", "D", "CAT", BigDecimal.TEN));

        Cart existingCart = Cart.builder()
                .id("cart1")
                .memberId(memberId)
                .productId("product1")
                .qty(2)
                .createdAt(Instant.now())
                .updatedAt(Instant.now())
                .build();

        when(cartRepository.findByMemberIdAndProductId(memberId, "product1"))
                .thenReturn(Optional.of(existingCart));

        when(cartRepository.save(existingCart)).thenReturn(existingCart);

        AddToCartResponse response = cartService.addToCart(memberId, request);

        assertEquals(5, response.qty());
        verify(cartRepository).save(existingCart);
    }

    @Test
    void testAddToCart_ProductNotFound() {
        String memberId = "member1";
        AddToCartRequest request = new AddToCartRequest("productX", 1);

        when(restTemplate.getForObject(anyString(), eq(GetProductResponse.class)))
                .thenThrow(HttpClientErrorException.create(
                        HttpStatus.NOT_FOUND,
                        "Not Found",
                        null,
                        null,
                        null
                ));

        BusinessException ex = assertThrows(
                BusinessException.class,
                () -> cartService.addToCart(memberId, request)
        );

        assertEquals("PRODUCT_NOT_FOUND", ex.getCode());
    }


    @Test
    void testGetCart() {
        String memberId = "member1";

        Cart cart1 = Cart.builder()
                .id("cart1")
                .memberId(memberId)
                .productId("product1")
                .qty(2)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Cart> page = new PageImpl<>(java.util.List.of(cart1), pageable, 1);

        when(cartRepository.findByMemberId(memberId, pageable)).thenReturn(page);

        GetProductResponse productResponse = new GetProductResponse(
                "product1",
                "Laptop",
                "Desc",
                "ELECTRONICS",
                new BigDecimal("100")
        );

        when(restTemplate.getForObject("http://localhost:8080/api/product/product-detail/product1", GetProductResponse.class))
                .thenReturn(productResponse);

        Page<GetCartResponse> result = cartService.getCart(memberId, pageable);

        assertEquals(1, result.getTotalElements());
        GetCartResponse item = result.getContent().get(0);
        assertEquals("Laptop", item.productName());
        assertEquals(new BigDecimal("200"), item.totalPrice());
    }

    @Test
    void testGetCart_ProductServiceReturnsNull() {
        String memberId = "member1";

        Cart cart1 = Cart.builder()
                .id("cart1")
                .memberId(memberId)
                .productId("productX")
                .qty(3)
                .build();

        Pageable pageable = PageRequest.of(0, 10);
        Page<Cart> page = new PageImpl<>(java.util.List.of(cart1), pageable, 1);

        when(cartRepository.findByMemberId(memberId, pageable)).thenReturn(page);

        when(restTemplate.getForObject(anyString(), eq(GetProductResponse.class)))
                .thenReturn(null);

        Page<GetCartResponse> result = cartService.getCart(memberId, pageable);

        GetCartResponse item = result.getContent().get(0);

        assertEquals("Unknown Product", item.productName());
        assertEquals(BigDecimal.ZERO, item.totalPrice());
    }

    @Test
    void testDeleteFromCartSuccess() {
        when(cartRepository.existsById("cart1")).thenReturn(true);
        doNothing().when(cartRepository).deleteById("cart1");

        boolean result = cartService.deleteFromCart("cart1");

        assertTrue(result);
        verify(cartRepository).deleteById("cart1");
    }

    @Test
    void testDeleteFromCartNotFound() {
        when(cartRepository.existsById("cart1")).thenReturn(false);

        boolean result = cartService.deleteFromCart("cart1");

        assertFalse(result);
        verify(cartRepository, never()).deleteById(anyString());
    }
}