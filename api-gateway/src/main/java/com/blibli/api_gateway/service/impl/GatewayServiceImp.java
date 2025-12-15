package com.blibli.api_gateway.service.impl;

import com.blibli.api_gateway.Feign.CartFeign;
import com.blibli.api_gateway.Feign.MemberFeign;
import com.blibli.api_gateway.Feign.ProductFeign;
import com.blibli.api_gateway.dto.*;
import com.blibli.api_gateway.service.GatewayService;
import com.blibli.api_gateway.utils.JwtUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class GatewayServiceImp implements GatewayService {

    @Autowired
    CartFeign cartFeign;
    @Autowired
    MemberFeign memberFeign;
    @Autowired
    ProductFeign productFeign;
    @Autowired
    JwtUtils jwtUtils;

    private String userName;
    private String token;

    @Override
    public AddToCartResponseDTO addProductToCart(String token ,AddToCartRequestDTO addToCartRequestDTO) {
        return cartFeign.addProductToCart(getUserNameFromToken(token),addToCartRequestDTO).getBody();
    }

    @Override
    public UserRegisterResponseDTO registerUser(UserRegisterRequestDTO userRegisterRequestDTO) {
        return memberFeign.authRegister(userRegisterRequestDTO).getBody();
    }

    @Override
    public LoginResponseDTO login(LoginRequestDTO loginRequestDTO) {
        LoginResponseDTO loginResponseDTO = memberFeign.login(loginRequestDTO).getBody();
        this.token = loginResponseDTO.getToken();
        return loginResponseDTO;
    }

    @Override
    public String getUserNameFromToken(String token) {
        this.userName = jwtUtils.extractUsername(token);
        return userName;
    }

    @Override
    public Boolean validateToken(String token) {
        return memberFeign.getValidateToken(token,getUserNameFromToken(token)).getBody();
    }

    @Override
    public LogoutResponseDTO logout(String token) {
        return memberFeign.logout(getUserNameFromToken(token),token).getBody();
    }

    @Override
    public AddToCartResponseDTO viewCart(String token) {
        return cartFeign.viewCart(getUserNameFromToken(token)).getBody();
    }

    @Override
    public AddToCartResponseDTO deleteBySku(String token ,String productSku) {
        return cartFeign.deletBySku(getUserNameFromToken(token),productSku).getBody();
    }

    @Override
    public AddToCartResponseDTO deletAllItems(String token ) {
        return cartFeign.deleteAllItems(getUserNameFromToken(token)).getBody();
    }

    @Override
    public List<CreateProductResponseDTO> createProduct(List<CreateProductRequestDTO> createProductRequestDTO) {
        return productFeign.createProduct(createProductRequestDTO).getBody();
    }

    @Override
    public CreateProductResponseDTO findProductById(String productId) {
        return productFeign.getProductProductById(productId).getBody();
    }

    @Override
    public CreateProductResponseDTO updateProductData(CreateProductRequestDTO createProductRequestDTO) {
        return productFeign.updateProductData(createProductRequestDTO).getBody();
    }

    @Override
    public Page<SearchResponseDTO> search(SearchRequestDTO searchRequestDTO, PageRequest of) {
        return productFeign.searchByProductName(searchRequestDTO,of).getBody();
    }
}
