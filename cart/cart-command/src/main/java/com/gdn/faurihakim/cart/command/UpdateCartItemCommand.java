package com.gdn.faurihakim.cart.command;

import com.blibli.oss.backend.command.loom.Command;
import com.gdn.faurihakim.cart.command.model.UpdateCartItemCommandRequest;
import com.gdn.faurihakim.cart.web.model.response.UpdateCartItemWebResponse;

public interface UpdateCartItemCommand extends Command<UpdateCartItemCommandRequest, UpdateCartItemWebResponse> {
}
