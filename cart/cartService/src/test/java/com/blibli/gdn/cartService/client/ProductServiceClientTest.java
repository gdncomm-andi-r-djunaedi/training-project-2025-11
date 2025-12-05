package com.blibli.gdn.cartService.client;

import com.blibli.gdn.cartService.client.dto.ProductDTO;
import com.blibli.gdn.cartService.client.dto.VariantDTO;
import com.blibli.gdn.cartService.exception.ProductNotFoundException;
import com.blibli.gdn.cartService.web.model.GdnResponseData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.function.Function;
import java.util.function.Supplier;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceClientTest {

    @Mock
    private ProductFeignClient productFeignClient;
    
    private ProductServiceClient productServiceClient;

    @BeforeEach
    void setUp() {
        productServiceClient = new ProductServiceClient(productFeignClient);
    }

    @Test
    void getProductBySku_Success() {
        String sku = "SHT-60001-988995";
        String productId = "SHT-60001";
        
        ProductDTO mockProduct = new ProductDTO();
        mockProduct.setProductId(productId);
        mockProduct.setName("Test Product");
        
        GdnResponseData<ProductDTO> mockResponse = new GdnResponseData<>();
        mockResponse.setSuccess(true);
        mockResponse.setData(mockProduct);

        when(productFeignClient.getProductById(productId)).thenReturn(mockResponse);

        ProductDTO result = productServiceClient.getProductBySku(sku);

        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        verify(productFeignClient).getProductById(productId);
    }

    @Test
    void getProductBySku_NotFound() {
        String sku = "SHT-60001-988995";
        String productId = "SHT-60001";
        
        GdnResponseData<ProductDTO> mockResponse = new GdnResponseData<>();
        mockResponse.setSuccess(false);

        when(productFeignClient.getProductById(productId)).thenReturn(mockResponse);

        assertThrows(ProductNotFoundException.class, () -> productServiceClient.getProductBySku(sku));
        verify(productFeignClient).getProductById(productId);
    }

    @Test
    void getVariantBySku_Success() {
        String sku = "SKU-123";
        VariantDTO variant = VariantDTO.builder().sku(sku).build();
        ProductDTO product = ProductDTO.builder()
                .variants(List.of(variant))
                .build();

        
        VariantDTO result = productServiceClient.getVariantBySku(product, sku);

        assertNotNull(result);
        assertEquals(sku, result.getSku());
    }

    @Test
    void getVariantBySku_VariantNotFound() {
        String sku = "SKU-123";
        ProductDTO product = ProductDTO.builder()
                .variants(Collections.emptyList())
                .build();

        assertThrows(ProductNotFoundException.class, () -> productServiceClient.getVariantBySku(product, sku));
    }

    @Test
    void getVariantBySku_VariantsNull_ShouldThrowProductNotFoundException() {
        String sku = "SKU-123";
        ProductDTO product = ProductDTO.builder().build();
        product.setVariants(null); 

        assertThrows(ProductNotFoundException.class, () -> productServiceClient.getVariantBySku(product, sku));
    }
    
    @Test
    void getVariantBySku_ProductDTO_DefaultInitialization() {
        ProductDTO product = ProductDTO.builder().build();
        assertNotNull(product.getVariants());
        assertTrue(product.getVariants().isEmpty());
    }
}
