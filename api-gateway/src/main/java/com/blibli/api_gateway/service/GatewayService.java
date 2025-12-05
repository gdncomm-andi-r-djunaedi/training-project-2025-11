package com.blibli.api_gateway.service;

import com.blibli.api_gateway.dto.*;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;

import java.util.List;

public interface GatewayService {
    AddToCartResponseDTO addProductToCart(String token,AddToCartRequestDTO addToCartRequestDTO);

    UserRegisterResponseDTO registerUser(UserRegisterRequestDTO userRegisterRequestDTO);

    LoginResponseDTO login(LoginRequestDTO loginRequestDTO);

    String getUserNameFromToken(String token);

    Boolean validateToken(String token );

    LogoutResponseDTO logout(String token );

    AddToCartResponseDTO viewCart(String token);

    AddToCartResponseDTO deleteBySku(String token ,String productSku);

    AddToCartResponseDTO deletAllItems(String token );

    List<CreateProductResponseDTO> createProduct(List<CreateProductRequestDTO> createProductRequestDTO);

    CreateProductResponseDTO findProductById(String productId);

    CreateProductResponseDTO updateProductData(CreateProductRequestDTO createProductRequestDTO);

    Page<SearchResponseDTO> search(SearchRequestDTO searchRequestDTO, PageRequest of);
}
