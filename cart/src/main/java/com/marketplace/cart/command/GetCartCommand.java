package com.marketplace.cart.command;

import com.marketplace.cart.dto.request.GetCartRequest;
import com.marketplace.cart.dto.response.CartResponse;
import com.marketplace.common.command.Command;

public interface GetCartCommand extends Command<GetCartRequest, CartResponse> {
}
