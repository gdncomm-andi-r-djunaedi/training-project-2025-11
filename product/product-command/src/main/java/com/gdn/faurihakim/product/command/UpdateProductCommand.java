package com.gdn.faurihakim.product.command;

import com.blibli.oss.backend.command.loom.Command;
import com.gdn.faurihakim.product.command.model.UpdateProductCommandRequest;
import com.gdn.faurihakim.product.web.model.response.UpdateProductWebResponse;

public interface UpdateProductCommand extends Command<UpdateProductCommandRequest, UpdateProductWebResponse> {
}
