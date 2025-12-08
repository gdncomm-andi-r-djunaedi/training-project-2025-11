package com.kailash.cart.service.impl;

import com.kailash.cart.client.ProductClient;
import com.kailash.cart.dto.ApiResponse;
import com.kailash.cart.dto.CartResponse;
import com.kailash.cart.dto.ProductResponse;
import com.kailash.cart.entity.Cart;
import com.kailash.cart.entity.CartItem;
import com.kailash.cart.exception.NotFoundException;
import com.kailash.cart.repository.CartRepository;
import com.kailash.cart.service.CartService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
public class CartServiceImpl implements CartService {

    @Autowired
    CartRepository cartRepository;
    @Autowired
    ProductClient productClient;

    public Double totalPrice(List<CartItem> cartItems) {
        Double total = 0.0;
        for (CartItem cartItem : cartItems) {
            total += cartItem.getPriceSnapshot();
        }
        return total;
    }

    public int totalItems(List<CartItem> cartItems) {
        int total = 0;
        for (CartItem cartItem : cartItems) {
            total += cartItem.getQty();
        }
        return total;
    }

    private CartResponse toCartResponse(Cart cart) {
        return new CartResponse(cart.getId(), cart.getMemberId(),
                cart.getTotalItems(), cart.getTotalPrice());
    }

    @Override
    public ApiResponse<CartResponse> getCart(String memberId) {
        try {
            Cart cart = cartRepository.findByMemberId(memberId).orElseGet(() -> {
                Cart c = new Cart();
                c.setMemberId(memberId);
                c.setItems(new ArrayList<>());
                return cartRepository.save(c);
            });
            return new ApiResponse<>(true, "Cart fetched successfully", toCartResponse(cart));
        } catch (Exception ex) {
            return new ApiResponse<>(false, ex.getMessage(), null);
        }
    }

    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<CartResponse> addOrUpdateItem(String memberId, String sku, int qty) {
        try {
            if (qty == 0) {
                return new ApiResponse<>(false, "Quantity should not be 0", null);
            }


            Cart cart = cartRepository.findByMemberId(memberId)
                    .orElseGet(() -> {
                        Cart c = new Cart();
                        c.setMemberId(memberId);
                        c.setItems(new ArrayList<>());
                        return cartRepository.save(c);
                    });


            ResponseEntity<ApiResponse<ProductResponse>> productApi = productClient.get(sku);
            ApiResponse<ProductResponse> productBody = productApi.getBody();
            if (!productBody.isSuccess() || productBody.getData() == null) {
                return new ApiResponse<>(false,
                        "Unable to fetch product details for SKU: " + sku,
                        null);
            }
            ProductResponse productResponse = productBody.getData();


            CartItem existingItem = cart.getItems().stream()
                    .filter(item -> item.getSku().equals(sku))
                    .findFirst()
                    .orElse(null);

            if (existingItem != null) {

                existingItem.setQty(existingItem.getQty() + qty);
                existingItem.setProductName(productResponse.getName());
                existingItem.setPriceSnapshot(productResponse.getPrice());
            } else {

                CartItem newItem = new CartItem();
                newItem.setSku(sku);
                newItem.setProductName(productResponse.getName());
                newItem.setQty(qty);
                newItem.setPriceSnapshot(productResponse.getPrice());
                cart.getItems().add(newItem);
            }


            if (totalItems(cart.getItems()) < 0) {
                cart.getItems().removeIf(item -> item.getSku().equals(sku));
            }


            int totalItems = totalItems(cart.getItems());
            cart.setTotalItems(totalItems);
            cart.setTotalPrice(totalItems * productResponse.getPrice());

            Cart savedCart = cartRepository.save(cart);
            return new ApiResponse<>(true, "Item added/updated successfully", toCartResponse(savedCart));

        } catch (Exception ex) {
            return new ApiResponse<>(false, ex.getMessage(), null);
        }
    }


    @Override
    @Transactional(rollbackFor = Exception.class)
    public ApiResponse<CartResponse> removeItem(String memberId, String sku) {
        try {
            Cart cart = cartRepository.findByMemberId(memberId)
                    .orElseThrow(() -> new NotFoundException("Cart is empty for the given member"));


            cart.getItems().removeIf(item -> sku.equals(item.getSku()));
            cart.setTotalItems(totalItems(cart.getItems()));
            cart.setTotalPrice(totalPrice(cart.getItems()));
            Cart savedCart = cartRepository.save(cart);

            return new ApiResponse<>(true, "Item removed successfully", toCartResponse(savedCart));
        } catch (Exception ex) {
            return new ApiResponse<>(false, ex.getMessage(), null);
        }
    }
}
