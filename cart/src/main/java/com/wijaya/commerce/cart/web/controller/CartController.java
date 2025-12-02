package com.wijaya.commerce.cart.web.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;

import com.wijaya.commerce.cart.command.AddToCartCommand;
import com.wijaya.commerce.cart.commandImpl.model.AddToCartCommandRequest;
import com.wijaya.commerce.cart.commandImpl.model.AddToCartCommandResponse;
import com.wijaya.commerce.cart.constant.CartApiPath;
import com.wijaya.commerce.cart.restWebModel.request.AddToCartRequestWebModel;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;

@RestController
@RequiredArgsConstructor
public class CartController {
    private final AddToCartCommand addToCartCommand;

    @PostMapping(CartApiPath.ADD_TO_CART)
    public AddToCartCommandResponse addToCart(@Valid @RequestBody AddToCartRequestWebModel request) {
        AddToCartCommandRequest commandRequest = AddToCartCommandRequest.builder()
                .userId(request.getUserId())
                .cartId(request.getCartId())
                .productSku(request.getProductSku())
                .quantity(request.getQuantity())
                .build();
        AddToCartCommandResponse response = addToCartCommand.doCommand(commandRequest);
        return response;
    }
}
