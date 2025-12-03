package com.blibli.gdn.cartService.client;

import com.blibli.gdn.cartService.client.dto.ProductDTO;
import com.blibli.gdn.cartService.client.dto.VariantDTO;
import com.blibli.gdn.cartService.exception.ProductNotFoundException;
import com.blibli.gdn.cartService.exception.ProductServiceUnavailableException;
import com.blibli.gdn.cartService.web.model.GdnResponseData;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.cloud.client.circuitbreaker.CircuitBreakerFactory;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProductServiceClient {

    private final ProductFeignClient productFeignClient;
    private final CircuitBreakerFactory circuitBreakerFactory;

    @Cacheable(value = "products", key = "#sku")
    public ProductDTO getProductBySku(String sku) {
        log.info("Fetching product from Product Service for SKU: {}", sku);

        return circuitBreakerFactory.create("productService").run(() -> {
            try {
                GdnResponseData<ProductDTO> response = productFeignClient.getProductBySku(sku);

                if (response != null) {
                    log.info("Product Service response: success={}, message={}, data={}",
                            response.isSuccess(), response.getMessage(), response.getData());
                } else {
                    log.warn("Product Service response is null");
                }

                if (response != null && response.isSuccess() && response.getData() != null) {
                    return response.getData();
                }

                log.error("Product Service returned unsuccessful response or null data for SKU: {}", sku);
                throw new ProductNotFoundException(sku);

            } catch (ProductNotFoundException e) {
                log.error("Product not found for SKU: {}", sku);
                throw e;
            } catch (Exception e) {
                log.error("Error calling Product Service for SKU: {}", sku, e);
                throw new RuntimeException(e);
            }
        }, throwable -> {
            log.error("Error fetching product from Product Service: {}", throwable.getMessage());
            if (throwable instanceof ProductNotFoundException) {
                throw (ProductNotFoundException) throwable;
            }
            throw new ProductServiceUnavailableException("Product Service is unavailable");
        });
    }

    public VariantDTO getVariantBySku(ProductDTO product, String sku) {
        if (product.getVariants() == null) {
            throw new ProductNotFoundException(sku);
        }
        return product.getVariants().stream()
                .filter(v -> v.getSku().equals(sku))
                .findFirst()
                .orElseThrow(() -> new ProductNotFoundException(sku));
    }
}
