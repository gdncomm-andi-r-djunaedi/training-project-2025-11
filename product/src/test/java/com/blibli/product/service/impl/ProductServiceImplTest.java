package com.blibli.product.service.impl;

import com.blibli.product.dto.PageResponse;
import com.blibli.product.dto.ProductEvent;
import com.blibli.product.dto.ProductRequest;
import com.blibli.product.dto.ProductResponse;
import com.blibli.product.entity.Product;
import com.blibli.product.enums.CategoryType;
import com.blibli.product.exception.BadRequestException;
import com.blibli.product.exception.ResourceNotFoundException;
import com.blibli.product.messaging.ProductEventProducer;
import com.blibli.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("Product Service Implementation Tests")
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private ProductEventProducer eventProducer;

    @InjectMocks
    private ProductServiceImpl productService;

    private static final String PRODUCT_ID = "product-123";
    private static final String SKU = "SKU-12345-23455";
    private Product product;
    private ProductRequest productRequest;

    @BeforeEach
    void setUp() {
        product = Product.builder()
                .id(PRODUCT_ID)
                .sku(SKU)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .category(CategoryType.ELECTRONIC)
                .stockQuantity(100)
                .isActive(true)
                .createdAt(new Date())
                .updatedAt(new Date())
                .build();

        productRequest = ProductRequest.builder()
                .sku(SKU)
                .name("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .category(CategoryType.ELECTRONIC)
                .stockQuantity(100)
                .build();
    }

    @Test
    @DisplayName("Should create product successfully")
    void createProductSuccess() {

        when(productRepository.existsBySku(SKU)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        doNothing().when(eventProducer).sendProductEvent(any(ProductEvent.class));


        ProductResponse response = productService.createProduct(productRequest);

        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(PRODUCT_ID);
        assertThat(response.getSku()).isEqualTo(SKU);
        assertThat(response.getName()).isEqualTo("Test Product");
        verify(productRepository).existsBySku(SKU);
        verify(productRepository).save(any(Product.class));
        verify(eventProducer).sendProductEvent(any(ProductEvent.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when SKU already exists")
    void createProductFailureDuplicateSKU() {

        when(productRepository.existsBySku(SKU)).thenReturn(true);


        assertThatThrownBy(() -> productService.createProduct(productRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("SKU already exists");

        verify(productRepository, never()).save(any(Product.class));
        verify(eventProducer, never()).sendProductEvent(any(ProductEvent.class));
    }

    @Test
    @DisplayName("Should use default stock quantity when not provided")
    void createProductSuccessDefaultStockQuantity() {

        ProductRequest requestWithoutStock = ProductRequest.builder()
                .sku(SKU)
                .name("Test Product")
                .price(new BigDecimal("99.99"))
                .category(CategoryType.ELECTRONIC)
                .build();

        when(productRepository.existsBySku(SKU)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenAnswer(invocation -> {
            Product saved = invocation.getArgument(0);
            assertThat(saved.getStockQuantity()).isEqualTo(1000); // Default value
            return product;
        });
        doNothing().when(eventProducer).sendProductEvent(any(ProductEvent.class));


        productService.createProduct(requestWithoutStock);


        verify(productRepository).save(any(Product.class));
    }

    @Test
    @DisplayName("Should update product successfully")
    void updateProductSuccess() {
        // Given - SKU must match existing product's SKU (immutability enforced)
        ProductRequest updateRequest = ProductRequest.builder()
                .sku(SKU) // Same SKU as existing product
                .name("Updated Product")
                .description("Updated Description")
                .price(new BigDecimal("149.99"))
                .category(CategoryType.FASHION)
                .stockQuantity(200)
                .build();

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        doNothing().when(eventProducer).sendProductEvent(any(ProductEvent.class));


        ProductResponse response = productService.updateProduct(PRODUCT_ID, updateRequest);


        assertThat(response).isNotNull();
        verify(productRepository).findById(PRODUCT_ID);
        verify(productRepository).save(any(Product.class));
        verify(eventProducer).sendProductEvent(any(ProductEvent.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product not found for update")
    void updateProductFailureProductNotFound() {

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> productService.updateProduct(PRODUCT_ID, productRequest))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");

        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should throw BadRequestException when trying to change SKU (SKU immutability)")
    void updateProductFailureSKUImmutability() {
        // Given - Try to change SKU to a different valid SKU
        String newSku = "ABC-12345-67890"; // Valid format but different from existing SKU
        ProductRequest updateRequest = ProductRequest.builder()
                .sku(newSku)
                .name("Updated Product")
                .price(new BigDecimal("99.99"))
                .category(CategoryType.ELECTRONIC)
                .build();

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));


        assertThatThrownBy(() -> productService.updateProduct(PRODUCT_ID, updateRequest))
                .isInstanceOf(BadRequestException.class)
                .hasMessageContaining("SKU cannot be changed");
    }

    @Test
    @DisplayName("Should get product by ID successfully")
    void getProductByIdSuccessById() {

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));

        ProductResponse response = productService.getProductById(PRODUCT_ID);


        assertThat(response).isNotNull();
        assertThat(response.getId()).isEqualTo(PRODUCT_ID);
        assertThat(response.getSku()).isEqualTo(SKU);
        verify(productRepository).findById(PRODUCT_ID);
        verify(productRepository, never()).findBySku(anyString());
    }

    @Test
    @DisplayName("Should get product by SKU when ID not found")
    void getProductByIdSuccessBySku() {

        when(productRepository.findById(SKU)).thenReturn(Optional.empty());
        when(productRepository.findBySku(SKU)).thenReturn(Optional.of(product));


        ProductResponse response = productService.getProductById(SKU);

        assertThat(response).isNotNull();
        assertThat(response.getSku()).isEqualTo(SKU);
        verify(productRepository).findById(SKU);
        verify(productRepository).findBySku(SKU);
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product not found")
    void getProductByIdFailureNotFound() {

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());
        when(productRepository.findBySku(PRODUCT_ID)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> productService.getProductById(PRODUCT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");
    }

    @Test
    @DisplayName("Should get product by SKU successfully")
    void getProductBySkuSuccess() {

        when(productRepository.findBySkuIgnoreCase(SKU)).thenReturn(Optional.of(product));


        ProductResponse response = productService.getProductBySku(SKU);


        assertThat(response).isNotNull();
        assertThat(response.getSku()).isEqualTo(SKU);
        verify(productRepository).findBySkuIgnoreCase(SKU);
    }

    @Test
    @DisplayName("Should get all products with pagination")
    void getAllProductsSuccess() {

        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products, pageRequest, 1);

        when(productRepository.findByIsActiveTrue(pageRequest)).thenReturn(productPage);


        PageResponse<ProductResponse> response = productService.getAllProducts(0, 10);


        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getPageNumber()).isEqualTo(0);
        assertThat(response.getPageSize()).isEqualTo(10);
        assertThat(response.getTotalElements()).isEqualTo(1);
        verify(productRepository).findByIsActiveTrue(pageRequest);
    }

    @Test
    @DisplayName("Should get products by category with pagination")
    void getProductsByCategorySuccess() {

        PageRequest pageRequest = PageRequest.of(0, 10);
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products, pageRequest, 1);

        when(productRepository.findByIsActiveTrueAndCategory(eq(CategoryType.ELECTRONIC), eq(pageRequest)))
                .thenReturn(productPage);


        PageResponse<ProductResponse> response = productService.getProductsByCategory(
                CategoryType.ELECTRONIC, 0, 10);


        assertThat(response).isNotNull();
        assertThat(response.getContent()).hasSize(1);
        assertThat(response.getContent().get(0).getCategory()).isEqualTo(CategoryType.ELECTRONIC);
        verify(productRepository).findByIsActiveTrueAndCategory(eq(CategoryType.ELECTRONIC), eq(pageRequest));
    }

    @Test
    @DisplayName("Should delete product (soft delete)")
    void deleteProductSuccess() {

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        doNothing().when(eventProducer).sendProductEvent(any(ProductEvent.class));


        productService.deleteProduct(PRODUCT_ID);


        ArgumentCaptor<Product> productCaptor = ArgumentCaptor.forClass(Product.class);
        verify(productRepository).save(productCaptor.capture());
        assertThat(productCaptor.getValue().getIsActive()).isFalse();
        verify(eventProducer).sendProductEvent(any(ProductEvent.class));
    }

    @Test
    @DisplayName("Should throw ResourceNotFoundException when product not found for delete")
    void deleteProductFailureProductNotFound() {

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.empty());


        assertThatThrownBy(() -> productService.deleteProduct(PRODUCT_ID))
                .isInstanceOf(ResourceNotFoundException.class)
                .hasMessageContaining("Product not found");

        verify(productRepository, never()).save(any(Product.class));
        verify(eventProducer, never()).sendProductEvent(any(ProductEvent.class));
    }

    @Test
    @DisplayName("Should send CREATE event when creating product")
    void createProductSuccessVerifyEventType() {
//     given
        when(productRepository.existsBySku(SKU)).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ArgumentCaptor<ProductEvent> eventCaptor = ArgumentCaptor.forClass(ProductEvent.class);
        doNothing().when(eventProducer).sendProductEvent(eventCaptor.capture());

//when
        productService.createProduct(productRequest);

        // Then
        ProductEvent event = eventCaptor.getValue();
        assertThat(event.getEventType()).isEqualTo("CREATE");
        assertThat(event.getId()).isEqualTo(PRODUCT_ID);
    }

    @Test
    @DisplayName("Should send UPDATE event when updating product")
    void updateProductSuccessVerifyEventType() {

        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
//        when(productRepository.existsBySku(anyString())).thenReturn(false);
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ArgumentCaptor<ProductEvent> eventCaptor = ArgumentCaptor.forClass(ProductEvent.class);
        doNothing().when(eventProducer).sendProductEvent(eventCaptor.capture());


        productService.updateProduct(PRODUCT_ID, productRequest);


        ProductEvent event = eventCaptor.getValue();
        assertThat(event.getEventType()).isEqualTo("UPDATE");
    }

    @Test
    @DisplayName("Should send DELETE event when deleting product")
    void deleteProductSuccessVerifyEventType() {
        // Given
        when(productRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);
        ArgumentCaptor<ProductEvent> eventCaptor = ArgumentCaptor.forClass(ProductEvent.class);
        doNothing().when(eventProducer).sendProductEvent(eventCaptor.capture());


        productService.deleteProduct(PRODUCT_ID);


        ProductEvent event = eventCaptor.getValue();
        assertThat(event.getEventType()).isEqualTo("DELETE");
    }
}

