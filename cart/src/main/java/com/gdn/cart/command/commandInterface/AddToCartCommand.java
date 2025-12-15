package com.gdn.cart.command.commandInterface;

import com.gdn.cart.command.base.Command;
import com.gdn.cart.command.model.AddToCartCommandRequest;
import com.gdn.cart.controller.webmodel.response.CartResponse;

public interface AddToCartCommand extends Command<AddToCartCommandRequest, CartResponse> {
}

