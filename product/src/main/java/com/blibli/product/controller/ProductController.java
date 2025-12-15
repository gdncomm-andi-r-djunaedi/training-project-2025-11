package com.blibli.product.controller;

import com.blibli.product.dto.CreateProductRequestDTO;
import com.blibli.product.dto.CreateProductResponseDTO;
import com.blibli.product.response.GdnResponse;
import com.blibli.product.service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class ProductController {
    @Autowired
    ProductService productService;
    @PostMapping("/product/createProduct")
    public ResponseEntity<GdnResponse<List<CreateProductResponseDTO>>> createProduct(@RequestBody List<CreateProductRequestDTO> createProductRequestDTO){
        return new ResponseEntity<>(new GdnResponse(true,null,productService.createProduct(createProductRequestDTO)), HttpStatus.OK);
    }

    @GetMapping("/product/getProductById/{productId}")
    public ResponseEntity<GdnResponse<CreateProductResponseDTO>> getProductProductById(@PathVariable("productId") String productId ){
        return new ResponseEntity<>(new GdnResponse(true,null,productService.findProductById(productId)),HttpStatus.OK);
    }

    @PutMapping("/product/updateProduct")
    public ResponseEntity<GdnResponse<CreateProductResponseDTO>> updateProductData(@RequestBody CreateProductRequestDTO createProductRequestDTO){
        return new ResponseEntity<>(new GdnResponse(true,null,productService.updateProductData(createProductRequestDTO)),HttpStatus.OK);
    }

    @PostMapping("/product/findByListProductId")
    public ResponseEntity<GdnResponse<List<CreateProductResponseDTO>>> findByListProductId(@RequestBody List<String> productIds){
        return new ResponseEntity<>(new GdnResponse(true,null,productService.findProductByIdList(productIds)),HttpStatus.OK);
    }
}
