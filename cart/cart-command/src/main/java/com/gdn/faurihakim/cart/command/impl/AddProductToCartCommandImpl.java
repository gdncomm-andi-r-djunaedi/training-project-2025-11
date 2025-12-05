package com.gdn.faurihakim.cart.command.impl;

import com.blibli.oss.backend.common.model.response.Response;
import com.gdn.faurihakim.Cart;
import com.gdn.faurihakim.CartItem;
import com.gdn.faurihakim.CartRepository;
import com.gdn.faurihakim.cart.client.MemberServiceClient;
import com.gdn.faurihakim.cart.client.ProductServiceClient;
import com.gdn.faurihakim.cart.client.model.GetMemberResponse;
import com.gdn.faurihakim.cart.client.model.GetProductResponse;
import com.gdn.faurihakim.cart.command.AddProductToCartCommand;
import com.gdn.faurihakim.cart.command.model.AddProductToCartCommandRequest;
import com.gdn.faurihakim.cart.web.model.MemberNotFoundException;
import com.gdn.faurihakim.cart.web.model.ProductNotFoundException;
import com.gdn.faurihakim.cart.web.model.response.AddProductToCartWebResponse;
import com.gdn.faurihakim.cart.web.model.response.CartItemResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Slf4j
@Service
public class AddProductToCartCommandImpl implements AddProductToCartCommand {
        @Autowired
        private CartRepository cartRepository;

        @Autowired
        private MemberServiceClient memberServiceClient;

        @Autowired
        private ProductServiceClient productServiceClient;

        @Override
        @Transactional
        public AddProductToCartWebResponse execute(AddProductToCartCommandRequest request) {
                // Validate member exists by calling member service
                log.info("Validating member with memberId: {}", request.getMemberId());
                try {
                        Response<GetMemberResponse> memberResponse = memberServiceClient
                                        .getMember(request.getMemberId());
                        if (memberResponse == null || memberResponse.getData() == null) {
                                throw new MemberNotFoundException("Member not found: " + request.getMemberId());
                        }
                        log.info("Member validated: {}", memberResponse.getData().getEmail());
                } catch (MemberNotFoundException e) {
                        throw e;
                } catch (Exception e) {
                        log.error("Failed to validate member: {}", e.getMessage());
                        throw new MemberNotFoundException("Failed to validate member: " + request.getMemberId(), e);
                }

                // Find or create cart for member
                Cart cart = cartRepository.findByMemberId(request.getMemberId())
                                .orElseGet(() -> {
                                        Cart newCart = Cart.builder()
                                                        .cartId(UUID.randomUUID().toString())
                                                        .memberId(request.getMemberId())
                                                        .totalAmount(0.0)
                                                        .build();
                                        return newCart;
                                });

                // Process each product in the list
                for (AddProductToCartCommandRequest.ProductItem productItem : request.getProducts()) {
                        // Fetch product details from Product Service
                        log.info("Fetching product details for productId: {}", productItem.getProductId());
                        String productName;
                        Double price;

                        try {
                                Response<GetProductResponse> productResponse = productServiceClient
                                                .getProduct(productItem.getProductId());
                                if (productResponse == null || productResponse.getData() == null) {
                                        throw new ProductNotFoundException(
                                                        "Product not found: " + productItem.getProductId());
                                }
                                productName = productResponse.getData().getProductName();
                                price = productResponse.getData().getPrice();
                        } catch (ProductNotFoundException e) {
                                throw e;
                        } catch (Exception e) {
                                log.error("Failed to fetch product: {}", e.getMessage());
                                throw new ProductNotFoundException(
                                                "Failed to fetch product: " + productItem.getProductId(), e);
                        }

                        // Create cart item
                        CartItem cartItem = CartItem.builder()
                                        .productId(productItem.getProductId())
                                        .productName(productName)
                                        .price(price)
                                        .quantity(productItem.getQuantity())
                                        .build();
                        cartItem.calculateSubtotal();

                        // Add or update item in cart
                        cart.addOrUpdateItem(cartItem);
                }

                // Save cart (cascade saves items)
                Cart savedCart = cartRepository.save(cart);

                // Build response
                return buildResponse(savedCart);
        }

        private AddProductToCartWebResponse buildResponse(Cart cart) {
                List<CartItemResponse> itemResponses = cart.getItems().stream()
                                .map(item -> CartItemResponse.builder()
                                                .productId(item.getProductId())
                                                .productName(item.getProductName())
                                                .price(item.getPrice())
                                                .quantity(item.getQuantity())
                                                .subtotal(item.getSubtotal())
                                                .build())
                                .collect(Collectors.toList());

                return AddProductToCartWebResponse.builder()
                                .cartId(cart.getCartId())
                                .memberId(cart.getMemberId())
                                .totalAmount(cart.getTotalAmount())
                                .items(itemResponses)
                                .version(cart.getVersion())
                                .createdDate(cart.getCreatedDate())
                                .createdBy(cart.getCreatedBy())
                                .lastModifiedDate(cart.getLastModifiedDate())
                                .lastModifiedBy(cart.getLastModifiedBy())
                                .build();
        }
}
