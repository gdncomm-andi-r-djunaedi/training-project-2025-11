package com.product.service;

import com.product.entity.Product;
import com.product.repository.ProductRepository;
import com.product.service.impl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ProductServiceTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    @BeforeEach
    void setup() {
        MockitoAnnotations.openMocks(this);
    }

    private Product createSampleProduct(UUID id) {
        Product p = new Product();
        p.setId(id);
        p.setName("Test Product");
        p.setDescription("Sample description");
        p.setPrice(new BigDecimal("199.99"));
        p.setCategory("Electronics");
        p.setImageUrl("http://example.com/img.jpg");
        p.setActive(true);
        return p;
    }

    @Test
    void testCreate() {
        Product input = createSampleProduct(null);

        Mockito.when(productRepository.save(input)).thenReturn(input);

        Product result = productService.create(input);

        assertNotNull(result);
        Mockito.verify(productRepository, Mockito.times(1)).save(input);
    }

    @Test
    void testGetById_Found() {
        UUID id = UUID.randomUUID();
        Product product = createSampleProduct(id);

        Mockito.when(productRepository.findById(id)).thenReturn(Optional.of(product));

        Product result = productService.getById(id);

        assertEquals(id, result.getId());
    }

    @Test
    void testGetById_NotFound() {
        UUID id = UUID.randomUUID();

        Mockito.when(productRepository.findById(id)).thenReturn(Optional.empty());

        RuntimeException ex = assertThrows(RuntimeException.class, () ->
                productService.getById(id)
        );

        assertEquals("Product not found", ex.getMessage());
    }

    @Test
    void testGetAll() {
        List<Product> list = List.of(
                createSampleProduct(UUID.randomUUID()),
                createSampleProduct(UUID.randomUUID())
        );

        Mockito.when(productRepository.findAll()).thenReturn(list);

        List<Product> result = productService.getAll();

        assertEquals(2, result.size());
    }

    @Test
    void testUpdate() {
        UUID id = UUID.randomUUID();
        Product existing = createSampleProduct(id);
        Product updated = createSampleProduct(id);
        updated.setName("Updated Name");

        Mockito.when(productRepository.findById(id)).thenReturn(Optional.of(existing));
        Mockito.when(productRepository.save(existing)).thenReturn(existing);

        Product result = productService.update(id, updated);

        assertEquals("Updated Name", result.getName());
        Mockito.verify(productRepository, Mockito.times(1)).save(existing);
    }

    @Test
    void testDelete() {
        UUID id = UUID.randomUUID();

        Mockito.doNothing().when(productRepository).deleteById(id);

        productService.delete(id);

        Mockito.verify(productRepository, Mockito.times(1)).deleteById(id);
    }

    @Test
    void testSearchProducts() {
        String keyword = "test";
        int page = 0;
        int size = 10;

        Product product = createSampleProduct(UUID.randomUUID());
        Page<Product> mockPage = new PageImpl<>(List.of(product));

        Mockito.when(productRepository.findByNameContainingIgnoreCase(
                Mockito.eq(keyword),
                Mockito.any(Pageable.class)
        )).thenReturn(mockPage);

        Page<Product> result = productService.searchProducts(keyword, page, size);

        assertEquals(1, result.getContent().size());
        assertEquals("Test Product", result.getContent().get(0).getName());
    }
}
