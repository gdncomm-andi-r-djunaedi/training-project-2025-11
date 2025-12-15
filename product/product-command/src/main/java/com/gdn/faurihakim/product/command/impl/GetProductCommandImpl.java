package com.gdn.faurihakim.product.command.impl;

import com.gdn.faurihakim.Product;
import com.gdn.faurihakim.ProductRepository;
import com.gdn.faurihakim.product.command.GetProductCommand;
import com.gdn.faurihakim.product.command.model.GetProductCommandRequest;
import com.gdn.faurihakim.product.web.model.ProductNotFoundException;
import com.gdn.faurihakim.product.web.model.response.GetProductWebResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class GetProductCommandImpl implements GetProductCommand {
    @Autowired
    private ProductRepository productRepository;

    @Override
    public GetProductWebResponse execute(GetProductCommandRequest request) {
        Product product = productRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + request.getProductId()));

        GetProductWebResponse response = new GetProductWebResponse();
        BeanUtils.copyProperties(product, response);
        return response;
    }
}
