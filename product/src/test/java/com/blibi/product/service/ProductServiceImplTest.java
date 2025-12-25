package com.blibi.product.service;

import com.blibi.product.dto.ProductDTO;
import com.blibi.product.entity.Product;
import com.blibi.product.repository.ProductRepository;
import com.blibi.product.serviceimpl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Optional;

import static com.blibi.product.utils.TestDataBuilder.*;
import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

/**
 * Unit tests for ProductServiceImpl.
 * 
 * This test class uses Mockito to mock dependencies and test the business logic
 * in isolation from the database and other external dependencies.
 * 
 * Test Strategy:
 * - Mock ProductRepository
 * - Test each service method independently
 * - Verify both happy path and error scenarios
 * - Use AssertJ for fluent assertions
 * - Follow AAA (Arrange-Act-Assert) pattern
 */
@ExtendWith(MockitoExtension.class)
@DisplayName("ProductServiceImpl Unit Tests")
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product testProduct;
    private ProductDTO testProductDTO;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testProduct = createTestProduct();
        testProductDTO = createTestProductDTO();
        pageable = createDefaultPageable();
    }

    // ==================== createProduct() Tests ====================

    @Test
    @DisplayName("createProduct - Valid Input - Returns Created Product")
    void createProduct_ValidInput_ReturnsCreatedProduct() {
        // Arrange
        when(productRepository.save(any(Product.class))).thenReturn(testProduct);

        // Act
        ProductDTO result = productService.createProduct(testProductDTO);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(testProduct.getProductId());
        assertThat(result.getProductName()).isEqualTo(testProduct.getProductName());
        assertThat(result.getPrice()).isEqualTo(testProduct.getPrice());

        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("createProduct - Repository Exception - Throws Exception")
    void createProduct_RepositoryException_ThrowsException() {
        // Arrange
        when(productRepository.save(any(Product.class)))
                .thenThrow(new RuntimeException("Database error"));

        // Act & Assert
        assertThatThrownBy(() -> productService.createProduct(testProductDTO))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Database error");

        verify(productRepository, times(1)).save(any(Product.class));
    }

    // ==================== viewProductDetailsByName() Tests ====================

    @Test
    @DisplayName("viewProductDetailsByName - Products Found - Returns Page of Products")
    void viewProductDetailsByName_ProductsFound_ReturnsPageOfProducts() {
        // Arrange
        Page<Product> productPage = createTestProductPage(pageable);
        when(productRepository.findByProductName("Test Laptop", pageable))
                .thenReturn(productPage);

        // Act
        Page<ProductDTO> result = productService.viewProductDetailsByName("Test Laptop", pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);

        verify(productRepository, times(1)).findByProductName("Test Laptop", pageable);
    }

    @Test
    @DisplayName("viewProductDetailsByName - No Products Found - Returns Empty Page")
    void viewProductDetailsByName_NoProductsFound_ReturnsEmptyPage() {
        // Arrange
        Page<Product> emptyPage = createEmptyProductPage(pageable);
        when(productRepository.findByProductName("Nonexistent Product", pageable))
                .thenReturn(emptyPage);

        // Act
        Page<ProductDTO> result = productService.viewProductDetailsByName("Nonexistent Product", pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();
        assertThat(result.getTotalElements()).isZero();

        verify(productRepository, times(1)).findByProductName("Nonexistent Product", pageable);
    }

    // ==================== searchProductByName() Tests ====================

    @Test
    @DisplayName("searchProductByName - Partial Match - Returns Matching Products")
    void searchProductByName_PartialMatch_ReturnsMatchingProducts() {
        // Arrange
        Page<Product> productPage = createTestProductPage(pageable);
        when(productRepository.searchProductByName("Laptop", pageable))
                .thenReturn(productPage);

        // Act
        Page<ProductDTO> result = productService.searchProductByName("Laptop", pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isNotEmpty();
        assertThat(result.getTotalElements()).isEqualTo(3);

        verify(productRepository, times(1)).searchProductByName("Laptop", pageable);
    }

    @Test
    @DisplayName("searchProductByName - Case Insensitive - Returns Matching Products")
    void searchProductByName_CaseInsensitive_ReturnsMatchingProducts() {
        // Arrange
        Page<Product> productPage = createTestProductPage(pageable);
        when(productRepository.searchProductByName("laptop", pageable))
                .thenReturn(productPage);

        // Act
        Page<ProductDTO> result = productService.searchProductByName("laptop", pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);

        verify(productRepository, times(1)).searchProductByName("laptop", pageable);
    }

    // ==================== getProductDetail() Tests ====================

    @Test
    @DisplayName("getProductDetail - Product Found - Returns Product Details")
    void getProductDetail_ProductFound_ReturnsProductDetails() {
        // Arrange
        when(productRepository.findByProductId("MTA-000001"))
                .thenReturn(Optional.of(testProduct));

        // Act
        ProductDTO result = productService.getProductDetail("MTA-000001");

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo("MTA-000001");
        assertThat(result.getProductName()).isEqualTo(testProduct.getProductName());

        verify(productRepository, times(1)).findByProductId("MTA-000001");
    }

    @Test
    @DisplayName("getProductDetail - Product Not Found - Throws RuntimeException")
    void getProductDetail_ProductNotFound_ThrowsRuntimeException() {
        // Arrange
        when(productRepository.findByProductId("INVALID-ID"))
                .thenReturn(Optional.empty());

        // Act & Assert
        assertThatThrownBy(() -> productService.getProductDetail("INVALID-ID"))
                .isInstanceOf(RuntimeException.class)
                .hasMessageContaining("Product not found with productId: INVALID-ID");

        verify(productRepository, times(1)).findByProductId("INVALID-ID");
    }

    // ==================== searchProductByCategory() Tests ====================

    @Test
    @DisplayName("searchProductByCategory - Category Found - Returns Products in Category")
    void searchProductByCategory_CategoryFound_ReturnsProductsInCategory() {
        // Arrange
        Page<Product> productPage = createTestProductPage(pageable);
        when(productRepository.searchProductByCategory("Electronics", pageable))
                .thenReturn(productPage);

        // Act
        Page<ProductDTO> result = productService.searchProductByCategory("Electronics", pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(3);
        assertThat(result.getTotalElements()).isEqualTo(3);

        verify(productRepository, times(1)).searchProductByCategory("Electronics", pageable);
    }

    @Test
    @DisplayName("searchProductByCategory - Empty Category - Returns Empty Page")
    void searchProductByCategory_EmptyCategory_ReturnsEmptyPage() {
        // Arrange
        Page<Product> emptyPage = createEmptyProductPage(pageable);
        when(productRepository.searchProductByCategory("NonexistentCategory", pageable))
                .thenReturn(emptyPage);

        // Act
        Page<ProductDTO> result = productService.searchProductByCategory("NonexistentCategory", pageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getContent()).isEmpty();

        verify(productRepository, times(1)).searchProductByCategory("NonexistentCategory", pageable);
    }

    // ==================== Pagination Tests ====================

    @Test
    @DisplayName("searchProductByName - Custom Pagination - Respects Page Size")
    void searchProductByName_CustomPagination_RespectsPageSize() {
        // Arrange
        Pageable customPageable = PageRequest.of(0, 10);
        Page<Product> productPage = createTestProductPage(customPageable);
        when(productRepository.searchProductByName("Laptop", customPageable))
                .thenReturn(productPage);

        // Act
        Page<ProductDTO> result = productService.searchProductByName("Laptop", customPageable);

        // Assert
        assertThat(result).isNotNull();
        assertThat(result.getPageable().getPageSize()).isEqualTo(10);

        verify(productRepository, times(1)).searchProductByName("Laptop", customPageable);
    }
}
