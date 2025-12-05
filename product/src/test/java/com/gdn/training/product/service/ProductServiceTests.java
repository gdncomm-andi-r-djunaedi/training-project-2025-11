package com.gdn.training.product.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.Arrays;
import java.util.Optional;
import java.util.regex.Pattern;

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

import com.gdn.training.product.model.entity.Product;
import com.gdn.training.product.model.response.PagedResponse;
import com.gdn.training.product.model.response.ProductDetailResponse;
import com.gdn.training.product.repository.ProductRepository;

@ExtendWith(MockitoExtension.class)
public class ProductServiceTests {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Product testProduct1;
    private Product testProduct2;
    private Pageable pageable;

    @BeforeEach
    void setUp() {
        testProduct1 = new Product();
        testProduct1.setId("507f1f77bcf86cd799439011");
        testProduct1.setName("Laptop");
        testProduct1.setDescription("Gaming Laptop");
        testProduct1.setPrice(1500.00);

        testProduct2 = new Product();
        testProduct2.setId("507f1f77bcf86cd799439012");
        testProduct2.setName("Mouse");
        testProduct2.setDescription("Wireless Mouse");
        testProduct2.setPrice(25.00);

        pageable = PageRequest.of(0, 10);
    }

    @Test
    void getAllProducts_WhenNameIsNull_ReturnsAllProducts() {
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct1, testProduct2), pageable, 2);
        when(productRepository.findAll(pageable)).thenReturn(productPage);

        PagedResponse<ProductDetailResponse> result = productService.getAll(null, pageable);

        assertNotNull(result);
        assertEquals(2, result.getTotalElements());
        assertEquals(2, result.getContent().size());
        assertEquals("Laptop", result.getContent().get(0).getName());
        assertEquals("Mouse", result.getContent().get(1).getName());
        verify(productRepository, times(1)).findAll(pageable);
    }

    @Test
    void getAllProducts_WhenNameProvided_ReturnsFilteredProducts() {
        String searchName = "Laptop";
        Page<Product> productPage = new PageImpl<>(Arrays.asList(testProduct1), pageable, 1);
        when(productRepository.findByNameRegex(any(Pattern.class), eq(pageable))).thenReturn(productPage);

        PagedResponse<ProductDetailResponse> result = productService.getAll(searchName, pageable);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("Laptop", result.getContent().get(0).getName());
        assertEquals(1500.00, result.getContent().get(0).getPrice());
        verify(productRepository, times(1)).findByNameRegex(any(Pattern.class), eq(pageable));
    }

    @Test
    void getDetail_WhenValidId_ReturnsProduct() {
        String productId = "507f1f77bcf86cd799439011";
        when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct1));

        ProductDetailResponse result = productService.getDetail(productId);

        assertNotNull(result);
        assertEquals(productId, result.getId());
        assertEquals("Laptop", result.getName());
        assertEquals("Gaming Laptop", result.getDescription());
        assertEquals(1500.00, result.getPrice());
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    void getDetail_WhenInvalidId_ThrowsRuntimeException() {
        String invalidId = "507f1f77bcf86cd799439999";
        when(productRepository.findById(invalidId)).thenReturn(Optional.empty());

        RuntimeException exception = assertThrows(RuntimeException.class, () -> {
            productService.getDetail(invalidId);
        });

        assertEquals("Product not found", exception.getMessage());
        verify(productRepository, times(1)).findById(invalidId);
    }

}
