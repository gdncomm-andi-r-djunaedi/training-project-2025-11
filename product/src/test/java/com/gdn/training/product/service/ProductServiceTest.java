package com.gdn.training.product.service;

import com.gdn.training.product.entity.Product;
import com.gdn.training.product.repository.ProductRepository;
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
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductService productService;

    private Pageable pageable;

    @BeforeEach
    void setUp() {
        pageable = PageRequest.of(0, 10);
    }

    @Test
    void searchProductsReturnsAllWhenQueryBlank() {
        Page<Product> page = new PageImpl<>(List.of(buildProduct("Alpha")));
        when(productRepository.findAll(pageable)).thenReturn(page);

        Page<Product> result = productService.searchProducts("   ", pageable);

        assertThat(result).isEqualTo(page);
        verify(productRepository).findAll(pageable);
    }

    @Test
    void searchProductsUsesWildcardQueryWhenPatternPresent() {
        Page<Product> page = new PageImpl<>(List.of(buildProduct("Gadget Pro")));
        when(productRepository.findByNameLikeIgnoreCase(eq("Gadget%Pro"), eq(pageable))).thenReturn(page);

        Page<Product> result = productService.searchProducts("Gadget*Pro", pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(productRepository).findByNameLikeIgnoreCase("Gadget%Pro", pageable);
    }

    @Test
    void searchProductsFallsBackToContainsSearch() {
        Page<Product> page = new PageImpl<>(List.of(buildProduct("Widget Beta")));
        when(productRepository.findByNameContainingIgnoreCase("widget", pageable)).thenReturn(page);

        Page<Product> result = productService.searchProducts("widget", pageable);

        assertThat(result.getContent()).hasSize(1);
        verify(productRepository).findByNameContainingIgnoreCase("widget", pageable);
    }

    @Test
    void getProductByIdReturnsProduct() {
        UUID id = UUID.randomUUID();
        Product product = buildProduct("Alpha");
        product.setId(id);
        when(productRepository.findById(id)).thenReturn(Optional.of(product));

        Product result = productService.getProductById(id);

        assertThat(result).isSameAs(product);
    }

    @Test
    void getProductByIdThrowsWhenMissing() {
        UUID id = UUID.randomUUID();
        when(productRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> productService.getProductById(id))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("Product not found");
    }

    private Product buildProduct(String name) {
        return Product.builder()
                .name(name)
                .description("desc")
                .price(BigDecimal.TEN)
                .quantity(Integer.MAX_VALUE)
                .imageUrl("https://example.com/" + name)
                .build();
    }
}

