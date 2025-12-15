package com.example.cartservice.service;

import com.example.cartservice.dto.AddToCartRequest;
import com.example.cartservice.dto.AddToCartResponse;
import com.example.cartservice.dto.GetCartResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface CartService {

    AddToCartResponse addToCart(String memberId, AddToCartRequest request);

    Page<GetCartResponse> getCart(String memberId, Pageable pageable);

    boolean deleteFromCart(String id);
}