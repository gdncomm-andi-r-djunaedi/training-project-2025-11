package com.blibli.cartData.services;


import com.blibli.cartData.client.ProductClient;
import com.blibli.cartData.dto.CartItemDTO;
import com.blibli.cartData.dto.CartProductDetailDTO;
import com.blibli.cartData.dto.CartResponseDTO;
import com.blibli.cartData.dto.ProductDTO;
import com.blibli.cartData.entity.Cart;
import com.blibli.cartData.entity.CartItem;
import com.blibli.cartData.repositories.CartRepository;
import com.blibli.cartData.services.impl.CartServiceImpl;
import com.blibli.cartData.util.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.*;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.util.*;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.BDDMockito.*;

class CartServiceImplTest {

    @InjectMocks
    private CartServiceImpl cartService;

    @Mock
    private CartRepository cartRepository;

    @Mock
    private ProductClient productClient;

    @Mock
    private JwtUtil jwtUtil;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.initMocks(this);
    }


    //getAllCartItems()
    @Test
    void getAllCartItems_shouldReturnPagedCartItems() {
        String token = "Bearer abc.def.ghi";
        String memberId = "member1";

        when(jwtUtil.getUserNameFromToken("abc.def.ghi")).thenReturn(memberId);

        List<CartItem> items = Arrays.asList(
                new CartItem("prod1", 2),
                new CartItem("prod2", 3)
        );

        Cart cart = new Cart();
        cart.setMemberId(memberId);
        cart.setItems(items);

        when(cartRepository.findByMemberId(memberId)).thenReturn(Optional.of(cart));

        List<String> categories1 = Arrays.asList("Electronics", "Mobile");
        List<String> categories2 = Arrays.asList("Home", "Appliances");

        ProductDTO prod1 = new ProductDTO();
        prod1.setProductId("prod1");
        prod1.setName("Product 1");
        prod1.setDescription("Desc 1");
        prod1.setBrand("Brand A");
        prod1.setPrice(100.0);
        prod1.setImageUrl("url1");
        prod1.setCategories(categories1);

        ProductDTO prod2 = new ProductDTO();
        prod2.setProductId("prod2");
        prod2.setName("Product 2");
        prod2.setDescription("Desc 2");
        prod2.setBrand("Brand B");
        prod2.setPrice(200.0);
        prod2.setImageUrl("url2");
        prod2.setCategories(categories2);

        when(productClient.getProductById("prod1")).thenReturn(prod1);
        when(productClient.getProductById("prod2")).thenReturn(prod2);

        Pageable pageable = PageRequest.of(0, 10);

        Page<CartProductDetailDTO> result = cartService.getAllCartItems(token, pageable);

        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getContent().get(0).getProductId()).isEqualTo("prod1");
        assertThat(result.getContent().get(1).getProductId()).isEqualTo("prod2");
    }

    @Test
    void getAllCartItems_shouldThrow_whenCartNotFound() {
        String token = "Bearer xyz";
        when(jwtUtil.getUserNameFromToken("xyz")).thenReturn("missingMember");

        when(cartRepository.findByMemberId("missingMember")).thenReturn(Optional.empty());

        Pageable pageable = PageRequest.of(0, 10);

        assertThrows(RuntimeException.class,
                () -> cartService.getAllCartItems(token, pageable));
    }


    // addProductToCart()
    @Test
    void addProductToCart_shouldAddNewProductToCart() {
        String token = "Bearer token123";
        String memberId = "member1";
        CartItemDTO cartItemDTO = new CartItemDTO("prod1", 2);

        when(jwtUtil.getUserNameFromToken("token123")).thenReturn(memberId);
        when(cartRepository.findByMemberId(memberId)).thenReturn(Optional.empty());
        List<String> c = Arrays.asList("Electronics", "Mobile");

        ProductDTO prod1 = new ProductDTO();
        prod1.setProductId("prod1");
        prod1.setName("Product 1");
        prod1.setDescription("Desc 1");
        prod1.setBrand("Brand A");
        prod1.setPrice(100.0);
        prod1.setImageUrl("url1");
        prod1.setCategories(c);

        when(productClient.getProductById("prod1")).thenReturn(prod1);

        CartResponseDTO response = cartService.addProductToCart(token, cartItemDTO);

        assertNotNull(response);
        assertEquals("prod1", response.getProductId());
        assertEquals(2, response.getQuantity());
        assertEquals("Product added successfully!", response.getMessage());

        then(cartRepository).should().save(any(Cart.class));
    }

    @Test
    void addProductToCart_shouldThrow_forInvalidQuantity() {
        String token = "Bearer token123";
        CartItemDTO cartItemDTO = new CartItemDTO("prod1", 0);

        assertThrows(IllegalArgumentException.class,
                () -> cartService.addProductToCart(token, cartItemDTO));
    }

    @Test
    void addProductToCart_shouldThrow_forInvalidProductId() {
        String token = "Bearer token123";
        String memberId = "member1";
        CartItemDTO cartItemDTO = new CartItemDTO("invalidProd", 1);

        when(jwtUtil.getUserNameFromToken("token123")).thenReturn(memberId);
        when(cartRepository.findByMemberId(memberId)).thenReturn(Optional.empty());
        when(productClient.getProductById("invalidProd")).thenReturn(null);

        assertThrows(IllegalArgumentException.class,
                () -> cartService.addProductToCart(token, cartItemDTO));
    }


    // deleteCartItem()
    @Test
    void deleteCartItem_shouldRemoveItemFromCart() {
        String token = "Bearer token123";
        String memberId = "member1";

        CartItem item1 = new CartItem("prod1", 2);
        CartItem item2 = new CartItem("prod2", 3);

        Cart cart = new Cart();
        cart.setMemberId(memberId);
        cart.setItems(new ArrayList<>(Arrays.asList(item1, item2)));

        when(jwtUtil.getUserNameFromToken("token123")).thenReturn(memberId);
        when(cartRepository.findByMemberId(memberId)).thenReturn(Optional.of(cart));

        cartService.deleteCartItem(token, "prod1");

        assertThat(cart.getItems()).hasSize(1);
        assertThat(cart.getItems().get(0).getProductId()).isEqualTo("prod2");
        then(cartRepository).should().save(cart);
    }

    @Test
    void deleteCartItem_shouldDeleteCart_whenEmpty() {
        String token = "Bearer token123";
        String memberId = "member1";

        CartItem item = new CartItem("prod1", 2);
        Cart cart = new Cart();
        cart.setMemberId(memberId);
        cart.setItems(new ArrayList<>(Arrays.asList(item)));

        when(jwtUtil.getUserNameFromToken("token123")).thenReturn(memberId);
        when(cartRepository.findByMemberId(memberId)).thenReturn(Optional.of(cart));

        cartService.deleteCartItem(token, "prod1");

        then(cartRepository).should().delete(cart);
    }

    @Test
    void deleteCartItem_shouldThrow_whenProductNotFound() {
        String token = "Bearer token123";
        String memberId = "member1";

        Cart cart = new Cart();
        cart.setMemberId(memberId);
        cart.setItems(new ArrayList<>(Arrays.asList(new CartItem("prod1", 1))));

        when(jwtUtil.getUserNameFromToken("token123")).thenReturn(memberId);
        when(cartRepository.findByMemberId(memberId)).thenReturn(Optional.of(cart));

        assertThrows(RuntimeException.class,
                () -> cartService.deleteCartItem(token, "prodX"));
    }
}
