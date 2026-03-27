package com.example.api_gateway.serviceImpl;

import com.example.api_gateway.client.CartServiceClient;
import com.example.api_gateway.client.ProductServiceClient;
import com.example.api_gateway.exception.TokenException;
import com.example.api_gateway.request.AddToCart;
import com.example.api_gateway.request.AddToCartRequest;
import com.example.api_gateway.response.AddToCartResponse;
import com.example.api_gateway.response.CartItemListResponse;
import com.example.api_gateway.exception.InvalidCredentialsExceptionToken;
import com.example.api_gateway.response.ProductResponse;
import com.example.api_gateway.service.CartService;
import com.example.api_gateway.service.JwtService;
import io.jsonwebtoken.Claims;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Service
public class CartServiceImp implements CartService {

    @Autowired
    JwtService jwtService;
    @Autowired
    ProductServiceClient productServiceClient;
    @Autowired
    CartServiceClient cartServiceClient;
    @Autowired
    private RedisTemplate<String, String> redisTemplate;

    @Override
    public AddToCartResponse addProductToBag(String token, AddToCart addToCart) throws Exception {
        Claims claims = jwtService.validateToken(token);
        UUID userId = jwtService.getUserIdFromToken(token);
        String email = jwtService.getUserMailFromToken(token);
        if (!redisTemplate.hasKey(email)) {
            throw new InvalidCredentialsExceptionToken("Token has been invalidated. Please login again.");
        }
        String userName = jwtService.getUsernameFromToken(token);
        ProductResponse productResponse = productServiceClient.getProductDetailByItemSku(addToCart.getProductId()).getBody();
        AddToCartRequest addToCartRequest = new AddToCartRequest();
        addToCartRequest.setCustomerId(userId);
        addToCartRequest.setCustomerName(userName);
        addToCartRequest.setProductId(productResponse.getItemSku());
        addToCartRequest.setProductQuantity(addToCart.getProductQuantity());
        addToCartRequest.setItemPrice(productResponse.getProductPrice());
        addToCartRequest.setProductName(productResponse.getProductName());
        AddToCartResponse addToCartResponse = cartServiceClient.addProductToBag(addToCartRequest).getBody();
        return addToCartResponse;
    }

    @Override
    public CartItemListResponse getAllCartProductsOFCustomer(String token, int page, int size) throws Exception{
        Claims claims = jwtService.validateToken(token);
        UUID userId = jwtService.getUserIdFromToken(token);
        String email = jwtService.getUserMailFromToken(token);
        if (!redisTemplate.hasKey(email)) {
            throw new InvalidCredentialsExceptionToken("Token has been invalidated. Please login again.");
        }
        CartItemListResponse cartItemListResponse = cartServiceClient.getAllCartProducts(page,size,userId).getBody();
        return cartItemListResponse;
    }

    @Override
    public void deleteAllproductsByCustomerId(String token) throws Exception{
        Claims claims = jwtService.validateToken(token);
        UUID userId = jwtService.getUserIdFromToken(token);
        String email = jwtService.getUserMailFromToken(token);
        if (!redisTemplate.hasKey(email)) {
            throw new InvalidCredentialsExceptionToken("Token has been invalidated. Please login again.");
        }
        cartServiceClient.deleteAllProductsByCustomerId(userId);
    }

    @Override
    public void deleteAllProductsByCustomeridAndProductId(String token,String productId) throws Exception{
        Claims claims = jwtService.validateToken(token);
        UUID userId = jwtService.getUserIdFromToken(token);
        String email = jwtService.getUserMailFromToken(token);
        if (!redisTemplate.hasKey(email)) {
            throw new InvalidCredentialsExceptionToken("Token has been invalidated. Please login again.");
        }
        productServiceClient.getProductDetailByItemSku(productId);
        cartServiceClient.deleteAllProductsByCustomerIdAndProductId(userId,productId);
    }

}
