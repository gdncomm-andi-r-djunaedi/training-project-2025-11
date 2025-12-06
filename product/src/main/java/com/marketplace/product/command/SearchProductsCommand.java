package com.marketplace.product.command;

import com.marketplace.common.command.Command;
import com.marketplace.product.document.Product;
import com.marketplace.product.dto.request.SearchProductsRequest;
import org.springframework.data.domain.Page;

public interface SearchProductsCommand extends Command<SearchProductsRequest, Page<Product>> {
}
