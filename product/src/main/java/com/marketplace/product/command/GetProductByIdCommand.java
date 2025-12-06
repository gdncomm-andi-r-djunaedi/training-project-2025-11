package com.marketplace.product.command;

import com.marketplace.common.command.Command;
import com.marketplace.product.document.Product;
import com.marketplace.product.dto.request.GetProductByIdRequest;

public interface GetProductByIdCommand extends Command<GetProductByIdRequest, Product> {
}
