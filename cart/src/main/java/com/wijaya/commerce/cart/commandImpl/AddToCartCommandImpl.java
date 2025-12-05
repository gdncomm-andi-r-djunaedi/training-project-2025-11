package com.wijaya.commerce.cart.commandImpl;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

import org.springframework.stereotype.Service;

import lombok.extern.slf4j.Slf4j;

import com.wijaya.commerce.cart.command.AddToCartCommand;
import com.wijaya.commerce.cart.commandImpl.model.AddToCartCommandRequest;
import com.wijaya.commerce.cart.commandImpl.model.AddToCartCommandResponse;
import com.wijaya.commerce.cart.exception.ProductNotActiveException;
import com.wijaya.commerce.cart.exception.ProductNotFoundException;
import com.wijaya.commerce.cart.exception.UserNotActiveException;
import com.wijaya.commerce.cart.exception.UserNotFoundException;
import com.wijaya.commerce.cart.modelDb.CartModelDb;
import com.wijaya.commerce.cart.modelDb.CartModelDb.CartItem;
import com.wijaya.commerce.cart.outbound.outboundModel.response.GetDetailProductOutboundResponse;
import com.wijaya.commerce.cart.outbound.outboundModel.response.GetDetailUserOutboundResponse;
import com.wijaya.commerce.cart.outbound.outboundModel.response.WebResponse;
import com.wijaya.commerce.cart.outbound.outboundService.ProductOutboundService;
import com.wijaya.commerce.cart.outbound.outboundService.UserOutboundService;
import com.wijaya.commerce.cart.repository.CartRepository;

import lombok.RequiredArgsConstructor;

@Slf4j
@Service
@RequiredArgsConstructor
public class AddToCartCommandImpl implements AddToCartCommand {

    private final UserOutboundService userOutbondService;

    private final ProductOutboundService productOutbondService;

    private final CartRepository cartRepository;

    @Override
    public AddToCartCommandResponse doCommand(AddToCartCommandRequest request) {
        log.info("Starting add to cart command for userId: {}, productSku: {}, quantity: {}, cartId: {}",
                request.getUserId(), request.getProductSku(), request.getQuantity(), request.getCartId());

        try {
            // Get and validate user
            WebResponse<?> user = getUser(request.getUserId());
            checkUserExistAndActive(user);

            // Get and validate product
            WebResponse<?> product = getProduct(request.getProductSku());
            checkProductExistAndActive(product);

            // Extract product data after validation
            GetDetailProductOutboundResponse productData = (GetDetailProductOutboundResponse) product.getData();

            CartModelDb cartModelDb;
            if (request.getCartId() != null && request.getUserId() != null
                    && cartRepository.existsByIdAndUserId(request.getCartId(), request.getUserId())) {
                cartModelDb = cartRepository.findByIdAndUserId(request.getCartId(), request.getUserId()).get();
                List<CartItem> items = cartModelDb.getItems();
                if (items == null) {
                    items = new ArrayList<>();
                }

                if (items.stream().anyMatch(item -> item.getProductSku().equals(request.getProductSku()))) {
                    // Update existing item
                    CartItem item = items.stream()
                            .filter(i -> i.getProductSku().equals(request.getProductSku()))
                            .findFirst()
                            .get();
                    item.setQuantity(request.getQuantity());
                    item.setSubTotal((int) (productData.getPrice() * item.getQuantity()));
                    item.setAddedAt(LocalDateTime.now());
                } else {
                    // Add new item to the cart
                    items = constructCartItem(items, productData, request);
                }
                cartModelDb.setItems(items);
                cartModelDb.setUpdatedAt(LocalDateTime.now());
            } else {
                cartModelDb = constructCartModelDb(new ArrayList<>(), request, productData);
            }
            cartRepository.save(cartModelDb);

            AddToCartCommandResponse response = addToCartToResponse(cartModelDb);
            return response;

        } catch (RuntimeException e) {
            log.error("Error executing add to cart command for userId: {}, productSku: {}, error: {}",
                    request.getUserId(), request.getProductSku(), e.getMessage(), e);
            throw e;
        }
    }

    private AddToCartCommandResponse addToCartToResponse(CartModelDb cartModelDb) {
        return AddToCartCommandResponse.builder()
                .cartId(cartModelDb.getId().toHexString())
                .items(cartModelDb.getItems().stream().map(item -> {
                    return AddToCartCommandResponse.CartItemResponse.builder()
                            .productSku(item.getProductSku())
                            .productName(item.getProductName())
                            .price(item.getPrice())
                            .comparePrice(item.getComparePrice())
                            .discountPercentage(item.getDiscountPercentage())
                            .quantity(item.getQuantity())
                            .subtotal(item.getSubTotal().longValue())
                            .addedAt(item.getAddedAt())
                            .build();
                }).collect(Collectors.toList()))
                .summary(AddToCartCommandResponse.CartSummary.builder()
                        .totalItems(cartModelDb.getItems().size())
                        .totalPrice(cartModelDb.getItems().stream().mapToLong(item -> item.getSubTotal()).sum())
                        .totalDiscount(cartModelDb.getItems().stream()
                                .mapToLong(item -> item.getSubTotal() - item.getPrice())
                                .sum())
                        .build())
                .build();
    }

