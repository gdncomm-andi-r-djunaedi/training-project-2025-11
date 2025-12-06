package com.training.marketplace.gateway.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.training.marketplace.gateway.client.ProductClientImpl;
import com.training.marketplace.product.controller.modal.request.GetProductDetailRequest;
import com.training.marketplace.product.controller.modal.request.GetProductDetailResponse;
import com.training.marketplace.product.controller.modal.request.GetProductListRequest;
import com.training.marketplace.product.controller.modal.request.GetProductListResponse;

@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductClientImpl productClient;

    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/getProductDetail")
    public GetProductDetailResponse getProductDetail(@RequestBody GetProductDetailRequest request){
        return productClient.getProductDetail(request);
    }

    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/getProductList")
    public GetProductListResponse getProductList(@RequestBody GetProductListRequest request){
        return productClient.getProductList(request);
    }
}
