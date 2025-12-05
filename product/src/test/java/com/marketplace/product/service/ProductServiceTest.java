package com.marketplace.product.service;

import com.marketplace.common.exception.ResourceNotFoundException;
import com.marketplace.product.dto.ProductResponse;
import com.marketplace.product.dto.ProductSearchRequest;
import com.marketplace.product.entity.Product;
import com.marketplace.product.mapper.ProductMapper;
import com.marketplace.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductMapper productMapper;

    @InjectMocks
    private ProductService productService;

    private Product product;
    private ProductResponse productResponse;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id("prod-001")
                .name("Test Product")
                .description("Test Description")
                .category("Electronics")
                .brand("TestBrand")
                .price(BigDecimal.valueOf(99.99))
                .active(true)
                .build();

        productResponse = ProductResponse.builder()
                .id("prod-001")
                .name("Test Product")
                .description("Test Description")
                .category("Electronics")
                .brand("TestBrand")
                .price(BigDecimal.valueOf(99.99))
                .active(true)
                .build();
    }

    @Nested
    @DisplayName("Get Product By ID Tests")
    class GetProductByIdTests {

        @Test
        @DisplayName("Should return product when found")
        void shouldReturnProductWhenFound() {
            // Given
            when(productRepository.findById(anyString())).thenReturn(Optional.of(product));
            when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

            // When
            ProductResponse result = productService.getProductById("prod-001");

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getId()).isEqualTo("prod-001");
            assertThat(result.getName()).isEqualTo("Test Product");
        }

        @Test
        @DisplayName("Should throw exception when product not found")
        void shouldThrowExceptionWhenNotFound() {
            // Given
            when(productRepository.findById(anyString())).thenReturn(Optional.empty());

            // When/Then
            assertThatThrownBy(() -> productService.getProductById("invalid-id"))
                    .isInstanceOf(ResourceNotFoundException.class);
        }
    }

    @Nested
    @DisplayName("List Products Tests")
    class ListProductsTests {

        @Test
        @DisplayName("Should return paginated products")
        void shouldReturnPaginatedProducts() {
            // Given
            Page<Product> productPage = new PageImpl<>(List.of(product));
            when(productRepository.findByActiveTrue(any(Pageable.class))).thenReturn(productPage);
            when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

            // When
            Page<ProductResponse> result = productService.listProducts(0, 10, null, null);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getName()).isEqualTo("Test Product");
        }
    }

    @Nested
    @DisplayName("Search Products Tests")
    class SearchProductsTests {

        @Test
        @DisplayName("Should search products by keyword")
        void shouldSearchProductsByKeyword() {
            // Given
            ProductSearchRequest request = ProductSearchRequest.builder()
                    .keyword("Test")
                    .page(0)
                    .size(10)
                    .build();

            Page<Product> productPage = new PageImpl<>(List.of(product));
            when(productRepository.searchProducts(anyString(), any(Pageable.class))).thenReturn(productPage);
            when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

            // When
            Page<ProductResponse> result = productService.searchProducts(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
        }

        @Test
        @DisplayName("Should search products by category")
        void shouldSearchProductsByCategory() {
            // Given
            ProductSearchRequest request = ProductSearchRequest.builder()
                    .category("Electronics")
                    .page(0)
                    .size(10)
                    .build();

            Page<Product> productPage = new PageImpl<>(List.of(product));
            when(productRepository.findByCategoryAndActiveTrue(anyString(), any(Pageable.class)))
                    .thenReturn(productPage);
            when(productMapper.toResponse(any(Product.class))).thenReturn(productResponse);

            // When
            Page<ProductResponse> result = productService.searchProducts(request);

            // Then
            assertThat(result).isNotNull();
            assertThat(result.getContent()).hasSize(1);
            assertThat(result.getContent().get(0).getCategory()).isEqualTo("Electronics");
        }
    }
}

