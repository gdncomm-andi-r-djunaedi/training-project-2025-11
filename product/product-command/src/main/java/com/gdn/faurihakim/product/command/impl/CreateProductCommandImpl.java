package com.gdn.faurihakim.product.command.impl;

import com.gdn.faurihakim.Product;
import com.gdn.faurihakim.ProductRepository;
import com.gdn.faurihakim.product.command.CreateProductCommand;
import com.gdn.faurihakim.product.command.model.CreateProductCommandRequest;
import com.gdn.faurihakim.product.web.model.response.CreateProductWebResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.UUID;

@Slf4j
@Service
public class CreateProductCommandImpl implements CreateProductCommand {
    @Autowired
    private ProductRepository productRepository;

    @Override
    public CreateProductWebResponse execute(CreateProductCommandRequest request) {
        Product product = new Product();
        BeanUtils.copyProperties(request, product);
        product.setProductId(UUID.randomUUID().toString());
        Product savedProduct = productRepository.save(product);
        CreateProductWebResponse response = new CreateProductWebResponse();
        BeanUtils.copyProperties(savedProduct, response);
        return response;
    }
}
