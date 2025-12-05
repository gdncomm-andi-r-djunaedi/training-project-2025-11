package com.gdn.faurihakim.cart.command;

import com.blibli.oss.backend.command.loom.Command;
import com.gdn.faurihakim.cart.command.model.DeleteCartCommandRequest;
import com.gdn.faurihakim.cart.web.model.response.DeleteCartWebResponse;

public interface DeleteCartCommand extends Command<DeleteCartCommandRequest, DeleteCartWebResponse> {
}
