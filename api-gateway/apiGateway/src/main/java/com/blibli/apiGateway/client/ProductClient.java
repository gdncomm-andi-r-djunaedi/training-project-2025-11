package com.blibli.apiGateway.client;

import com.blibli.apiGateway.dto.ProductDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.data.domain.Page;
import org.springframework.web.bind.annotation.*;

@FeignClient(name = "productService", url = "${product.service.url}")
public interface ProductClient {

    @GetMapping("/api/product/view/{productId}")
    ProductDTO viewProductById(@PathVariable("productId") String prod);

    @GetMapping("/api/product/search/{searchTerm}")
    Page<ProductDTO> search(@PathVariable String searchTerm,
                            @RequestParam(defaultValue = "0") int page,
                            @RequestParam(defaultValue = "20") int size);

    @GetMapping("/api/product/list")
    Page<ProductDTO> listAllProducts(@RequestParam(defaultValue = "0") int page,
                               @RequestParam(defaultValue = "20") int size);
}
