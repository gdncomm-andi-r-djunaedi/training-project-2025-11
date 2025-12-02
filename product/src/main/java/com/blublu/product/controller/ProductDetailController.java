package com.blublu.product.controller;

import com.blublu.product.document.ProductDetail;
import com.blublu.product.interfaces.ProductDetailService;
import com.blublu.product.model.response.GenericBodyResponse;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Objects;

@RestController
@RequestMapping("/product")
public class ProductDetailController {

  @Autowired
  ProductDetailService productDetailService;

  @RequestMapping(value = "/{sku}/_detail", method = RequestMethod.GET, produces = MediaType.APPLICATION_JSON_VALUE)
  public ResponseEntity<?> findProductDetail(@PathVariable String sku) {
    ProductDetail productDetail = productDetailService.findProductDetailBySku(sku);
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
