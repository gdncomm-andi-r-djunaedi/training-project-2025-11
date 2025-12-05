package com.gdn.training.product;

import com.gdn.training.product.dto.SearchProductRequest;
import com.gdn.training.product.entity.Product;
import com.gdn.training.product.repository.ProductRepository;
import com.gdn.training.product.service.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class SearchProductTest {

    @Mock
    private ProductRepository productRepository;

    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productRepository);
    }

    @Test
    @DisplayName("search product with filter and pagination")
    void searchProductTest() {
        SearchProductRequest request = new SearchProductRequest();
        request.setProductName("Test");
        request.setPage(0);
        request.setSize(10);

        Product product = new Product("SKU-000001", "Test Product", 10000.0, "Description");
        Page<Product> productPage = new PageImpl<>(Collections.singletonList(product));

        when(productRepository.searchByName(any(String.class), any(Pageable.class))).thenReturn(productPage);

        Page<Product> result = productService.searchProduct(request);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("SKU-000001", result.getContent().get(0).getProduct_id());

        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).searchByName(nameCaptor.capture(), pageableCaptor.capture());

        assertEquals("Test", nameCaptor.getValue());
        Pageable capturedPageable = pageableCaptor.getValue();
        assertEquals(0, capturedPageable.getPageNumber());
        assertEquals(10, capturedPageable.getPageSize());
    }

    @Test
    @DisplayName("search product with invalid name should return empty result")
    void searchProductWithInvalidNameTest() {
        SearchProductRequest request = new SearchProductRequest();
        request.setProductName("InvalidName");
        request.setPage(0);
        request.setSize(10);

        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList());

        when(productRepository.searchByName(any(String.class), any(Pageable.class))).thenReturn(emptyPage);

        Page<Product> result = productService.searchProduct(request);

        assertNotNull(result);
        assertEquals(0, result.getTotalElements());
        assertEquals(0, result.getContent().size());

        ArgumentCaptor<String> nameCaptor = ArgumentCaptor.forClass(String.class);
        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).searchByName(nameCaptor.capture(), pageableCaptor.capture());

        assertEquals("InvalidName", nameCaptor.getValue());
        Pageable capturedPageable = pageableCaptor.getValue();
        assertEquals(0, capturedPageable.getPageNumber());
        assertEquals(10, capturedPageable.getPageSize());
    }
}
