package com.example.product.service.impl;

import com.example.product.dto.GetBulkProductResponseDTO;
import com.example.product.dto.ProductRequestDTO;
import com.example.product.dto.ProductResponseDTO;
import com.example.product.entity.Product;
import com.example.product.exceptions.ProductNotFoundException;
import com.example.product.repository.ProductRepository;
import com.example.product.service.KafkaProducerService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.junit.jupiter.params.provider.CsvSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.util.Assert;

import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @Mock
    private SequenceGeneratorService sequenceGeneratorService;

    @Mock
    private KafkaProducerService kafkaProducerService;

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductRequestDTO productRequestDTO;
    private Product product;
    private long productId;

    @BeforeEach
    void setUp() {
        productId = 1L;

        productRequestDTO = new ProductRequestDTO();
        productRequestDTO.setTitle("Test Product");
        productRequestDTO.setDescription("Test Description");
        productRequestDTO.setPrice(new BigDecimal("99.99"));
        productRequestDTO.setImageUrl("test-image.jpg");
        productRequestDTO.setCategory("Electronics");

        product = Product.builder()
                .id("mongo-id-123")
                .productId(productId)
                .title("Test Product")
                .description("Test Description")
                .price(new BigDecimal("99.99"))
                .imageUrl("test-image.jpg")
                .category("Electronics")
                .markForDelete(false)
                .build();
    }

    @Test
    void createProduct_validRequest_createsAndPublishesProduct() {
        
        when(sequenceGeneratorService.getNextSequence("product_sequence")).thenReturn(productId);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        ProductResponseDTO result = productService.createProduct(productRequestDTO);

        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        assertEquals(productRequestDTO.getTitle(), result.getTitle());
        assertEquals(productRequestDTO.getDescription(), result.getDescription());
        assertEquals(productRequestDTO.getPrice(), result.getPrice());
        assertEquals(productRequestDTO.getImageUrl(), result.getImageUrl());
        assertEquals(productRequestDTO.getCategory(), result.getCategory());
        assertFalse(result.getMarkForDelete());

        verify(sequenceGeneratorService).getNextSequence("product_sequence");
        verify(productRepository).save(any(Product.class));
        verify(kafkaProducerService).publishProductCreated(any(ProductResponseDTO.class));
    }

    @Test
    void getProductByProductId_existingProduct_returnsProduct() {
        
        when(productRepository.findByProductId(productId)).thenReturn(Optional.of(product));

        ProductResponseDTO result = productService.getProductByProductId(productId);

        assertNotNull(result);
        assertEquals(productId, result.getProductId());
        assertEquals(product.getTitle(), result.getTitle());
        assertEquals(product.getDescription(), result.getDescription());
        assertEquals(product.getPrice(), result.getPrice());

        verify(productRepository).findByProductId(productId);
    }

    @Test
    void getProductsByCategory_validCategory_returnsProducts() {
        
        String category = "Electronics";
        Product product2 = Product.builder()
                .productId(2L)
                .title("Product 2")
                .category(category)
                .price(new BigDecimal("49.99"))
                .markForDelete(false)
                .build();

        List<Product> products = Arrays.asList(product, product2);
        when(productRepository.findByCategory(category)).thenReturn(products);

        List<ProductResponseDTO> result = productService.getProductsByCategory(category);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(category, result.get(0).getCategory());
        assertEquals(category, result.get(1).getCategory());

        verify(productRepository).findByCategory(category);
    }

    @Test
    void searchProductsByTitle_validTitle_returnsMatchingProducts() {
        
        String searchTerm = "Test";
        when(productRepository.findByTitleContainingIgnoreCase(searchTerm))
                .thenReturn(Collections.singletonList(product));

        List<ProductResponseDTO> result = productService.searchProductsByTitle(searchTerm);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertTrue(result.get(0).getTitle().contains(searchTerm));

        verify(productRepository).findByTitleContainingIgnoreCase(searchTerm);
    }

    @Test
    void updateProduct_validRequest_updatesAndPublishesProduct() {
        
        ProductRequestDTO updateRequest = new ProductRequestDTO();
        updateRequest.setTitle("Updated Product");
        updateRequest.setDescription("Updated Description");
        updateRequest.setPrice(new BigDecimal("149.99"));
        updateRequest.setImageUrl("updated-image.jpg");
        updateRequest.setCategory("Electronics");

        Product updatedProduct = Product.builder()
                .productId(productId)
                .title("Updated Product")
                .description("Updated Description")
                .price(new BigDecimal("149.99"))
                .imageUrl("updated-image.jpg")
                .category("Electronics")
                .markForDelete(false)
                .build();

        when(productRepository.findByProductId(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        ProductResponseDTO result = productService.updateProduct(productId, updateRequest);

        assertNotNull(result);
        assertEquals("Updated Product", result.getTitle());
        assertEquals("Updated Description", result.getDescription());
        assertEquals(new BigDecimal("149.99"), result.getPrice());

        verify(productRepository).findByProductId(productId);
        verify(productRepository).save(any(Product.class));
        verify(kafkaProducerService).publishProductUpdated(any(ProductResponseDTO.class));
    }

    @Test
    void deleteProductByProductId_existingProduct_marksAsDeletedAndPublishes() {
        
        when(productRepository.findByProductId(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        String result = productService.deleteProductByProductId(productId);

        assertEquals("Product deleted successfully", result);
        assertTrue(product.isMarkForDelete());

        verify(productRepository).findByProductId(productId);
        verify(productRepository).save(product);
        verify(kafkaProducerService).publishProductDeleted(productId);
    }

    @Test
    void getProductsInBulk_validIds_returnsProducts() {
        
        Product product2 = Product.builder()
                .productId(2L)
                .title("Product 2")
                .price(new BigDecimal("49.99"))
                .imageUrl("image2.jpg")
                .markForDelete(false)
                .build();

        List<Long> productIds = Arrays.asList(1L, 2L);
        List<Product> products = Arrays.asList(product, product2);

        when(productRepository.findByProductIdIn(productIds)).thenReturn(products);

        List<GetBulkProductResponseDTO> result = productService.getProductsInBulk(productIds);

        assertNotNull(result);
        assertEquals(2, result.size());
        assertEquals(1L, result.get(0).getProductId());
        assertEquals(2L, result.get(1).getProductId());

        verify(productRepository).findByProductIdIn(productIds);
    }

    @Test
    void searchProductsByTitle_caseInsensitive_findsProducts() {
        
        String searchTerm = "test"; // lowercase
        when(productRepository.findByTitleContainingIgnoreCase(searchTerm))
                .thenReturn(Collections.singletonList(product));

        List<ProductResponseDTO> result = productService.searchProductsByTitle(searchTerm);

        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository).findByTitleContainingIgnoreCase(searchTerm);
    }

    @Test
    void getProductsInBulk_emptyList_returnsEmptyList() {
        
        List<Long> emptyIds = Collections.emptyList();
        when(productRepository.findByProductIdIn(emptyIds)).thenReturn(Collections.emptyList());

        List<GetBulkProductResponseDTO> result = productService.getProductsInBulk(emptyIds);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository).findByProductIdIn(emptyIds);
    }

    @Test
    void getProductsByCategory_noProductsFound_returnsEmptyList() {
        
        String category = "NonExistentCategory";
        when(productRepository.findByCategory(category)).thenReturn(Collections.emptyList());

        List<ProductResponseDTO> result = productService.getProductsByCategory(category);

        assertNotNull(result);
        assertTrue(result.isEmpty());
        verify(productRepository).findByCategory(category);
    }

    @Test
    void searchProductsByTitle_noMatches_returnsEmptyList() {
        
        String searchTerm = "NonExistent";
        when(productRepository.findByTitleContainingIgnoreCase(searchTerm))
                .thenReturn(Collections.emptyList());

        List<ProductResponseDTO> result = productService.searchProductsByTitle(searchTerm);

        assertNotNull(result);
        assertTrue(result.isEmpty());
    }

    @Test
    void getProductByProductId_nonExistentProduct_throwsProductNotFoundException() {
        
        when(productRepository.findByProductId(productId)).thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.getProductByProductId(productId)
        );

        assertEquals("Product not found", exception.getMessage());
        verify(productRepository).findByProductId(productId);
    }

    @Test
    void updateProduct_deletedProduct_throwsRuntimeException() {
        
        product.setMarkForDelete(true);
        when(productRepository.findByProductId(productId)).thenReturn(Optional.of(product));

        RuntimeException exception = assertThrows(
                RuntimeException.class,
                () -> productService.updateProduct(productId, productRequestDTO)
        );

        assertEquals("cannot update the deleted product", exception.getMessage());
        verify(productRepository).findByProductId(productId);
        verify(productRepository, never()).save(any());
        verify(kafkaProducerService, never()).publishProductUpdated(any());
    }

    @Test
    void updateProduct_nonExistentProduct_throwsProductNotFoundException() {
        
        when(productRepository.findByProductId(productId)).thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.updateProduct(productId, productRequestDTO)
        );

        assertEquals("Product not found", exception.getMessage());
        verify(productRepository).findByProductId(productId);
        verify(productRepository, never()).save(any());
    }

    @Test
    void deleteProductByProductId_nonExistentProduct_throwsProductNotFoundException() {
        
        when(productRepository.findByProductId(productId)).thenReturn(Optional.empty());

        ProductNotFoundException exception = assertThrows(
                ProductNotFoundException.class,
                () -> productService.deleteProductByProductId(productId)
        );

        assertEquals("Product not found", exception.getMessage());
        verify(productRepository).findByProductId(productId);
        verify(productRepository, never()).save(any());
        verify(kafkaProducerService, never()).publishProductDeleted(anyLong());
    }

    @ParameterizedTest
    @ValueSource(strings = {"0.01", "10.99", "999.99", "9999.99"})
    void createProduct_variousPrices_createsSuccessfully(String priceValue) {
        
        BigDecimal price = new BigDecimal(priceValue);
        productRequestDTO.setPrice(price);

        Product productWithPrice = Product.builder()
                .productId(productId)
                .title(productRequestDTO.getTitle())
                .description(productRequestDTO.getDescription())
                .price(price)
                .imageUrl(productRequestDTO.getImageUrl())
                .category(productRequestDTO.getCategory())
                .markForDelete(false)
                .build();

        when(sequenceGeneratorService.getNextSequence("product_sequence")).thenReturn(productId);
        when(productRepository.save(any(Product.class))).thenReturn(productWithPrice);

        ProductResponseDTO result = productService.createProduct(productRequestDTO);

        assertNotNull(result);
        assertEquals(price, result.getPrice());
        verify(kafkaProducerService).publishProductCreated(any(ProductResponseDTO.class));
    }

    @ParameterizedTest
    @CsvSource({
            "laptop, Laptop",
            "PHONE, Phone",
            "TeSt, Test",
            "product, Product"
    })
    void searchProductsByTitle_variousSearchTerms_findsCorrectly(String searchTerm, String expectedTitle) {
        
        Product foundProduct = Product.builder()
                .productId(productId)
                .title(expectedTitle)
                .price(new BigDecimal("99.99"))
                .markForDelete(false)
                .build();

        when(productRepository.findByTitleContainingIgnoreCase(searchTerm))
                .thenReturn(Collections.singletonList(foundProduct));

        List<ProductResponseDTO> result = productService.searchProductsByTitle(searchTerm);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(expectedTitle, result.get(0).getTitle());
    }

    @ParameterizedTest
    @ValueSource(strings = {"Electronics", "Books", "Clothing", "Home & Garden"})
    void getProductsByCategory_variousCategories_returnsCorrectProducts(String category) {
        
        Product categoryProduct = Product.builder()
                .productId(productId)
                .title("Test Product")
                .category(category)
                .price(new BigDecimal("99.99"))
                .markForDelete(false)
                .build();

        when(productRepository.findByCategory(category))
                .thenReturn(Collections.singletonList(categoryProduct));

        List<ProductResponseDTO> result = productService.getProductsByCategory(category);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(category, result.get(0).getCategory());
    }

    @Test
    void createProduct_sequenceGeneration_usesCorrectSequenceName() {
        
        when(sequenceGeneratorService.getNextSequence("product_sequence")).thenReturn(productId);
        when(productRepository.save(any(Product.class))).thenReturn(product);

        productService.createProduct(productRequestDTO);

        verify(sequenceGeneratorService).getNextSequence("product_sequence");
    }

    @Test
    void deleteProduct_alreadyDeleted_stillMarksAsDeleted() {
        
        product.setMarkForDelete(true);
        when(productRepository.findByProductId(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(product);

        String result = productService.deleteProductByProductId(productId);

        assertEquals("Product deleted successfully", result);
        assertTrue(product.isMarkForDelete());
        verify(kafkaProducerService).publishProductDeleted(productId);
    }

    @Test
    void getProductsInBulk_partialResults_returnsAvailableProducts() {
        
        List<Long> requestedIds = Arrays.asList(1L, 2L, 3L);
        List<Product> availableProducts = Collections.singletonList(product);

        when(productRepository.findByProductIdIn(requestedIds)).thenReturn(availableProducts);

        List<GetBulkProductResponseDTO> result = productService.getProductsInBulk(requestedIds);

        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(1L, result.get(0).getProductId());
    }
}
