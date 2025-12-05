package com.training.cartService.cartmongo.service;

import com.training.cartService.cartmongo.dto.ProductDTO;
import com.training.cartService.cartmongo.exception.CartException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.client.HttpClientErrorException;

@Service
public class ProductService {

    private final RestTemplate restTemplate;

    @Value("${product.service.url:http://localhost:8083/product/getProduct}")
    private String productServiceUrl;

    public ProductService(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ProductDTO getProductBySku(String sku) {
        try {
            String url = productServiceUrl + "/" + sku;
            ProductDTO product = restTemplate.getForObject(url, ProductDTO.class);
            if (product == null) {
                throw new CartException.ProductNotFoundException(sku);
            }
            return product;
        } catch (HttpClientErrorException.NotFound e) {
            throw new CartException.ProductNotFoundException(sku);
        } catch (Exception e) {
            throw new RuntimeException("Error fetching product details: " + e.getMessage(), e);
        }
    }
}
