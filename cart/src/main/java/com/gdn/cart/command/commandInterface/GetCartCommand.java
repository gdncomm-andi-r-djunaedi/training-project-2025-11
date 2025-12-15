package com.gdn.cart.command.commandInterface;

import com.gdn.cart.command.base.Command;
import com.gdn.cart.command.model.GetCartCommandRequest;
import com.gdn.cart.controller.webmodel.response.CartResponse;

public interface GetCartCommand extends Command<GetCartCommandRequest, CartResponse> {
}

