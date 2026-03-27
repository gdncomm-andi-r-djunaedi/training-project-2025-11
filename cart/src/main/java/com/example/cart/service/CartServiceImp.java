package com.example.cart.service;

import com.example.cart.dto.AddToCartRequest;
import com.example.cart.dto.AddToCartResponse;
import com.example.cart.dto.CartItemListResponse;
import com.example.cart.entity.CartEntity;
import com.example.cart.repository.CartRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
public class CartServiceImp implements CartService {

    @Autowired
    private CartRepository cartRepository;

    @Override
    public AddToCartResponse addProductToCart(AddToCartRequest addToCart) {
        CartEntity cartEntity = cartRepository.findByCustomerIdAndProductId(addToCart.getCustomerId(), addToCart.getProductId());
        if (cartEntity == null) {
            cartEntity = new CartEntity();
            cartEntity.setProductId(addToCart.getProductId());
            cartEntity.setProductName(addToCart.getProductName());
            cartEntity.setProductQuantity(addToCart.getProductQuantity());
            cartEntity.setItemPrice(addToCart.getItemPrice());
            cartEntity.setTotalPrice(addToCart.getItemPrice() * addToCart.getProductQuantity());
            cartEntity.setCustomerId(addToCart.getCustomerId());
            cartEntity.setCustomerName(addToCart.getCustomerName());
        } else {
            int newQuantity = cartEntity.getProductQuantity() + addToCart.getProductQuantity();
            cartEntity.setProductQuantity(newQuantity);
            double newTotal = cartEntity.getTotalPrice()
                    + (cartEntity.getItemPrice() * addToCart.getProductQuantity());
            cartEntity.setTotalPrice(newTotal);
        }
        CartEntity saved = cartRepository.save(cartEntity);
        return convertToResponse(saved);
    }

    @Override
    @Transactional(readOnly = true)
    public CartItemListResponse getAllCartProducts( int pageNumber, int pageSize, UUID customerId) {
        if (pageNumber < 0) {
            throw new IllegalArgumentException("Page number cannot be negative");
        }
        if (pageSize <= 0) {
            throw new IllegalArgumentException("Page size must be greater than 0");
        }
        Pageable pageable = PageRequest.of(pageNumber, pageSize);
        Page<CartEntity> cartEntities = cartRepository.findAllByCustomerId(pageable, customerId);
        return buildCartItemListResponse(cartEntities);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteAllCartItemsByCustomerId(UUID customerId){
        cartRepository.deleteAllByCustomerId(customerId);
    }

    @Override
    @Transactional(readOnly = false)
    public void deleteAllCartItemsByCustomerIdProductId(UUID customerId,String productId){
        cartRepository.deleteAllByCustomerIdAndProductId(customerId,productId);
    }

    private CartItemListResponse buildCartItemListResponse(Page<CartEntity> cartEntities) {
        List<AddToCartResponse> cartResponses = new ArrayList<>();
        for (CartEntity cartEntity : cartEntities.getContent()) {
            AddToCartResponse addToCartResponse = convertToResponse(cartEntity);
            cartResponses.add(addToCartResponse);
        }
        CartItemListResponse response = new CartItemListResponse();
        response.setCartResponses(cartResponses);
        response.setCurrentPage(cartEntities.getNumber());
        response.setPageSize(cartEntities.getSize());
        response.setTotalElements(cartEntities.getTotalElements());
        response.setTotalPages(cartEntities.getTotalPages());
        response.setHasNext(cartEntities.hasNext());
        response.setHasPrevious(cartEntities.hasPrevious());
        return response;
    }

    private AddToCartResponse convertToResponse(CartEntity cartEntity) {
        AddToCartResponse response = new AddToCartResponse();
        response.setProductId(cartEntity.getProductId());
        response.setProductName(cartEntity.getProductName());
        response.setProductQuantity(cartEntity.getProductQuantity());
        response.setItemPrice(cartEntity.getItemPrice());
        response.setTotalPrice(cartEntity.getTotalPrice());
        return response;
    }
}
