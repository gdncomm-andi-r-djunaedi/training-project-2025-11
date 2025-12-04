package com.marketplace.cart.command;

import com.marketplace.cart.dto.request.RemoveFromCartRequest;
import com.marketplace.cart.dto.response.CartResponse;
import com.marketplace.common.command.Command;

public interface RemoveFromCartCommand extends Command<RemoveFromCartRequest, CartResponse> {
}
