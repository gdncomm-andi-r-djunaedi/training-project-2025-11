package com.gdn.faurihakim.cart.command.impl;

import com.blibli.oss.backend.common.model.response.Response;
import com.gdn.faurihakim.Cart;
import com.gdn.faurihakim.CartRepository;
import com.gdn.faurihakim.cart.client.MemberServiceClient;
import com.gdn.faurihakim.cart.client.ProductServiceClient;
import com.gdn.faurihakim.cart.client.model.GetMemberResponse;
import com.gdn.faurihakim.cart.client.model.GetProductResponse;
import com.gdn.faurihakim.cart.command.model.AddProductToCartCommandRequest;
import com.gdn.faurihakim.cart.web.model.response.AddProductToCartWebResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.Collections;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("AddProductToCartCommandImpl Happy Path Tests")
class AddProductToCartCommandImplTest {

    @Mock
    private CartRepository cartRepository;

    @Mock
    private MemberServiceClient memberServiceClient;

    @Mock
    private ProductServiceClient productServiceClient;

    @InjectMocks
    private AddProductToCartCommandImpl addProductToCartCommand;

    private static final String MEMBER_ID = "member-123";
    private static final String PRODUCT_ID = "product-456";
    private static final Integer QUANTITY = 2;

    private AddProductToCartCommandRequest request;
    private GetMemberResponse memberResponse;
    private GetProductResponse productResponse;

    @BeforeEach
    void setUp() {
        // Setup request
        AddProductToCartCommandRequest.ProductItem productItem = AddProductToCartCommandRequest.ProductItem.builder()
                .productId(PRODUCT_ID)
                .quantity(QUANTITY)
                .build();

        request = AddProductToCartCommandRequest.builder()
                .memberId(MEMBER_ID)
                .products(Collections.singletonList(productItem))
                .build();

        // Setup member response
        memberResponse = new GetMemberResponse();
        memberResponse.setMemberId(MEMBER_ID);
        memberResponse.setFullName("Test User");

        // Setup product response
        productResponse = new GetProductResponse();
        productResponse.setProductId(PRODUCT_ID);
        productResponse.setProductName("Test Product");
        productResponse.setPrice(100.0);
    }

    @Test
    @DisplayName("Should successfully add products to new cart")
    void testExecute_NewCart_AddsProductsSuccessfully() {
        // Arrange
        Response<GetMemberResponse> memberServiceResponse = new Response<>();
        memberServiceResponse.setData(memberResponse);

        Response<GetProductResponse> productServiceResponse = new Response<>();
        productServiceResponse.setData(productResponse);

        when(memberServiceClient.getMember(MEMBER_ID)).thenReturn(memberServiceResponse);
        when(productServiceClient.getProduct(PRODUCT_ID)).thenReturn(productServiceResponse);
        when(cartRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.empty());

        Cart savedCart = new Cart();
        savedCart.setCartId("cart-123");
        savedCart.setMemberId(MEMBER_ID);
        when(cartRepository.save(any(Cart.class))).thenReturn(savedCart);

        ArgumentCaptor<Cart> cartCaptor = ArgumentCaptor.forClass(Cart.class);

        // Act
        AddProductToCartWebResponse response = addProductToCartCommand.execute(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCartId()).isEqualTo("cart-123");

        verify(memberServiceClient).getMember(MEMBER_ID);
        verify(productServiceClient).getProduct(PRODUCT_ID);
        verify(cartRepository).findByMemberId(MEMBER_ID);
        verify(cartRepository).save(cartCaptor.capture());

        Cart capturedCart = cartCaptor.getValue();
        assertThat(capturedCart.getMemberId()).isEqualTo(MEMBER_ID);
        assertThat(capturedCart.getItems()).isNotEmpty();
    }

    @Test
    @DisplayName("Should successfully add products to existing cart")
    void testExecute_ExistingCart_AddsProductsSuccessfully() {
        // Arrange
        Response<GetMemberResponse> memberServiceResponse = new Response<>();
        memberServiceResponse.setData(memberResponse);

        Response<GetProductResponse> productServiceResponse = new Response<>();
        productServiceResponse.setData(productResponse);

        Cart existingCart = new Cart();
        existingCart.setCartId("existing-cart-123");
        existingCart.setMemberId(MEMBER_ID);

        when(memberServiceClient.getMember(MEMBER_ID)).thenReturn(memberServiceResponse);
        when(productServiceClient.getProduct(PRODUCT_ID)).thenReturn(productServiceResponse);
        when(cartRepository.findByMemberId(MEMBER_ID)).thenReturn(Optional.of(existingCart));
        when(cartRepository.save(any(Cart.class))).thenReturn(existingCart);

        // Act
        AddProductToCartWebResponse response = addProductToCartCommand.execute(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getCartId()).isEqualTo("existing-cart-123");

        verify(cartRepository).findByMemberId(MEMBER_ID);
        verify(cartRepository).save(existingCart);
    }

    @Test
    @DisplayName("Should validate member exists")
    void testExecute_ValidMember_Proceeds() {
        // Arrange
        Response<GetMemberResponse> memberServiceResponse = new Response<>();
        memberServiceResponse.setData(memberResponse);

        Response<GetProductResponse> productServiceResponse = new Response<>();
        productServiceResponse.setData(productResponse);

        when(memberServiceClient.getMember(MEMBER_ID)).thenReturn(memberServiceResponse);
        when(productServiceClient.getProduct(PRODUCT_ID)).thenReturn(productServiceResponse);
        when(cartRepository.findByMemberId(anyString())).thenReturn(Optional.empty());

        Cart savedCart = new Cart();
        savedCart.setCartId("cart-123");
        when(cartRepository.save(any(Cart.class))).thenReturn(savedCart);

        // Act
        AddProductToCartWebResponse response = addProductToCartCommand.execute(request);

        // Assert
        assertThat(response).isNotNull();
        verify(memberServiceClient).getMember(MEMBER_ID);
    }

    @Test
    @DisplayName("Should validate product exists")
    void testExecute_ValidProduct_Proceeds() {
        // Arrange
        Response<GetMemberResponse> memberServiceResponse = new Response<>();
        memberServiceResponse.setData(memberResponse);

        Response<GetProductResponse> productServiceResponse = new Response<>();
        productServiceResponse.setData(productResponse);

        when(memberServiceClient.getMember(MEMBER_ID)).thenReturn(memberServiceResponse);
        when(productServiceClient.getProduct(PRODUCT_ID)).thenReturn(productServiceResponse);
        when(cartRepository.findByMemberId(anyString())).thenReturn(Optional.empty());

        Cart savedCart = new Cart();
        savedCart.setCartId("cart-123");
        when(cartRepository.save(any(Cart.class))).thenReturn(savedCart);

        // Act
        AddProductToCartWebResponse response = addProductToCartCommand.execute(request);

        // Assert
        assertThat(response).isNotNull();
        verify(productServiceClient).getProduct(PRODUCT_ID);
    }
}
