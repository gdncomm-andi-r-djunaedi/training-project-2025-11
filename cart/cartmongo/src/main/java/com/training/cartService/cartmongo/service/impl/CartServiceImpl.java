package com.training.cartService.cartmongo.service.impl;

import com.training.cartService.cartmongo.dto.AddToCartRequest;
import com.training.cartService.cartmongo.dto.CartResponse;
import com.training.cartService.cartmongo.dto.ProductDTO;
import com.training.cartService.cartmongo.entity.Cart;
import com.training.cartService.cartmongo.entity.CartItemEntity;
import com.training.cartService.cartmongo.exception.CartException;
import com.training.cartService.cartmongo.repository.CartRepository;
import com.training.cartService.cartmongo.service.CartService;
import com.training.cartService.cartmongo.service.ProductService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.Optional;

@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductService productService;

    public CartServiceImpl(CartRepository cartRepository, ProductService productService) {
        this.cartRepository = cartRepository;
        this.productService = productService;
    }



    private void recalculateCartTotals(Cart cart) {
        double totalPrice = 0.0;
        int totalQuantity = 0;

        for (CartItemEntity item : cart.getItems()) {
            totalPrice += item.getPrice() * item.getQuantity();
            totalQuantity += item.getQuantity();
        }

        cart.setTotalPrice(totalPrice);
        cart.setTotalQuantity(totalQuantity);
    }

    @Override
    public CartResponse addToCart(String userId, AddToCartRequest request) {
        if (request.getQuantity() <= 0) {
            throw new CartException.InvalidRequestException("Quantity must be greater than 0");
        }
        if(userId == null || userId.isEmpty()) {
            throw new CartException.InvalidRequestException("User ID cannot be null or empty");
        }
        ProductDTO product = productService.getProductBySku(request.getSku());
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new CartException.CartNotFoundException(userId));
        Optional<CartItemEntity> existingItem = cart.getItems().stream().filter(item -> item.getId().equals(product.getId())).findFirst();
        if (existingItem.isPresent()) {
            CartItemEntity item = existingItem.get();
            item.setQuantity(item.getQuantity() + request.getQuantity());
        } else {
            CartItemEntity newItem = getCartItemEntity(request, product);
            cart.getItems().add(newItem);
        }
        recalculateCartTotals(cart);
        Cart savedCart = cartRepository.save(cart);
        return new CartResponse(savedCart.getTotalPrice(), savedCart.getTotalQuantity(), savedCart.getItems());
    }

    private static CartItemEntity getCartItemEntity(AddToCartRequest request, ProductDTO product) {
        CartItemEntity newItem = new CartItemEntity();
        newItem.setId(product.getId());
        newItem.setName(product.getName());
        newItem.setSku(product.getSku());
        newItem.setDescription(product.getDescription());
        newItem.setPrice(product.getPrice());
        newItem.setCategory(product.getCategory());
        newItem.setTags(product.getTags());
        newItem.setImages(product.getImages());
        newItem.setQuantity(request.getQuantity());
        return newItem;
    }

    public CartResponse getCart(String userId) {
        if(userId == null || userId.isEmpty()) {
            throw new CartException.InvalidRequestException("User ID cannot be null or empty");
        }
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new CartException.CartNotFoundException(userId));

        return new CartResponse(cart.getTotalPrice(), cart.getTotalQuantity(), cart.getItems());
    }

    public CartResponse deleteCartItems(String userId) {
        if(userId == null || userId.isEmpty()) {
            throw new CartException.InvalidRequestException("User ID cannot be null or empty");
        }
        Cart cart = cartRepository.findByUserId(userId).orElseThrow(() -> new CartException.CartNotFoundException(userId));
        cart.getItems().clear();
        cart.setTotalPrice(0.0);
        cart.setTotalQuantity(0);
        Cart savedCart = cartRepository.save(cart);
        return new CartResponse(savedCart.getTotalPrice(), savedCart.getTotalQuantity(), savedCart.getItems());
    }

}
