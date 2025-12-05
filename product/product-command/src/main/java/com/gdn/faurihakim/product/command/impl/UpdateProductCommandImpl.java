package com.gdn.faurihakim.product.command.impl;

import com.gdn.faurihakim.Product;
import com.gdn.faurihakim.ProductRepository;
import com.gdn.faurihakim.product.command.UpdateProductCommand;
import com.gdn.faurihakim.product.command.model.UpdateProductCommandRequest;
import com.gdn.faurihakim.product.web.model.ProductNotFoundException;
import com.gdn.faurihakim.product.web.model.response.UpdateProductWebResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

@Slf4j
@Service
public class UpdateProductCommandImpl implements UpdateProductCommand {
    @Autowired
    private ProductRepository productRepository;

    @Override
    public UpdateProductWebResponse execute(UpdateProductCommandRequest request) {
        Product product = productRepository.findByProductId(request.getProductId())
                .orElseThrow(() -> new ProductNotFoundException("Product not found: " + request.getProductId()));
        if (product != null) {
            BeanUtils.copyProperties(request, product);
            productRepository.save(product);
            UpdateProductWebResponse response = new UpdateProductWebResponse();
            BeanUtils.copyProperties(product, response);
            return response;
        } else {
            return null;
        }
    }
}
