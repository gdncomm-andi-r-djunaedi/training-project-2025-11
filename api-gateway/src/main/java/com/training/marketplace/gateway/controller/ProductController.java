package com.training.marketplace.gateway.controller;

import com.training.marketplace.gateway.service.ProductClientService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.training.marketplace.product.controller.modal.request.GetProductDetailRequest;
import com.training.marketplace.product.controller.modal.request.GetProductDetailResponse;
import com.training.marketplace.product.controller.modal.request.GetProductListRequest;
import com.training.marketplace.product.controller.modal.request.GetProductListResponse;

import com.training.marketplace.gateway.dto.product.GetProductDetailResponseDTO;
import com.training.marketplace.gateway.dto.product.ProductDTO;
import com.training.marketplace.gateway.dto.product.GetProductDetailRequestDTO;

import java.util.stream.Collectors;

import com.training.marketplace.gateway.dto.product.ProductListItemDTO;
import com.training.marketplace.gateway.dto.product.GetProductListRequestDTO;
import com.training.marketplace.gateway.dto.product.GetProductListResponseDTO;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@RestController
@RequestMapping("/api/product")
public class ProductController {

    @Autowired
    private ProductClientService productClient;

    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/getProductDetail")
    public GetProductDetailResponseDTO getProductDetail(@RequestBody GetProductDetailRequestDTO request){
        log.info(String.format("getting product detail for %s", request.getProductId()));
        GetProductDetailResponse response = productClient.getProductDetail(GetProductDetailRequest.newBuilder()
        .setProductId(request.getProductId())
        .build());
        return GetProductDetailResponseDTO.builder()
        .product(ProductDTO.builder()
        .productId(response.getProduct().getProductId())
        .productName(response.getProduct().getProductName())
        .productPrice(response.getProduct().getProductPrice())
        .productDetail(response.getProduct().getProductDetail())
        .productNotes(response.getProduct().getProductNotes())
        .productImage(response.getProduct().getProductImage())
        .build())
        .build();
    }

    @GetMapping(consumes = MediaType.APPLICATION_JSON_VALUE, path = "/getProductList")
    public GetProductListResponseDTO getProductList(@RequestBody GetProductListRequestDTO request){
        log.info(String.format("getting product list with query %s, page %s, and item per page %s", request.getQuery(), request.getPage(), request.getItemPerPage()));
        GetProductListResponse response = productClient.getProductList(GetProductListRequest.newBuilder()
        .setQuery(request.getQuery())
        .setPage(request.getPage())
        .setItemPerPage(request.getItemPerPage())
        .build());
        return GetProductListResponseDTO.builder()
        .productList(response.getProductListList().stream().map(product -> ProductListItemDTO.builder()
        .productId(product.getProductId())
        .productName(product.getProductName())
        .productPrice(product.getProductPrice())
        .productImage(product.getProductImage())
        .build()).collect(Collectors.toList()))
        .page(response.getPage())
        .itemPerPage(response.getItemPerPage())
        .build();
    }
}
