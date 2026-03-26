package com.Product.ProductService;


import com.Product.ProductService.dto.ProductResponseDTO;
import com.Product.ProductService.entity.Product;
import com.Product.ProductService.exceptions.ProductServiceExceptions;
import com.Product.ProductService.repository.ProductRepository;
import com.Product.ProductService.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.*;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductServiceUnitTests {
    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductResponseDTO productResponseDTO;

    @BeforeEach
    void setUp() {
        MockitoAnnotations.openMocks(this);

        product = new Product(
                "1",
                "PC001",
                "Test Product",
                "A sample description",
                100.0,
                "Electronics",
                10
        );

        productResponseDTO = new ProductResponseDTO();
        productResponseDTO.setProductId("1");
        productResponseDTO.setProductCode("PC001");
        productResponseDTO.setProductName("Test Product");
        productResponseDTO.setProductDescription("A sample description");
        productResponseDTO.setCategory("Electronics");
        productResponseDTO.setPrice(100.0);
    }


    // saveProduct()


    @Test
    void saveProduct_Success() {
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponseDTO saved = productService.saveProduct(productResponseDTO);

        assertNotNull(saved);
        assertEquals("Test Product", saved.getProductName());
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    void saveProduct_ThrowsWhenNullBody() {
        ProductServiceExceptions ex = assertThrows(
                ProductServiceExceptions.class,
                () -> productService.saveProduct(null)
        );

        assertEquals("Product body cannot be null", ex.getMessage());
    }

    @Test
    void saveProduct_ThrowsWhenProductNameEmpty() {
        productResponseDTO.setProductName("  ");

        ProductServiceExceptions ex = assertThrows(
                ProductServiceExceptions.class,
                () -> productService.saveProduct(productResponseDTO)
        );

        assertEquals("Product name cannot be empty", ex.getMessage());
    }

    @Test
    void saveProduct_ThrowsWhenProductDescriptionEmpty() {
        productResponseDTO.setProductDescription("");

        ProductServiceExceptions ex = assertThrows(
                ProductServiceExceptions.class,
                () -> productService.saveProduct(productResponseDTO)
        );

        assertEquals("Product description cannot be empty", ex.getMessage());
    }


    // getProductById()


    @Test
    void getProductById_Success() {
        when(productRepository.findById("1")).thenReturn(Optional.of(product));

        ProductResponseDTO dto = productService.getProductById("1");

        assertNotNull(dto);
        assertEquals("Test Product", dto.getProductName());
        verify(productRepository, times(1)).findById("1");
    }

    @Test
    void getProductById_ThrowsIfNotFound() {
        when(productRepository.findById("1")).thenReturn(Optional.empty());

        ProductServiceExceptions ex = assertThrows(
                ProductServiceExceptions.class,
                () -> productService.getProductById("1")
        );

        assertTrue(ex.getMessage().contains("Product not found with id 1"));
    }


    // getProducts()


    @Test
    void getProducts_Success() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> mockPage = new PageImpl<>(java.util.List.of(product));

        when(productRepository.findAll(pageable)).thenReturn(mockPage);

        Page<ProductResponseDTO> response = productService.getProducts(pageable);

        assertEquals(1, response.getTotalElements());
        assertEquals("Test Product", response.getContent().get(0).getProductName());
    }


    // searchProducts()


    @Test
    void searchProducts_WithKeyword() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> mockPage = new PageImpl<>(java.util.List.of(product));

        when(productRepository
                .findByProductNameContainingIgnoreCaseOrProductDescriptionContainingIgnoreCase(
                        "test", "test", pageable))
                .thenReturn(mockPage);

        Page<ProductResponseDTO> result = productService.searchProducts("test", pageable);

        assertEquals(1, result.getTotalElements());
        verify(productRepository, times(1))
                .findByProductNameContainingIgnoreCaseOrProductDescriptionContainingIgnoreCase(
                        "test", "test", pageable);
    }

    @Test
    void searchProducts_WithoutKeyword_ReturnsAllProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> mockPage = new PageImpl<>(java.util.List.of(product));

        when(productRepository.findAll(pageable)).thenReturn(mockPage);

        Page<ProductResponseDTO> result = productService.searchProducts(null, pageable);

        assertEquals(1, result.getTotalElements());
        verify(productRepository, times(1)).findAll(pageable);
    }
}
