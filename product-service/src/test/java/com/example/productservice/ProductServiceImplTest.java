package com.example.productservice;

import com.example.productservice.constant.Category;
import com.example.productservice.dto.GetProductResponse;
import com.example.productservice.exception.BusinessException;
import com.example.productservice.model.Product;
import com.example.productservice.repository.ProductRepository;
import com.example.productservice.service.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private Product createDummyProduct(String id, String name) {
        return Product.builder()
                .id(id)
                .name(name)
                .description("desc " + name)
                .category(Category.ELECTRONICS)
                .price(new BigDecimal("100.00"))
                .build();
    }

    @Test
    void testGetAllProducts() {
        Product p1 = createDummyProduct("1", "Laptop");
        Product p2 = createDummyProduct("2", "Smartphone");

        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(p1, p2), pageable, 2);

        when(productRepository.findAll(pageable)).thenReturn(productPage);

        Page<GetProductResponse> result = productService.getAllProducts(pageable);

        assertEquals(2, result.getTotalElements());
        assertEquals("Laptop", result.getContent().get(0).name());
        assertEquals("ELECTRONICS", result.getContent().get(0).category());
        assertEquals(new BigDecimal("100.00"), result.getContent().get(0).price());

        verify(productRepository).findAll(pageable);
    }

    @Test
    void testSearchProducts() {
        Product p1 = createDummyProduct("1", "Laptop");

        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> productPage = new PageImpl<>(List.of(p1), pageable, 1);

        when(productRepository.findByNameContainingIgnoreCase("lap", pageable))
                .thenReturn(productPage);

        Page<GetProductResponse> result = productService.searchProducts("lap", pageable);

        assertEquals(1, result.getTotalElements());
        assertTrue(result.getContent().get(0).name().toLowerCase().contains("lap"));
        assertEquals("ELECTRONICS", result.getContent().get(0).category());

        verify(productRepository).findByNameContainingIgnoreCase("lap", pageable);
    }

    @Test
    void testGetProductDetailFound() {
        Product p = createDummyProduct("1", "Laptop");

        when(productRepository.findById("1")).thenReturn(Optional.of(p));

        GetProductResponse response = productService.getProductDetail("1");

        assertEquals("1", response.id());
        assertEquals("Laptop", response.name());
        assertEquals("desc Laptop", response.description());
        assertEquals("ELECTRONICS", response.category());
        assertEquals(new BigDecimal("100.00"), response.price());

        verify(productRepository).findById("1");
    }

    @Test
    void testGetProductDetailNotFound() {
        when(productRepository.findById("99")).thenReturn(Optional.empty());

        BusinessException exception = assertThrows(
                BusinessException.class,
                () -> productService.getProductDetail("99")
        );

        assertEquals("PRODUCT_NOT_FOUND", exception.getCode());
        assertEquals("Product not found with id: 99", exception.getMessage());

        verify(productRepository).findById("99");
    }
}