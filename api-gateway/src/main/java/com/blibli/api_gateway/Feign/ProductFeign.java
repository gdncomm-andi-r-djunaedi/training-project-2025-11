package com.blibli.api_gateway.Feign;

import com.blibli.api_gateway.dto.CreateProductRequestDTO;
import com.blibli.api_gateway.dto.CreateProductResponseDTO;
import com.blibli.api_gateway.dto.SearchRequestDTO;
import com.blibli.api_gateway.dto.SearchResponseDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@FeignClient(name = "product",url = "http://localhost:8082/api")
public interface ProductFeign {
    @PostMapping("/product/createProduct")
    public ResponseEntity<List<CreateProductResponseDTO>> createProduct(@RequestBody List<CreateProductRequestDTO> createProductRequestDTO);
    @GetMapping("/product/getProductById/{productId}")
    public ResponseEntity<CreateProductResponseDTO> getProductProductById(@PathVariable("productId") String productId );
    @PutMapping("/product/updateProduct")
    public ResponseEntity<CreateProductResponseDTO> updateProductData(@RequestBody CreateProductRequestDTO createProductRequestDTO);
    @PostMapping("/search/byProductName")
    public ResponseEntity<Page<SearchResponseDTO>> searchByProductName(@RequestBody SearchRequestDTO searchRequestDTO, PageRequest of) ;
}
