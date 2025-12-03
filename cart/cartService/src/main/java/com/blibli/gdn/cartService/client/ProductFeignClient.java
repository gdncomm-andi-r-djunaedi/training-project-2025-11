package com.blibli.gdn.cartService.client;

import com.blibli.gdn.cartService.client.dto.ProductDTO;
import com.blibli.gdn.cartService.web.model.GdnResponseData;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

@FeignClient(name = "productService", url = "${product.service.url}")
public interface ProductFeignClient {

    @GetMapping("/api/v1/internal/products/sku/{sku}")
    GdnResponseData<ProductDTO> getProductBySku(@PathVariable("sku") String sku);
}
