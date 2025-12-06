package com.marketplace.product.command;

import com.marketplace.common.command.Command;
import com.marketplace.product.dto.request.GetProductByIdRequest;
import com.marketplace.product.dto.response.ProductResponse;

public interface GetProductByIdCommand extends Command<GetProductByIdRequest, ProductResponse> {
}
