package com.gdn.product.command;

import com.gdn.product.command.commandInterface.GetProductByIdCommand;
import com.gdn.product.command.model.GetProductByIdCommandRequest;
import com.gdn.product.controller.webmodel.response.GetProductResponse;
import com.gdn.product.exception.DataNotFoundException;
import com.gdn.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class GetProductByIdCommandImpl implements GetProductByIdCommand {

    private final ProductRepository productRepository;

    @Override
    public GetProductResponse execute(GetProductByIdCommandRequest commandRequest) {
        return productRepository.findById(commandRequest.getId())
                .map(product -> GetProductResponse.builder()
                        .id(product.getId())
                        .name(product.getName())
                        .price(product.getPrice())
                        .stock(product.getStock())
                        .build())
                .orElseThrow(DataNotFoundException::new);
    }
}
