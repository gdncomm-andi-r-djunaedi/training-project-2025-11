package com.gdn.product.command.commandInterface;

import com.gdn.product.command.base.Command;
import com.gdn.product.command.model.GetProductByIdCommandRequest;
import com.gdn.product.command.model.GetProductCommandRequest;
import com.gdn.product.controller.webmodel.response.GetProductResponse;

public interface GetProductByIdCommand extends Command<GetProductByIdCommandRequest, GetProductResponse> {
}
