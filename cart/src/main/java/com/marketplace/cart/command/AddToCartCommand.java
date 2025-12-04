package com.marketplace.cart.command;

import com.marketplace.cart.dto.request.AddToCartCommandRequest;
import com.marketplace.cart.dto.response.CartResponse;
import com.marketplace.common.command.Command;

public interface AddToCartCommand extends Command<AddToCartCommandRequest, CartResponse> {
}
