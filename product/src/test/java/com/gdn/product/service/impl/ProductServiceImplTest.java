package com.gdn.product.service.impl;

import com.gdn.product.dto.request.ProductDTO;
import com.gdn.product.dto.request.SearchProductDTO;
import com.gdn.product.dto.response.ProductSearchResponseDTO;
import com.gdn.product.entity.Product;
import com.gdn.product.event.ProductUpdateEvent;
import com.gdn.product.exception.InvalidSearchRequestException;
import com.gdn.product.exception.ProductNotFoundException;
import com.gdn.product.repository.ProductServiceRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.*;

import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductServiceRepository repository;

    @Mock
    private KafkaTemplate<String, ProductUpdateEvent> kafkaTemplate;

    @InjectMocks
    private ProductServiceImpl productService;

    private Product product;
    private ProductDTO productDTO;

    @BeforeEach
    void setUp() {
        product = new Product();
        product.setId("db-id-1");
        product.setProductId("P-001");
        product.setProductName("Test Product");
        product.setDescription("Test description");
        product.setPrice(Double.valueOf(100));
        product.setCategory("Electronics");
        product.setBrand("BrandX");
        productDTO = new ProductDTO();
        productDTO.setProductId("P-001");
        productDTO.setProductName("Test Product");
        productDTO.setDescription("Test description");
        productDTO.setPrice(Double.valueOf(100));
        productDTO.setCategory("Electronics");
        productDTO.setBrand("BrandX");
    }


    @Test
    void createProduct_shouldSaveAndReturnDto() {
        when(repository.save(any(Product.class))).thenAnswer(invocation -> {
            Product p = invocation.getArgument(0);
            p.setId("db-id-1");
            return p;
        });


        ProductDTO result = productService.createProduct(productDTO);
        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo(productDTO.getProductId());
        assertThat(result.getProductName()).isEqualTo(productDTO.getProductName());
        assertThat(result.getPrice()).isEqualByComparingTo(productDTO.getPrice());
        verify(repository, times(1)).save(any(Product.class));
        verifyNoInteractions(kafkaTemplate);
    }


    @Test
    void update_whenProductExists_shouldUpdateAndSendEvent() {
        Product existing = new Product();
        existing.setId("db-id-1");
        existing.setProductId("P-001");
        existing.setProductName("Old Name");
        existing.setPrice(java.lang.Double.valueOf(50));
        existing.setCategory("OldCat");
        existing.setBrand("OldBrand");
        ProductDTO updateDto = new ProductDTO();
        updateDto.setProductId("P-001");
        updateDto.setProductName("New Name");
        updateDto.setPrice(java.lang.Double.valueOf(200));
        updateDto.setCategory("NewCat");
        updateDto.setBrand("NewBrand");

        when(repository.findByProductId("P-001")).thenReturn(Optional.of(existing));
        when(repository.save(any(Product.class))).thenAnswer(invocation -> invocation.getArgument(0));
        ProductDTO result = productService.update(updateDto);

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo("P-001");
        assertThat(result.getProductName()).isEqualTo("New Name");
        assertThat(result.getPrice()).isEqualByComparingTo(java.lang.Double.valueOf(200));
        assertThat(result.getCategory()).isEqualTo("NewCat");
        assertThat(result.getBrand()).isEqualTo("NewBrand");

        verify(repository, times(1)).findByProductId("P-001");
        verify(repository, times(1)).save(any(Product.class));

        ArgumentCaptor<ProductUpdateEvent> eventCaptor = ArgumentCaptor.forClass(ProductUpdateEvent.class);
        verify(kafkaTemplate, times(1))
                .send(eq("product-update"), eq("P-001"), eventCaptor.capture());

        ProductUpdateEvent sentEvent = eventCaptor.getValue();
        assertThat(sentEvent.getProductId()).isEqualTo("P-001");
        assertThat(sentEvent.getProductName()).isEqualTo("New Name");
        assertThat(sentEvent.getPrice()).isEqualByComparingTo(java.lang.Double.valueOf(200));
        assertThat(sentEvent.getCategory()).isEqualTo("NewCat");
        assertThat(sentEvent.getBrand()).isEqualTo("NewBrand");
    }

    @Test
    void update_whenProductNotFound_shouldThrowProductNotFoundException() {
        ProductDTO dto = new ProductDTO();
        dto.setProductId("P-999");

        when(repository.findByProductId("P-999")).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.update(dto));

        verify(repository, times(1)).findByProductId("P-999");
        verify(repository, never()).save(any());
        verifyNoInteractions(kafkaTemplate);
    }


    @Test
    void getById_whenProductExists_shouldReturnEntity() {
        when(repository.findById("db-id-1")).thenReturn(Optional.of(product));


        Product result = productService.getById("db-id-1");

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo("P-001");
        verify(repository, times(1)).findById("db-id-1");
    }

    @Test
    void getById_whenProductNotFound_shouldThrowProductNotFoundException() {

        when(repository.findById("db-id-1")).thenReturn(Optional.empty());


        assertThrows(ProductNotFoundException.class,
                () -> productService.getById("db-id-1"));

        verify(repository, times(1)).findById("db-id-1");
    }


    @Test
    void search_whenKeywordBlank_shouldThrowInvalidSearchRequestException() {
        SearchProductDTO req = new SearchProductDTO();
        req.setKeyword("   ");
        assertThrows(InvalidSearchRequestException.class,
                () -> productService.search(req));

        verifyNoInteractions(repository);
    }

    @Test
    void search_whenValidRequest_shouldReturnPagedResults() {
        SearchProductDTO req = new SearchProductDTO();
        req.setKeyword("test");
        req.setPage(0);
        req.setSize(2);
        req.setSort(1);

        Product p1 = new Product();
        p1.setId("id-1");
        p1.setProductId("P-001");
        p1.setProductName("Test A");
        p1.setPrice(java.lang.Double.valueOf(100));

        Product p2 = new Product();
        p2.setId("id-2");
        p2.setProductId("P-002");
        p2.setProductName("Test B");
        p2.setPrice(java.lang.Double.valueOf(200));

        List<Product> content = List.of(p1, p2);
        Page<Product> page = new PageImpl<>(content, PageRequest.of(0, 2, Sort.by("price").ascending()), 2);

        when(repository.findByProductNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                eq("test"),
                eq("test"),
                any(Pageable.class))
        ).thenReturn(page);

        ProductSearchResponseDTO result = productService.search(req);

        assertThat(result).isNotNull();
        assertThat(result.getContent()).hasSize(2);
        assertThat(result.getPage()).isEqualTo(0);
        assertThat(result.getSize()).isEqualTo(2);
        assertThat(result.getTotalElements()).isEqualTo(2);
        assertThat(result.getTotalPages()).isEqualTo(1);

        assertThat(result.getContent().get(0).getProductId()).isEqualTo("P-001");
        assertThat(result.getContent().get(1).getProductId()).isEqualTo("P-002");

        verify(repository, times(1))
                .findByProductNameContainingIgnoreCaseOrDescriptionContainingIgnoreCase(
                        eq("test"),
                        eq("test"),
                        any(Pageable.class)
                );
    }


    @Test
    void getProductDetail_whenProductExists_shouldReturnDto() {
        when(repository.findByProductId("P-001")).thenReturn(Optional.of(product));

        ProductDTO result = productService.getProductDetail("P-001");

        assertThat(result).isNotNull();
        assertThat(result.getProductId()).isEqualTo("P-001");
        assertThat(result.getProductName()).isEqualTo("Test Product");
        assertThat(result.getPrice()).isEqualByComparingTo(java.lang.Double.valueOf(100.0));

        verify(repository, times(1)).findByProductId("P-001");
    }

    @Test
    void getProductDetail_whenProductNotFound_shouldThrowProductNotFoundException() {
        when(repository.findByProductId("P-999")).thenReturn(Optional.empty());

        assertThrows(ProductNotFoundException.class,
                () -> productService.getProductDetail("P-999"));

        verify(repository, times(1)).findByProductId("P-999");
    }
}
