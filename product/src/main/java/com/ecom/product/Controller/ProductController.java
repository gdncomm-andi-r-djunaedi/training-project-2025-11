package com.ecom.product.Controller;

import com.ecom.product.Dto.ApiResponse;
import com.ecom.product.Dto.ProductDto;
import com.ecom.product.Service.ProductService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/products")
public class ProductController {

    @Autowired
    ProductService productService;

    @GetMapping("/getAll")
    public ResponseEntity<ApiResponse<Page<ProductDto>>> getAllProducts(@RequestParam("name") String name, @RequestParam(defaultValue = "0") int page, @RequestParam(defaultValue = "10") int size){
        Page<ProductDto> product = productService.findAllByName(name,page,size);
        return ResponseEntity.ok(ApiResponse.success(200,product));
    }


    @GetMapping("/get/{sku}")
    public ResponseEntity<ApiResponse<ProductDto>> findBySku(@PathVariable String sku){
        return ResponseEntity.ok(ApiResponse.success(200, productService.findBySku(sku)));
    }

}
