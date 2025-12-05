package com.blibli.productModule.service;

import com.blibli.productModule.dto.ProductDto;
import com.blibli.productModule.dto.ProductSearchResponseDto;
import com.blibli.productModule.entity.Product;
import com.blibli.productModule.repository.ProductRepository;
import com.blibli.productModule.service.impl.ProductImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductImpl productService;

    private Product product;
    private ProductDto productDto;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId("ID123");
        product.setProductId("PROD123");
        product.setName("Test Product");
        product.setDescription("Test Description");
        product.setCategory("Electronics");
        product.setPrice(new BigDecimal("100.00"));
        product.setBrand("Test Brand");
        product.setImageUrl("http://gmail.com/image.jpg");
        product.setIsActive(true);
        product.setCreatedAt(new Date());
        product.setUpdatedAt(new Date());

        productDto = new ProductDto();
        productDto.setProductId("PROD123");
        productDto.setName("Test Product");
        productDto.setDescription("Test Description");
        productDto.setCategory("Electronics");
        productDto.setPrice(new BigDecimal("100.00"));
        productDto.setBrand("Test Brand");
        productDto.setImageUrl("http://gmail.com/image.jpg");
        productDto.setIsActive(true);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void testCreateProduct_Success() {
        when(productRepository.existsByProductId("PROD123")).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductDto result = productService.createProduct(productDto);

        assertNotNull(result);
        assertEquals("PROD123", result.getProductId());
        assertEquals("Test Product", result.getName());
        verify(productRepository, times(1)).existsByProductId("PROD123");
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void testCreateProduct_ProductAlreadyExists() {
        when(productRepository.existsByProductId("PROD123")).thenReturn(true);

        assertThrows(RuntimeException.class, () -> {
            productService.createProduct(productDto);
        });

        verify(productRepository, times(1)).existsByProductId("PROD123");
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    void testSearchProducts_WithSearchTerm() {
        List<Product> products = new ArrayList<>();
        products.add(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);

        when(productRepository.searchProducts(anyString(), any(Pageable.class))).thenReturn(
                productPage);

        ProductSearchResponseDto result = productService.searchProducts("laptop", null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        assertEquals(0, result.getPage());
        assertEquals(10, result.getSize());
        assertEquals(1, result.getTotalElements());
        verify(productRepository, times(1)).searchProducts(anyString(), any(Pageable.class));
    }

    @Test
    void testSearchProducts_WithSearchTermAndCategory() {
        List<Product> products = new ArrayList<>();
        products.add(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);

        when(productRepository.searchProductsByCategory(anyString(), anyString(),
                any(Pageable.class))).thenReturn(productPage);

        ProductSearchResponseDto result = productService.searchProducts("laptop", "Electronics", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(productRepository, times(1)).searchProductsByCategory(anyString(), anyString(),
                any(Pageable.class));
    }

    @Test
    void testSearchProducts_WithCategoryOnly() {
        List<Product> products = new ArrayList<>();
        products.add(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);

        when(productRepository.findByCategoryAndIsActiveTrue(anyString(),
                any(Pageable.class))).thenReturn(productPage);

        ProductSearchResponseDto result = productService.searchProducts(null, "Electronics", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(productRepository, times(1)).findByCategoryAndIsActiveTrue(anyString(),
                any(Pageable.class));
    }

    @Test
    void testSearchProducts_NoFilters() {
        List<Product> products = new ArrayList<>();
        products.add(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);

        when(productRepository.findByIsActiveTrue(any(Pageable.class))).thenReturn(productPage);

        ProductSearchResponseDto result = productService.searchProducts(null, null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(productRepository, times(1)).findByIsActiveTrue(any(Pageable.class));
    }

    @Test
    void testGetAllProducts_WithCategory() {
        List<Product> products = new ArrayList<>();
        products.add(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);

        when(productRepository.findByCategoryAndIsActiveTrue(anyString(),
                any(Pageable.class))).thenReturn(productPage);

        ProductSearchResponseDto result = productService.getAllProducts("Electronics", 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(productRepository, times(1)).findByCategoryAndIsActiveTrue(anyString(),
                any(Pageable.class));
    }

    @Test
    void testGetAllProducts_NoCategory() {
        List<Product> products = new ArrayList<>();
        products.add(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 1);

        when(productRepository.findByIsActiveTrue(any(Pageable.class))).thenReturn(productPage);

        ProductSearchResponseDto result = productService.getAllProducts(null, 0, 10);

        assertNotNull(result);
        assertEquals(1, result.getContent().size());
        verify(productRepository, times(1)).findByIsActiveTrue(any(Pageable.class));
    }

    @Test
    void testGetProductById_Success() {
        when(productRepository.findByProductId("PROD123")).thenReturn(Optional.of(product));

        ProductDto result = productService.getProductById("PROD123");

        assertNotNull(result);
        assertEquals("PROD123", result.getProductId());
        assertEquals("Test Product", result.getName());
        verify(productRepository, times(1)).findByProductId("PROD123");
    }

    @Test
    void testGetProductById_NotFound() {
        when(productRepository.findByProductId("PROD123")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            productService.getProductById("PROD123");
        });

        verify(productRepository, times(1)).findByProductId("PROD123");
    }

    @Test
    void testDeleteProduct_Success() {
        when(productRepository.findByProductId("PROD123")).thenReturn(Optional.of(product));
        doNothing().when(productRepository).delete(any(Product.class));

        productService.deleteProduct("PROD123");

        verify(productRepository, times(1)).findByProductId("PROD123");
        verify(productRepository, times(1)).delete(any(Product.class));
    }

    @Test
    void testDeleteProduct_NotFound() {
        when(productRepository.findByProductId("PROD123")).thenReturn(Optional.empty());

        assertThrows(RuntimeException.class, () -> {
            productService.deleteProduct("PROD123");
        });

        verify(productRepository, times(1)).findByProductId("PROD123");
        verify(productRepository, never()).delete(any(Product.class));
    }
}

