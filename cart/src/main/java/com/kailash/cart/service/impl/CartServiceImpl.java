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
import org.springframework.stereotype.Service;

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
    public ApiResponse<CartResponse> addOrUpdateItem(String memberId, String sku, int qty) {
        try {
            if (qty == 0) {
                return new ApiResponse<>(false, "Quantity should not be 0 or lesser", null);
            }

            Cart cart = cartRepository.findByMemberId(memberId).orElseGet(() -> {
                Cart c = new Cart();
                c.setMemberId(memberId);
                c.setItems(new ArrayList<>());
                return cartRepository.save(c);
            });

            if (cart.getItems().isEmpty()) {
                CartItem cartItem = new CartItem();
                ProductResponse productResponse = productClient.get(sku).getBody().getData();
                cartItem.setSku(productResponse.getSku());
                cartItem.setProductName(productResponse.getName());
                cartItem.setQty(qty);
                cartItem.setPriceSnapshot(productResponse.getPrice());
                cart.getItems().add(cartItem);
                cart.setTotalItems(totalItems(cart.getItems()));
                cart.setTotalPrice(cart.getTotalItems()*productResponse.getPrice());

            } else {
                CartItem duplicate = null;
                for (CartItem cartItem : cart.getItems()) {
                    if (cartItem.getSku().equals(sku)) {
                        ProductResponse productResponse=productClient.get(sku).getBody().getData();
                        cartItem.setQty(qty+cartItem.getQty());
                        cartItem.setProductName(productResponse.getName());
                        cartItem.setPriceSnapshot(productResponse.getPrice());
                        cart.setTotalItems(totalItems(cart.getItems()));
                        cart.setTotalPrice(cart.getTotalItems()*productResponse.getPrice());
                        duplicate = cartItem;
                    }
                }
                if (duplicate==null) {

                    duplicate=new CartItem();
                    duplicate.setSku(sku);
                    duplicate.setProductName(productClient.get(sku).getBody().getData().getName());
                    duplicate.setQty(qty);
                    duplicate.setPriceSnapshot(productClient.get(sku).getBody().getData().getPrice());
                    cart.getItems().add(duplicate);
                    cart.setTotalItems(totalItems(cart.getItems()));
                    cart.setTotalPrice(cart.getTotalItems()*productClient.get(sku).getBody().getData().getPrice());

                }
            }
            if (cart.getTotalItems()<0)
            {
                cart.getItems().removeIf(item -> sku.equals(item.getSku()));
                cart.setTotalItems(totalItems(cart.getItems()));
                cart.setTotalPrice(cart.getTotalItems()*productClient.get(sku).getBody().getData().getPrice());

            }
            Cart savedCart = cartRepository.save(cart);
            return new ApiResponse<>(true, "Item added/updated successfully", toCartResponse(savedCart));
        } catch (Exception ex) {
            return new ApiResponse<>(false, ex.getMessage(), null);
        }
    }

    @Override
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