    private CartModelDb constructCartModelDb(List<CartItem> cartItems, AddToCartCommandRequest request,
            GetDetailProductOutboundResponse product) {
        CartModelDb cartModelDb = CartModelDb.builder()
                .userId(request.getUserId())
                .items(constructCartItem(cartItems, product, request))
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();

        return cartModelDb;
    }

    private List<CartItem> constructCartItem(List<CartItem> cartItems, GetDetailProductOutboundResponse product,
            AddToCartCommandRequest request) {
        cartItems.add(CartItem.builder()
                .productSku(product.getSku())
                .productName(product.getName())
                .price(product.getPrice())
                .comparePrice(product.getComparePrice())
                .discountPercentage(product.getDiscountPercentage())
                .quantity(request.getQuantity())
                .subTotal((int) (product.getPrice() * request.getQuantity()))
                .addedAt(LocalDateTime.now())
                .build());
        return cartItems;
    }

    private void checkUserExistAndActive(WebResponse<?> user) {
        // Check if response is null or unsuccessful
        if (user == null || !user.isSuccess()) {
            log.error("User validation failed - response is null or unsuccessful");
            throw new UserNotFoundException("User not found");
        }

        GetDetailUserOutboundResponse userDetail;

        // Handle different data types
        if (user.getData() instanceof String) {
            log.error("User not found - received error: {}", user.getData());
            throw new UserNotFoundException("User not found");
        } else if (user.getData() instanceof java.util.Map) {
            // Jackson deserialized as LinkedHashMap, convert to proper type
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                userDetail = mapper.convertValue(user.getData(), GetDetailUserOutboundResponse.class);
            } catch (Exception e) {
                log.error("Failed to convert user data from Map", e);
                throw new UserNotFoundException("Invalid user data format");
            }
        } else if (user.getData() instanceof GetDetailUserOutboundResponse) {
            userDetail = (GetDetailUserOutboundResponse) user.getData();
        } else {
            log.error("Unexpected data type in user response: {}", user.getData().getClass());
            throw new UserNotFoundException("User not found");
        }

        if (!userDetail.getStatus().equals("ACTIVE")) {
            log.error("User is not active, status: {}", userDetail.getStatus());
            throw new UserNotActiveException("User not active");
        }
    }

    private void checkProductExistAndActive(WebResponse<?> product) {
        // Check if response is null or unsuccessful
        if (product == null || !product.isSuccess()) {
            log.error("Product validation failed - response is null or unsuccessful");
            throw new ProductNotFoundException("Product not found");
        }

        GetDetailProductOutboundResponse productDetail;

        // Handle different data types
        if (product.getData() instanceof String) {
            log.error("Product not found - received error: {}", product.getData());
            throw new ProductNotFoundException("Product not found");
        } else if (product.getData() instanceof java.util.Map) {
            // Jackson deserialized as LinkedHashMap, convert to proper type
            try {
                com.fasterxml.jackson.databind.ObjectMapper mapper = new com.fasterxml.jackson.databind.ObjectMapper();
                productDetail = mapper.convertValue(product.getData(), GetDetailProductOutboundResponse.class);
            } catch (Exception e) {
                log.error("Failed to convert product data from Map", e);
                throw new ProductNotFoundException("Invalid product data format");
            }
        } else if (product.getData() instanceof GetDetailProductOutboundResponse) {
            productDetail = (GetDetailProductOutboundResponse) product.getData();
        } else {
            log.error("Unexpected data type in product response: {}", product.getData().getClass());
            throw new ProductNotFoundException("Product not found");
        }

        if (!productDetail.getActive()) {
            log.error("Product is not active for sku: {}", productDetail.getSku());
            throw new ProductNotActiveException("Product not active");
        }
    }

    private WebResponse<?> getUser(String userId) {
        try {
            return userOutbondService.getUserDetail(userId);
        } catch (Exception e) {
            log.error("Failed to fetch user details for userId: {}, error: {}", userId, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch user details", e);
        }
    }

    private WebResponse<?> getProduct(String sku) {
        try {
            return productOutbondService.getProductDetail(sku);
        } catch (Exception e) {
            log.error("Failed to fetch product details for sku: {}, error: {}", sku, e.getMessage(), e);
            throw new RuntimeException("Failed to fetch product details", e);
        }
    }

}
