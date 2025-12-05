package com.blibli.api_gateway.controller;

import com.blibli.api_gateway.dto.CreateProductRequestDTO;
import com.blibli.api_gateway.dto.CreateProductResponseDTO;
import com.blibli.api_gateway.dto.SearchRequestDTO;
import com.blibli.api_gateway.dto.SearchResponseDTO;
import com.blibli.api_gateway.service.GatewayService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api")
public class GatewayProductControler {
    @Autowired
    GatewayService gatewayService;

    @PostMapping("/product/createProduct")
    public ResponseEntity<List<CreateProductResponseDTO>> createProduct(@RequestBody List<CreateProductRequestDTO> createProductRequestDTO){
        return new ResponseEntity<>(gatewayService.createProduct(createProductRequestDTO), HttpStatus.OK);
    }

    @GetMapping("/product/getProductById/{productId}")
    public ResponseEntity<CreateProductResponseDTO> getProductProductById(@PathVariable("productId") String productId ){
        return new ResponseEntity<>(gatewayService.findProductById(productId),HttpStatus.OK);
    }

    @PutMapping("/product/updateProduct")
    public ResponseEntity<CreateProductResponseDTO> updateProductData(@RequestBody CreateProductRequestDTO createProductRequestDTO){
        return new ResponseEntity<>(gatewayService.updateProductData(createProductRequestDTO),HttpStatus.OK);
    }

    @PostMapping("/search/byProductName")
    public ResponseEntity<Page<SearchResponseDTO>> searchByProductName(@RequestBody SearchRequestDTO searchRequestDTO) {
        return new ResponseEntity<>(gatewayService.search(searchRequestDTO, PageRequest.of(searchRequestDTO.getPageNo(), searchRequestDTO.getSize(), Sort.Direction.valueOf(searchRequestDTO.getSort()),searchRequestDTO.getSearchKeyword())), HttpStatus.OK);
    }

}
