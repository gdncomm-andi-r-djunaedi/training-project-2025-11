package com.gdn.product.command.commandInterface;

import com.gdn.product.command.base.Command;
import com.gdn.product.command.model.GetProductCommandRequest;
import com.gdn.product.controller.webmodel.response.GetProductResponse;
import org.springframework.data.domain.Page;

public interface GetProductCommand extends Command<GetProductCommandRequest, Page<GetProductResponse>> {
}
