package com.gdn.faurihakim.product.command;

import com.blibli.oss.backend.command.loom.Command;
import com.gdn.faurihakim.product.command.model.CreateProductCommandRequest;
import com.gdn.faurihakim.product.web.model.response.CreateProductWebResponse;

public interface CreateProductCommand extends Command<CreateProductCommandRequest, CreateProductWebResponse> {
}
