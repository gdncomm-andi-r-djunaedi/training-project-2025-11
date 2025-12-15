package com.elfrida.product.service;

import com.elfrida.product.model.Product;
import com.elfrida.product.repository.ProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.server.ResponseStatusException;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @Test
    void createProduct_shouldSaveProduct() {
        Product product = new Product();
        product.setName("Shoe");
        product.setPrice(BigDecimal.TEN);

        when(productRepository.save(product)).thenReturn(product);

        Product result = productService.createProduct(product);

        assertThat(result).isSameAs(product);
        verify(productRepository).save(product);
    }

    @Test
    void getAllProducts_shouldReturnPagedProducts() {
        Pageable pageable = PageRequest.of(0, 10);
        Product p = new Product();
        p.setName("Shoe");
        Page<Product> page = new PageImpl<>(List.of(p), pageable, 1);

        when(productRepository.findAll(pageable)).thenReturn(page);

        Page<Product> result = productService.getAllProducts(pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        assertThat(result.getContent().getFirst().getName()).isEqualTo("Shoe");
    }

    @Test
    void searchProducts_shouldDelegateToRepository() {
        Pageable pageable = PageRequest.of(0, 10);
        Product p = new Product();
        p.setName("Shoe");
        Page<Product> page = new PageImpl<>(List.of(p), pageable, 1);

        when(productRepository.findByNameContainingIgnoreCase("shoe", pageable))
                .thenReturn(page);

        Page<Product> result = productService.searchProducts("shoe", pageable);

        assertThat(result.getTotalElements()).isEqualTo(1);
        verify(productRepository).findByNameContainingIgnoreCase("shoe", pageable);
    }

    @Test
    void getProductById_shouldReturnProduct_whenFound() {
        Product p = new Product();
        p.setId("P001");
        p.setName("Shoe");

        when(productRepository.findById("P001")).thenReturn(Optional.of(p));

        Product result = productService.getProductById("P001");

        assertThat(result).isSameAs(p);
    }

    @Test
    void getProductById_shouldThrow_whenNotFound() {
        when(productRepository.findById("P999")).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById("P999"))
                .isInstanceOf(ResponseStatusException.class);
    }
}


