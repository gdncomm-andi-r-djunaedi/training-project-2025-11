package com.gdn.faurihakim.cart.command.impl;

import com.gdn.faurihakim.Cart;
import com.gdn.faurihakim.CartRepository;
import com.gdn.faurihakim.cart.command.GetMemberCartCommand;
import com.gdn.faurihakim.cart.command.model.GetMemberCartCommandRequest;
import com.gdn.faurihakim.cart.web.model.CartNotFoundException;
import com.gdn.faurihakim.cart.web.model.response.CartItemResponse;
import com.gdn.faurihakim.cart.web.model.response.GetMemberCartWebResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
public class GetMemberCartCommandImpl implements GetMemberCartCommand {
        @Autowired
        private CartRepository cartRepository;

        @Override
        public GetMemberCartWebResponse execute(GetMemberCartCommandRequest request) {
                Cart cart = cartRepository.findByMemberId(request.getMemberId())
                                .orElseThrow(() -> new CartNotFoundException(
                                                "Cart not found for member: " + request.getMemberId()));

                List<CartItemResponse> itemResponses = cart.getItems().stream()
                                .map(item -> CartItemResponse.builder()
                                                .productId(item.getProductId())
                                                .productName(item.getProductName())
                                                .price(item.getPrice())
                                                .quantity(item.getQuantity())
                                                .subtotal(item.getSubtotal())
                                                .build())
                                .collect(Collectors.toList());

                return GetMemberCartWebResponse.builder()
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
