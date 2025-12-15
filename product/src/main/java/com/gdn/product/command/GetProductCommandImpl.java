package com.gdn.product.command;

import com.gdn.product.command.commandInterface.GetProductCommand;
import com.gdn.product.command.model.GetProductCommandRequest;
import com.gdn.product.controller.webmodel.response.GetProductResponse;
import com.gdn.product.entity.Product;
import com.gdn.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class GetProductCommandImpl implements GetProductCommand {
    private final ProductRepository productRepository;
    @Override
    public Page<GetProductResponse> execute(GetProductCommandRequest request) {
        Pageable pageable = PageRequest.of(request.getPage(), request.getSize());
        Page<Product> productList;
        if(request.getProductName().isBlank() || request.getProductName().trim().equals(""))
        {
            productList = productRepository.findAll(pageable);
        }
        else {
            productList = productRepository.findByNameContainingIgnoreCase(request.getProductName(), pageable);
        }
        return productList
                .map(product -> GetProductResponse.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .price(product.getPrice())
                        .stock(product.getStock())
                        .build());
    }
}
