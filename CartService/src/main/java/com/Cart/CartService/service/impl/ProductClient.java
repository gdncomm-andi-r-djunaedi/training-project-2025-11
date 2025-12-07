package com.Cart.CartService.service.impl;

import com.Cart.CartService.dto.ProductResponseDTO;
import com.Cart.CartService.exception.ProductServiceExceptions;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Component;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

@Component
public class ProductClient {
    private final RestTemplate restTemplate;
    private final String productBaseUrl = "http://localhost:8091/products";

    @Autowired
    public ProductClient(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    public ProductResponseDTO getProductById(String productId) {
        try {
            String url = productBaseUrl + "/getProductById/" + productId;
            return restTemplate.getForObject(url, ProductResponseDTO.class);
        } catch (HttpClientErrorException.NotFound ex) {
            throw new ProductServiceExceptions("Product not found with id: " + productId, HttpStatus.NOT_FOUND);
        } catch (HttpClientErrorException ex) {
            throw new ProductServiceExceptions("Failed to fetch product: " + ex.getStatusCode(), HttpStatus.BAD_GATEWAY);
        } catch (Exception ex) {
            throw new ProductServiceExceptions("Error calling Product Service: " + ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
        }
    }
}
