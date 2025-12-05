package com.gdn.product.controller;

import com.gdn.product.command.commandInterface.GetProductByIdCommand;
import com.gdn.product.command.commandInterface.GetProductCommand;
import com.gdn.product.command.model.GetProductByIdCommandRequest;
import com.gdn.product.command.model.GetProductCommandRequest;
import com.gdn.product.controller.base.BaseCommandController;
import com.gdn.product.controller.webmodel.response.GetProductResponse;
import com.gdn.product.entity.Product;
import com.gdn.product.repository.ProductRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Slf4j
public class ProductController extends BaseCommandController {

  @GetMapping
  public ResponseEntity<Page<GetProductResponse>> getAllProducts(
      @RequestParam(value = "name", required = false, defaultValue = "") String name,
      @RequestParam(value = "page", defaultValue = "0") Integer page,
      @RequestParam(value = "size", defaultValue = "10") Integer size) {

      return ResponseEntity.ok(executor.execute(
              GetProductCommand.class,
              GetProductCommandRequest.builder()
                      .productName(name)
                      .page(page)
                      .size(size)
                      .build()));
  }

  @GetMapping("/{id}")
  public ResponseEntity<GetProductResponse> getProductById(@PathVariable("id") String id) {

      return ResponseEntity.ok(executor.execute(
              GetProductByIdCommand.class,
              GetProductByIdCommandRequest.builder()
                      .id(id)
                      .build()
      ));
  }

}

