package com.gdn.faurihakim.cart.command;

import com.blibli.oss.backend.command.loom.Command;
import com.gdn.faurihakim.cart.command.model.AddProductToCartCommandRequest;
import com.gdn.faurihakim.cart.web.model.response.AddProductToCartWebResponse;

public interface AddProductToCartCommand extends Command<AddProductToCartCommandRequest, AddProductToCartWebResponse> {
}
