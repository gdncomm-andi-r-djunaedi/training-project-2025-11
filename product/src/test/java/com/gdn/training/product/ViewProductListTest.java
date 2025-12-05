package com.gdn.training.product;

import com.gdn.training.product.dto.ProductListRequest;
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

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
public class ViewProductListTest {

    @Mock
    private ProductRepository productRepository;

    private ProductServiceImpl productService;

    @BeforeEach
    void setUp() {
        productService = new ProductServiceImpl(productRepository);
    }

    @Test
    @DisplayName("view product list with filter and pagination")
    void viewProductListTest() {
        ProductListRequest request = new ProductListRequest();
        request.setProductId("SKU-000001");
        request.setProductName("Test");
        request.setPage(0);
        request.setSize(10);

        Product product = new Product("SKU-000001", "Test Product", 10000.0, "Description");
        Page<Product> productPage = new PageImpl<>(Collections.singletonList(product));

        when(productRepository.findByFilters(eq("SKU-000001"), eq("Test"), any(Pageable.class)))
                .thenReturn(productPage);

        Page<Product> result = productService.viewProductList(request);

        assertNotNull(result);
        assertEquals(1, result.getTotalElements());
        assertEquals("SKU-000001", result.getContent().get(0).getProduct_id());

        ArgumentCaptor<Pageable> pageableCaptor = ArgumentCaptor.forClass(Pageable.class);
        verify(productRepository).findByFilters(eq("SKU-000001"), eq("Test"), pageableCaptor.capture());

        Pageable capturedPageable = pageableCaptor.getValue();
        assertEquals(0, capturedPageable.getPageNumber());
        assertEquals(10, capturedPageable.getPageSize());
    }
}
