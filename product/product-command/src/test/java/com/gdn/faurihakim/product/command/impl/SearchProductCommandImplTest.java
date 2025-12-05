package com.gdn.faurihakim.product.command.impl;

import com.gdn.faurihakim.Product;
import com.gdn.faurihakim.ProductRepository;
import com.gdn.faurihakim.product.command.model.SearchProductCommandRequest;
import com.gdn.faurihakim.product.web.model.response.SearchProductWebResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("SearchProductCommandImpl Happy Path Tests")
class SearchProductCommandImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private SearchProductCommandImpl searchProductCommand;

    private Product product1;
    private Product product2;

    @BeforeEach
    void setUp() {
        product1 = new Product();
        product1.setProductId("prod-1");
        product1.setProductName("Test Product 1");
        product1.setPrice(100.0);

        product2 = new Product();
        product2.setProductId("prod-2");
        product2.setProductName("Test Product 2");
        product2.setPrice(200.0);
    }

    @Test
    @DisplayName("Should successfully search products with query")
    void testExecute_WithQuery_ReturnsProducts() {
        // Arrange
        SearchProductCommandRequest request = new SearchProductCommandRequest();
        request.setQuery("Test");
        request.setPage(0);
        request.setSize(10);

        List<Product> products = Arrays.asList(product1, product2);
        Page<Product> productPage = new PageImpl<>(products, PageRequest.of(0, 10), 2);

        when(productRepository.findByProductNameContainingIgnoreCase(anyString(), any(Pageable.class)))
                .thenReturn(productPage);

        // Act
        SearchProductWebResponse response = searchProductCommand.execute(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getProducts()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getTotalPages()).isEqualTo(1);

        verify(productRepository).findByProductNameContainingIgnoreCase("Test", PageRequest.of(0, 10));
    }

    @Test
    @DisplayName("Should successfully get all products without query")
    void testExecute_WithoutQuery_ReturnsAllProducts() {
        // Arrange
        SearchProductCommandRequest request = new SearchProductCommandRequest();
        request.setPage(0);
        request.setSize(10);

        List<Product> products = Arrays.asList(product1, product2);
        Page<Product> productPage = new PageImpl<>(products, PageRequest.of(0, 10), 2);

        when(productRepository.findAll(any(Pageable.class))).thenReturn(productPage);

        // Act
        SearchProductWebResponse response = searchProductCommand.execute(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getProducts()).hasSize(2);
        assertThat(response.getTotalElements()).isEqualTo(2);

        verify(productRepository).findAll(PageRequest.of(0, 10));
        verify(productRepository, never()).findByProductNameContainingIgnoreCase(anyString(), any());
    }

    @Test
    @DisplayName("Should return empty list when no products match query")
    void testExecute_NoMatches_ReturnsEmptyList() {
        // Arrange
        SearchProductCommandRequest request = new SearchProductCommandRequest();
        request.setQuery("NonExistent");
        request.setPage(0);
        request.setSize(10);

        Page<Product> emptyPage = new PageImpl<>(Collections.emptyList(), PageRequest.of(0, 10), 0);

        when(productRepository.findByProductNameContainingIgnoreCase(anyString(), any(Pageable.class)))
                .thenReturn(emptyPage);

        // Act
        SearchProductWebResponse response = searchProductCommand.execute(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getProducts()).isEmpty();
        assertThat(response.getTotalElements()).isEqualTo(0);
    }

    @Test
    @DisplayName("Should handle pagination correctly")
    void testExecute_Pagination_ReturnsCorrectPage() {
        // Arrange
        SearchProductCommandRequest request = new SearchProductCommandRequest();
        request.setPage(1); // Second page
        request.setSize(1);

        Page<Product> productPage = new PageImpl<>(Collections.singletonList(product2), PageRequest.of(1, 1), 2);

        when(productRepository.findAll(any(Pageable.class))).thenReturn(productPage);

        // Act
        SearchProductWebResponse response = searchProductCommand.execute(request);

        // Assert
        assertThat(response).isNotNull();
        assertThat(response.getProducts()).hasSize(1);
        assertThat(response.getTotalElements()).isEqualTo(2);
        assertThat(response.getTotalPages()).isEqualTo(2);
        assertThat(response.getCurrentPage()).isEqualTo(1);

        verify(productRepository).findAll(PageRequest.of(1, 1));
    }
}
