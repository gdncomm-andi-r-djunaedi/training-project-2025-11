package com.blublu.product.controller;

import com.blublu.product.document.Product;
import com.blublu.product.document.ProductDetail;
import com.blublu.product.interfaces.ProductDetailService;
import com.blublu.product.interfaces.ProductService;
import com.blublu.product.model.response.GenericBodyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;

@RestController
@RequestMapping("/product")
public class ProductController {

  @Autowired
  ProductService productService;

  @Autowired
  ProductDetailService productDetailService;

  @RequestMapping(method = RequestMethod.GET)
  public ResponseEntity<?> findAllProduct(@RequestParam(required = false, defaultValue = "0") int page,
      @RequestParam(required = false, defaultValue = "10") int size) {
    List<Product> products = productService.findAllProductWithPageAndSize(page, size);
    if (products.isEmpty()) {
      return ResponseEntity.internalServerError()
          .body(GenericBodyResponse.builder()
              .content(new ArrayList<>())
              .errorMessage("No product found")
              .errorCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
              .success(false)
              .build());
    } else {
      return ResponseEntity.ok().body(GenericBodyResponse.builder().content(products).success(true));
    }
  }

  @RequestMapping(value = "/{productName}", method = RequestMethod.GET)
  public ResponseEntity<?> findProductByName(@PathVariable("productName") String productName,
      @RequestParam(required = false, defaultValue = "0") int page,
      @RequestParam(required = false, defaultValue = "5") int size) {
    List<Product> product = productService.findByName(productName, page, size);
    if (Objects.isNull(product)) {
      return ResponseEntity.internalServerError()
          .body(GenericBodyResponse.builder()
              .errorMessage("No product found")
              .errorCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
              .success(false)
              .content(new ArrayList<>()));
    } else {
      return ResponseEntity.ok()
          .body(GenericBodyResponse.builder().content(Collections.singletonList(product)).success(true));
    }
  }

  @RequestMapping(value = "/{productName}/_detail", method = RequestMethod.GET)
  public ResponseEntity<?> findProductDetail(@PathVariable String productName) {
    ProductDetail productDetail = productDetailService.findProductDetailByName(productName);
    return Objects.isNull(productDetail) ?
        ResponseEntity.internalServerError()
            .body(GenericBodyResponse.builder()
                .errorCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
                .errorMessage("Product detail not found!")
                .success(false)
                .content(new ArrayList<>())
                .build()) :
        ResponseEntity.ok()
            .body(GenericBodyResponse.builder()
                .success(true)
                .content(Collections.singletonList(productDetail))
                .build());
  }
}
