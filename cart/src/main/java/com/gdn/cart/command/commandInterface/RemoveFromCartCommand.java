package com.gdn.cart.command.commandInterface;

import com.gdn.cart.command.base.Command;
import com.gdn.cart.command.model.RemoveFromCartCommandRequest;
import com.gdn.cart.controller.webmodel.response.CartResponse;

public interface RemoveFromCartCommand extends Command<RemoveFromCartCommandRequest, CartResponse> {
}

