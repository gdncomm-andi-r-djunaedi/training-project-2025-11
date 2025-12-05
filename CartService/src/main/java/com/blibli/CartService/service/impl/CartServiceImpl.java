package com.blibli.CartService.service.impl;

import com.blibli.CartService.client.ProductFeignClient;
import com.blibli.CartService.dto.AddToCartRequest;
import com.blibli.CartService.dto.CartItemDto;
import com.blibli.CartService.dto.CartResponseDto;
import com.blibli.CartService.dto.ProductResponseDto;
import com.blibli.CartService.entity.CartEntity;
import com.blibli.CartService.entity.CartItem;
import com.blibli.CartService.exception.CartNotFoundException;
import com.blibli.CartService.exception.ProductUnavailableException;
import com.blibli.CartService.repository.CartRepository;
import com.blibli.CartService.service.CartService;
import com.blibli.CartService.util.ApiResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
public class CartServiceImpl implements CartService {

    private final CartRepository cartRepository;
    private final ProductFeignClient productClient;

    public CartServiceImpl(CartRepository cartRepository, ProductFeignClient productClient) {
        this.cartRepository = cartRepository;
        this.productClient = productClient;
    }


    @Override
    public CartResponseDto addOrUpdateCart(String userId, AddToCartRequest addToCartRequest) {
        CartEntity cart = cartRepository
                .findByUserId(userId)
                .orElse(CartEntity.builder()
                        .userId(userId)
                        .items(new ArrayList<>())
                        .build());

        Optional<CartItem> existingItem = cart.getItems().stream()
                .filter(i -> i.getProductId().equals(addToCartRequest.getProductId()))
                .findFirst();



        if (existingItem.isPresent()) {
            if (addToCartRequest.getQuantity() <= 0) {
                cart.getItems().remove(existingItem.get());
            } else {
                existingItem.get().setQuantity(addToCartRequest.getQuantity());
            }
        } else {
            if (addToCartRequest.getQuantity() > 0) {

                ApiResponse<ProductResponseDto> response = productClient.getProduct(addToCartRequest.getProductId());

                log.info("Response is : "+ response.getData());
                ProductResponseDto product =
                        response.getData();
                BigDecimal total = product.getPrice().multiply(BigDecimal.valueOf(addToCartRequest.getQuantity()).setScale(2, RoundingMode.HALF_UP));
                cart.getItems().add(CartItem.builder()
                        .productId(product.getSku())
                        .productName(product.getProductName())
                        .price(total)
                        .description(product.getDescription())
                        .quantity(addToCartRequest.getQuantity())
                        .build());
            }
        }

        cartRepository.save(cart);
        return convertToDto(cart);
    }

    @Override
    public CartResponseDto viewCart(String userId) {
        CartEntity cart = cartRepository.findByUserId(userId)
                .orElseThrow(() -> new CartNotFoundException("Cart is empty"));

        boolean cartUpdated = false;

        for (CartItem item : cart.getItems()) {

            ApiResponse<ProductResponseDto> response = productClient.getProduct(item.getProductId());

            ProductResponseDto product =response.getData();


            BigDecimal latestTotal =
                    product.getPrice()
                            .multiply(BigDecimal.valueOf(item.getQuantity()))
                            .setScale(2, RoundingMode.HALF_UP);

            if (item.getPrice().compareTo(latestTotal) != 0) {
                item.setPrice(latestTotal);
                cartUpdated = true;
            }

            if (!item.getProductName().equals(product.getProductName())) {
                item.setProductName(product.getProductName());
                cartUpdated = true;
            }

            if (!item.getDescription().equals(product.getDescription())) {
                item.setDescription(product.getDescription());
                cartUpdated = true;
            }
        }

        if (cartUpdated) {
            cartRepository.save(cart);
        }

        return convertToDto(cart);
    }

    @Override
    public void deleteItem(String userId, String productId) {

        CartEntity cart = cartRepository
                .findByUserId(userId)
                .orElseThrow(()-> new ProductUnavailableException("Product Not Found in the cart"));

        cart.getItems()
                .removeIf(i -> i.getProductId().equals(productId));

        cartRepository.save(cart);
    }



    public CartResponseDto convertToDto(CartEntity cartEntity){
        CartResponseDto dto = new CartResponseDto();

        dto.setUserId(cartEntity.getUserId());

        if (cartEntity.getItems() != null) {
            List<CartItemDto> itemDtos = cartEntity.getItems().stream()
                    .map(this::convertItemToDto)
                    .toList();

            dto.setItems(itemDtos);
        }

        return dto;
    }


    private CartItemDto convertItemToDto(CartItem item) {
        CartItemDto dto = new CartItemDto();
        dto.setProductId(item.getProductId());
        dto.setProductName(item.getProductName());
        dto.setPrice(item.getPrice());
        dto.setQuantity(item.getQuantity());
        return dto;
    }
}
