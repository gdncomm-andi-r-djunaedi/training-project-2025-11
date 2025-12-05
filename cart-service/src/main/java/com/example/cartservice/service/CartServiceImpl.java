package com.example.cartservice.service;

import com.example.cartservice.dto.AddToCartRequest;
import com.example.cartservice.dto.AddToCartResponse;
import com.example.cartservice.dto.GetCartResponse;
import com.example.cartservice.dto.GetProductResponse;
import com.example.cartservice.exception.BusinessException;
import com.example.cartservice.model.Cart;
import com.example.cartservice.repository.CartRepository;
import lombok.RequiredArgsConstructor;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.math.BigDecimal;
import java.time.Instant;

@Service
@RequiredArgsConstructor
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;

    private final RestTemplate restTemplate;

    @Value("${product.service.base-url}")
    @Setter
    private String productServiceUrl;

    @Override
    public AddToCartResponse addToCart(String memberId, AddToCartRequest request) {

        String productUrl = productServiceUrl + "/api/product/product-detail/" + request.productId();

        try {
            GetProductResponse product = restTemplate.getForObject(productUrl, GetProductResponse.class);
            if (product == null) {
                throw new BusinessException("PRODUCT_NOT_FOUND", "Product not found with id: " + request.productId());
            }
        } catch (HttpClientErrorException.NotFound ex) {
            throw new BusinessException("PRODUCT_NOT_FOUND", "Product not found with id: " + request.productId());
        }

        Cart cart = cartRepository
                .findByMemberIdAndProductId(memberId, request.productId())
                .map(existing -> {
                    existing.setQty(request.qty());
                    existing.setUpdatedAt(Instant.now());
                    return existing;
                })
                .orElseGet(() -> Cart.builder()
                        .memberId(memberId)
                        .productId(request.productId())
                        .qty(request.qty())
                        .createdAt(Instant.now())
                        .updatedAt(Instant.now())
                        .build()
                );

        Cart saved = cartRepository.save(cart);

        return new AddToCartResponse(
                saved.getId(),
                saved.getMemberId(),
                saved.getProductId(),
                saved.getQty()
        );
    }

    @Override
    public Page<GetCartResponse> getCart(String memberId, Pageable pageable) {
        return cartRepository.findByMemberId(memberId, pageable)
                .map(cart -> {
                    String productUrl = productServiceUrl + "/api/product/product-detail/" + cart.getProductId();
                    GetProductResponse product = restTemplate.getForObject(productUrl, GetProductResponse.class);

                    String productName = product != null ? product.name() : "Unknown Product";
                    BigDecimal totalPrice = product != null
                            ? product.price().multiply(BigDecimal.valueOf(cart.getQty()))
                            : BigDecimal.ZERO;

                    return new GetCartResponse(
                            cart.getId(),
                            cart.getMemberId(),
                            cart.getProductId(),
                            productName,
                            cart.getQty(),
                            totalPrice
                    );
                });
    }

    @Override
    public boolean deleteFromCart(String id) {
        if (!cartRepository.existsById(id)) return false;

        cartRepository.deleteById(id);
        return true;
    }
}