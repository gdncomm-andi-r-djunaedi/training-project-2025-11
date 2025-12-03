package com.blublu.product.controller;

import com.blublu.product.document.ProductDetail;
import com.blublu.product.document.Products;
import com.blublu.product.interfaces.ProductDetailService;
import com.blublu.product.interfaces.ProductService;
import com.blublu.product.model.response.GenericBodyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
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

  @RequestMapping(value = "/all", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> findAllProduct(@RequestParam(required = false, defaultValue = "0") int page,
      @RequestParam(required = false, defaultValue = "10") int size) {
    List<Products> products = productService.findAllProductWithPageAndSize(page, size);
    System.out.println(products);
    if (products.isEmpty()) {
      return ResponseEntity.internalServerError()
          .body(GenericBodyResponse.builder()
              .content(new ArrayList<>())
              .errorMessage("No product found")
              .errorCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
              .success(false)
              .build());
    } else {
      return ResponseEntity.ok().body(GenericBodyResponse.builder().content(products).success(true).build());
    }
  }

  @RequestMapping(method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> findProductByName(@RequestParam("productName") String productName,
      @RequestParam(required = false, defaultValue = "0") int page,
      @RequestParam(required = false, defaultValue = "5") int size) {
    List<Products> products = productService.findByName(productName, page, size);
    if (Objects.isNull(products)) {
      return ResponseEntity.internalServerError()
          .body(GenericBodyResponse.builder()
              .errorMessage("No product found")
              .errorCode(HttpStatus.INTERNAL_SERVER_ERROR.value())
              .success(false)
              .content(new ArrayList<>())
              .build());
    } else {
      return ResponseEntity.ok()
          .body(GenericBodyResponse.builder().content(Collections.singletonList(products)).success(true).build());
    }
  }

  @RequestMapping(value = "/{skuCode}/_detail", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> findProductDetail(@PathVariable String skuCode) {
    ProductDetail productDetail = productDetailService.findProductDetailBySku(skuCode);
    System.out.println(productDetail);
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
