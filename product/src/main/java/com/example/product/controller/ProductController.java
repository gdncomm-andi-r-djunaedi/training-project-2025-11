package com.example.product.controller;

import com.example.product.dto.ProductListResponse;
import com.example.product.dto.ProductRequest;
import com.example.product.dto.ProductResponse;
import com.example.product.dto.UpdateProductRequest;
import com.example.product.service.ProductService;
import com.example.product.service.ProductServiceImp;
import io.swagger.v3.oas.annotations.tags.Tag;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/products")
@Tag(name = "Product API", description = "APIs for managing products")
public class ProductController {

  @Autowired
  private ProductService productService;

  @PostMapping("/addProducts")
  public ResponseEntity<List<ProductResponse>> addProducts(@RequestBody List<ProductRequest> productRequests) {
    List<ProductResponse> savedProducts = productService.addProducts(productRequests);
    return new ResponseEntity<>(savedProducts, HttpStatus.CREATED);
  }

  @GetMapping("/listing")
  public ResponseEntity<ProductListResponse> getProductsListing(
      @RequestParam(defaultValue = "0") int pageNumber,
      @RequestParam(defaultValue = "10") int pageSize) {
    ProductListResponse response = productService.getProductsListing(pageNumber, pageSize);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @GetMapping("/detail/{itemSku}")
  public ResponseEntity<ProductResponse> getProductDetailByItemSku(@PathVariable String itemSku) {
    ProductResponse response = productService.getProductDetailByItemSku(itemSku);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @GetMapping("/search")
  public ResponseEntity<ProductListResponse> getProductsBySearchTerm(
      @RequestParam String searchTerm, @RequestParam(defaultValue = "0") int pageNumber,
      @RequestParam(defaultValue = "10") int pageSize) {
    ProductListResponse response =
        productService.getProductsBySearchTerm(searchTerm, pageNumber, pageSize);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @PutMapping("/update/{itemSku}")
  public ResponseEntity<ProductResponse> updateProduct(@PathVariable String itemSku, @RequestBody UpdateProductRequest updateRequest) throws Exception{
    ProductResponse response = productService.updateProduct(itemSku, updateRequest);
    return new ResponseEntity<>(response, HttpStatus.OK);
  }

  @DeleteMapping("/deleteProductByItemSku")
  public void deleteProductByItemSku(@RequestParam("itemSku")String itemSku) throws Exception{
    productService.deleteProductByItemSku(itemSku);
  }
}

