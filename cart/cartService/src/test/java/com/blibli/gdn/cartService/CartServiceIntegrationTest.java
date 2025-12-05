package com.blibli.gdn.cartService;

import com.blibli.gdn.cartService.client.ProductServiceClient;
import com.blibli.gdn.cartService.client.dto.ProductDTO;
import com.blibli.gdn.cartService.client.dto.VariantDTO;
import com.blibli.gdn.cartService.model.Cart;
import com.blibli.gdn.cartService.repository.CartRepository;
import com.blibli.gdn.cartService.service.CartService;
import com.blibli.gdn.cartService.web.model.AddToCartRequest;
import com.blibli.gdn.cartService.web.model.UpdateQuantityRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MongoDBContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@Testcontainers
class CartServiceIntegrationTest {

    @Container
    static MongoDBContainer mongoDBContainer = new MongoDBContainer("mongo:6.0");

    @DynamicPropertySource
    static void setProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.data.mongodb.uri", mongoDBContainer::getReplicaSetUrl);
    }

    @Autowired
    private CartService cartService;

    @Autowired
    private CartRepository cartRepository;

    @MockBean
    private ProductServiceClient productServiceClient;

    private static final String MEMBER_ID = "test-member";
    private static final String SKU = "SKU-123";

    @BeforeEach
    void setUp() {
        ProductDTO productDTO = ProductDTO.builder()
                .productId("PROD-123")
                .name("Test Product")
                .build();

        VariantDTO variantDTO = VariantDTO.builder()
                .sku(SKU)
                .price(new BigDecimal("100.00"))
                .color("Red")
                .size("M")
                .build();

        when(productServiceClient.getProductBySku(anyString())).thenReturn(productDTO);
        when(productServiceClient.getVariantBySku(any(), anyString())).thenReturn(variantDTO);
    }

    @AfterEach
    void tearDown() {
        cartRepository.deleteAll();
    }

    @Test
    void testCartLifecycle() {
        // 1. Add Item to Cart
        AddToCartRequest addRequest = new AddToCartRequest();
        addRequest.setSku(SKU);
        addRequest.setQty(2);

        Cart cart = cartService.addToCart(MEMBER_ID, addRequest);
        assertNotNull(cart);
        assertEquals(1, cart.getItems().size());
        assertEquals(2, cart.getItems().get(0).getQty());

        // 2. Get Cart
        Cart retrievedCart = cartService.getCart(MEMBER_ID);
        assertEquals(1, retrievedCart.getItems().size());
        assertEquals(SKU, retrievedCart.getItems().get(0).getSku());

        // 3. Update Quantity
        UpdateQuantityRequest updateRequest = new UpdateQuantityRequest();
        updateRequest.setQty(5);
        Cart updatedCart = cartService.updateQuantity(MEMBER_ID, SKU, updateRequest);
        assertEquals(5, updatedCart.getItems().get(0).getQty());

        // 4. Remove Item
        cartService.removeItem(MEMBER_ID, SKU);
        Cart emptyCart = cartService.getCart(MEMBER_ID);
        assertTrue(emptyCart.getItems().isEmpty());
    }
}
