package com.gdn.faurihakim.product.command;

import com.blibli.oss.backend.command.loom.Command;
import com.gdn.faurihakim.product.command.model.GetProductCommandRequest;
import com.gdn.faurihakim.product.web.model.response.GetProductWebResponse;

public interface GetProductCommand extends Command<GetProductCommandRequest, GetProductWebResponse> {
}
