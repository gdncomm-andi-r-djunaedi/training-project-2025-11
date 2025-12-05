package com.zasura.product.service;

import com.zasura.product.dto.Pagination;
import com.zasura.product.dto.ProductSearchRequest;
import com.zasura.product.entity.Product;
import com.zasura.product.exception.ProductNotFoundException;
import com.zasura.product.repository.ProductRepository;
import org.bson.types.ObjectId;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@DisplayName("ProductServiceImpl Tests")
class ProductServiceImplTest {

  @Mock
  private ProductRepository productRepository;

  @Mock
  private MongoTemplate mongoTemplate;

  @InjectMocks
  private ProductServiceImpl productService;

  private Product testProduct;
  private ObjectId testProductId;

  @BeforeEach
  void setUp() {
    testProductId = new ObjectId();
    testProduct = Product.builder()
        .id(testProductId)
        .name("Test Product")
        .description("Test Description")
        .price(99.99)
        .createdDate(LocalDateTime.now())
        .lastModifiedDate(LocalDateTime.now())
        .build();
  }

  @Nested
  @DisplayName("getProductDetail Tests")
  class GetProductDetailTests {

    @Test
    @DisplayName("Should return product when product exists")
    void testGetProductDetailSuccess() {
      // Given
      String productId = testProductId.toHexString();
      when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

      // When
      Product result = productService.getProductDetail(productId);

      // Then
      assertNotNull(result);
      assertEquals(testProduct.getId(), result.getId());
      assertEquals(testProduct.getName(), result.getName());
      assertEquals(testProduct.getDescription(), result.getDescription());
      assertEquals(testProduct.getPrice(), result.getPrice());
      verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("Should throw ProductNotFoundException when product does not exist")
    void testGetProductDetailNotFound() {
      // Given
      String productId = testProductId.toHexString();
      when(productRepository.findById(productId)).thenReturn(Optional.empty());

      // When & Then
      ProductNotFoundException exception = assertThrows(ProductNotFoundException.class,
          () -> productService.getProductDetail(productId));

      assertTrue(exception.getMessage().contains(productId));
      assertTrue(exception.getMessage().contains("not found"));
      verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("Should handle null product ID")
    void testGetProductDetailWithNullId() {
      // Given
      when(productRepository.findById(null)).thenReturn(Optional.empty());

      // When & Then
      assertThrows(ProductNotFoundException.class, () -> productService.getProductDetail(null));
      verify(productRepository, times(1)).findById(null);
    }

    @Test
    @DisplayName("Should handle empty product ID")
    void testGetProductDetailWithEmptyId() {
      // Given
      String emptyId = "";
      when(productRepository.findById(emptyId)).thenReturn(Optional.empty());

      // When & Then
      assertThrows(ProductNotFoundException.class, () -> productService.getProductDetail(emptyId));
      verify(productRepository, times(1)).findById(emptyId);
    }
  }


  @Nested
  @DisplayName("createProduct Tests")
  class CreateProductTests {

    @Test
    @DisplayName("Should create product successfully")
    void testCreateProductSuccess() {
      // Given
      Product newProduct = Product.builder()
          .name("New Product")
          .description("New Description")
          .price(149.99)
          .build();

      Product savedProduct = Product.builder()
          .id(new ObjectId())
          .name("New Product")
          .description("New Description")
          .price(149.99)
          .createdDate(LocalDateTime.now())
          .lastModifiedDate(LocalDateTime.now())
          .build();

      when(productRepository.save(newProduct)).thenReturn(savedProduct);

      // When
      Product result = productService.createProduct(newProduct);

      // Then
      assertNotNull(result);
      assertNotNull(result.getId());
      assertEquals(savedProduct.getName(), result.getName());
      assertEquals(savedProduct.getDescription(), result.getDescription());
      assertEquals(savedProduct.getPrice(), result.getPrice());
      verify(productRepository, times(1)).save(newProduct);
    }

    @Test
    @DisplayName("Should create product with minimal fields")
    void testCreateProductWithMinimalFields() {
      // Given
      Product minimalProduct = Product.builder().name("Minimal Product").price(0.0).build();

      Product savedProduct =
          Product.builder().id(new ObjectId()).name("Minimal Product").price(0.0).build();

      when(productRepository.save(minimalProduct)).thenReturn(savedProduct);

      // When
      Product result = productService.createProduct(minimalProduct);

      // Then
      assertNotNull(result);
      assertNotNull(result.getId());
      assertEquals(savedProduct.getName(), result.getName());
      assertNull(result.getDescription());
      assertEquals(savedProduct.getPrice(), result.getPrice());
      verify(productRepository, times(1)).save(minimalProduct);
    }

    @Test
    @DisplayName("Should create product with all fields")
    void testCreateProductWithAllFields() {
      // Given
      when(productRepository.save(testProduct)).thenReturn(testProduct);

      // When
      Product result = productService.createProduct(testProduct);

      // Then
      assertNotNull(result);
      assertEquals(testProduct.getId(), result.getId());
      assertEquals(testProduct.getName(), result.getName());
      assertEquals(testProduct.getDescription(), result.getDescription());
      assertEquals(testProduct.getPrice(), result.getPrice());
      assertEquals(testProduct.getCreatedDate(), result.getCreatedDate());
      assertEquals(testProduct.getLastModifiedDate(), result.getLastModifiedDate());
      verify(productRepository, times(1)).save(testProduct);
    }
  }


  @Nested
  @DisplayName("searchProducts Tests")
  class SearchProductsTests {

    @Test
    @DisplayName("Should search products with name filter")
    void testSearchProductsWithNameFilter() {
      // Given
      ProductSearchRequest searchRequest =
          ProductSearchRequest.builder().name("Test").pagination(new Pagination(0, 10)).build();

      List<Product> products = Arrays.asList(testProduct);

      when(mongoTemplate.find(any(Query.class), eq(Product.class))).thenReturn(products);
      when(mongoTemplate.count(any(Query.class), eq(Product.class))).thenReturn(1L);

      // When
      Page<Product> result = productService.searchProducts(searchRequest);

      // Then
      assertNotNull(result);
      assertEquals(1, result.getTotalElements());
      assertEquals(1, result.getContent().size());
      assertEquals(testProduct.getName(), result.getContent().get(0).getName());
      verify(mongoTemplate, times(1)).find(any(Query.class), eq(Product.class));
    }

    @Test
    @DisplayName("Should search products with description filter")
    void testSearchProductsWithDescriptionFilter() {
      // Given
      ProductSearchRequest searchRequest = ProductSearchRequest.builder()
          .description("Description")
          .pagination(new Pagination(0, 10))
          .build();

      List<Product> products = Arrays.asList(testProduct);

      when(mongoTemplate.find(any(Query.class), eq(Product.class))).thenReturn(products);
      when(mongoTemplate.count(any(Query.class), eq(Product.class))).thenReturn(1L);

      // When
      Page<Product> result = productService.searchProducts(searchRequest);

      // Then
      assertNotNull(result);
      assertEquals(1, result.getTotalElements());
      assertEquals(1, result.getContent().size());
      verify(mongoTemplate, times(1)).find(any(Query.class), eq(Product.class));
    }

    @Test
    @DisplayName("Should search products with min price filter")
    void testSearchProductsWithMinPriceFilter() {
      // Given
      ProductSearchRequest searchRequest =
          ProductSearchRequest.builder().minPrice(50.0).pagination(new Pagination(0, 10)).build();

      List<Product> products = Arrays.asList(testProduct);

      when(mongoTemplate.find(any(Query.class), eq(Product.class))).thenReturn(products);
      when(mongoTemplate.count(any(Query.class), eq(Product.class))).thenReturn(1L);

      // When
      Page<Product> result = productService.searchProducts(searchRequest);

      // Then
      assertNotNull(result);
      assertEquals(1, result.getTotalElements());
      verify(mongoTemplate, times(1)).find(any(Query.class), eq(Product.class));
    }

    @Test
    @DisplayName("Should search products with max price filter")
    void testSearchProductsWithMaxPriceFilter() {
      // Given
      ProductSearchRequest searchRequest =
          ProductSearchRequest.builder().maxPrice(150.0).pagination(new Pagination(0, 10)).build();

      List<Product> products = Arrays.asList(testProduct);

      when(mongoTemplate.find(any(Query.class), eq(Product.class))).thenReturn(products);
      when(mongoTemplate.count(any(Query.class), eq(Product.class))).thenReturn(1L);

      // When
      Page<Product> result = productService.searchProducts(searchRequest);

      // Then
      assertNotNull(result);
      assertEquals(1, result.getTotalElements());
      verify(mongoTemplate, times(1)).find(any(Query.class), eq(Product.class));
    }

    @Test
    @DisplayName("Should search products with price range filter")
    void testSearchProductsWithPriceRangeFilter() {
      // Given
      ProductSearchRequest searchRequest = ProductSearchRequest.builder()
          .minPrice(50.0)
          .maxPrice(150.0)
          .pagination(new Pagination(0, 10))
          .build();

      List<Product> products = Arrays.asList(testProduct);

      when(mongoTemplate.find(any(Query.class), eq(Product.class))).thenReturn(products);
      when(mongoTemplate.count(any(Query.class), eq(Product.class))).thenReturn(1L);

      // When
      Page<Product> result = productService.searchProducts(searchRequest);

      // Then
      assertNotNull(result);
      assertEquals(1, result.getTotalElements());
      verify(mongoTemplate, times(1)).find(any(Query.class), eq(Product.class));
    }

    @Test
    @DisplayName("Should search products with all filters")
    void testSearchProductsWithAllFilters() {
      // Given
      ProductSearchRequest searchRequest = ProductSearchRequest.builder()
          .name("Test")
          .description("Description")
          .minPrice(50.0)
          .maxPrice(150.0)
          .pagination(new Pagination(0, 10))
          .build();

      List<Product> products = Arrays.asList(testProduct);

      when(mongoTemplate.find(any(Query.class), eq(Product.class))).thenReturn(products);
      when(mongoTemplate.count(any(Query.class), eq(Product.class))).thenReturn(1L);

      // When
      Page<Product> result = productService.searchProducts(searchRequest);

      // Then
      assertNotNull(result);
      assertEquals(1, result.getTotalElements());
      assertEquals(1, result.getContent().size());
      verify(mongoTemplate, times(1)).find(any(Query.class), eq(Product.class));
    }

    @Test
    @DisplayName("Should search products with no filters")
    void testSearchProductsWithNoFilters() {
      // Given
      ProductSearchRequest searchRequest =
          ProductSearchRequest.builder().pagination(new Pagination(0, 10)).build();

      List<Product> products = Arrays.asList(testProduct);

      when(mongoTemplate.find(any(Query.class), eq(Product.class))).thenReturn(products);
      when(mongoTemplate.count(any(Query.class), eq(Product.class))).thenReturn(1L);

      // When
      Page<Product> result = productService.searchProducts(searchRequest);

      // Then
      assertNotNull(result);
      assertEquals(1, result.getTotalElements());
      verify(mongoTemplate, times(1)).find(any(Query.class), eq(Product.class));
    }

    @Test
    @DisplayName("Should return empty page when no products match")
    void testSearchProductsNoResults() {
      // Given
      ProductSearchRequest searchRequest = ProductSearchRequest.builder()
          .name("NonExistent")
          .pagination(new Pagination(0, 10))
          .build();

      when(mongoTemplate.find(any(Query.class),
          eq(Product.class))).thenReturn(Collections.emptyList());
      when(mongoTemplate.count(any(Query.class), eq(Product.class))).thenReturn(0L);

      // When
      Page<Product> result = productService.searchProducts(searchRequest);

      // Then
      assertNotNull(result);
      assertEquals(0, result.getTotalElements());
      assertTrue(result.getContent().isEmpty());
      verify(mongoTemplate, times(1)).find(any(Query.class), eq(Product.class));
    }

    @Test
    @DisplayName("Should search products with custom pagination")
    void testSearchProductsWithCustomPagination() {
      // Given
      ProductSearchRequest searchRequest =
          ProductSearchRequest.builder().name("Test").pagination(new Pagination(0, 5)).build();

      List<Product> products = Arrays.asList(testProduct);

      when(mongoTemplate.find(any(Query.class), eq(Product.class))).thenReturn(products);
      when(mongoTemplate.count(any(Query.class), eq(Product.class))).thenReturn(10L);

      // When
      Page<Product> result = productService.searchProducts(searchRequest);

      // Then
      assertNotNull(result);
      assertEquals(0, result.getNumber());
      assertEquals(5, result.getSize());
      verify(mongoTemplate, times(1)).find(any(Query.class), eq(Product.class));
    }

    @Test
    @DisplayName("Should search products with multiple results")
    void testSearchProductsMultipleResults() {
      // Given
      Product product2 = Product.builder()
          .id(new ObjectId())
          .name("Test Product 2")
          .description("Another Description")
          .price(199.99)
          .build();

      Product product3 = Product.builder()
          .id(new ObjectId())
          .name("Test Product 3")
          .description("Third Description")
          .price(299.99)
          .build();

      ProductSearchRequest searchRequest =
          ProductSearchRequest.builder().name("Test").pagination(new Pagination(0, 10)).build();

      List<Product> products = Arrays.asList(testProduct, product2, product3);

      when(mongoTemplate.find(any(Query.class), eq(Product.class))).thenReturn(products);
      when(mongoTemplate.count(any(Query.class), eq(Product.class))).thenReturn(3L);

      // When
      Page<Product> result = productService.searchProducts(searchRequest);

      // Then
      assertNotNull(result);
      assertEquals(3, result.getTotalElements());
      assertEquals(3, result.getContent().size());
      verify(mongoTemplate, times(1)).find(any(Query.class), eq(Product.class));
    }

    @Test
    @DisplayName("Should handle empty name filter")
    void testSearchProductsWithEmptyName() {
      // Given
      ProductSearchRequest searchRequest =
          ProductSearchRequest.builder().name("").pagination(new Pagination(0, 10)).build();

      List<Product> products = Arrays.asList(testProduct);

      when(mongoTemplate.find(any(Query.class), eq(Product.class))).thenReturn(products);
      when(mongoTemplate.count(any(Query.class), eq(Product.class))).thenReturn(1L);

      // When
      Page<Product> result = productService.searchProducts(searchRequest);

      // Then
      assertNotNull(result);
      assertEquals(1, result.getTotalElements());
      verify(mongoTemplate, times(1)).find(any(Query.class), eq(Product.class));
    }

    @Test
    @DisplayName("Should handle empty description filter")
    void testSearchProductsWithEmptyDescription() {
      // Given
      ProductSearchRequest searchRequest =
          ProductSearchRequest.builder().description("").pagination(new Pagination(0, 10)).build();

      List<Product> products = Arrays.asList(testProduct);

      when(mongoTemplate.find(any(Query.class), eq(Product.class))).thenReturn(products);
      when(mongoTemplate.count(any(Query.class), eq(Product.class))).thenReturn(1L);

      // When
      Page<Product> result = productService.searchProducts(searchRequest);

      // Then
      assertNotNull(result);
      assertEquals(1, result.getTotalElements());
      verify(mongoTemplate, times(1)).find(any(Query.class), eq(Product.class));
    }

    @Test
    @DisplayName("Should search products with zero min price")
    void testSearchProductsWithZeroMinPrice() {
      // Given
      ProductSearchRequest searchRequest =
          ProductSearchRequest.builder().minPrice(0.0).pagination(new Pagination(0, 10)).build();

      List<Product> products = Arrays.asList(testProduct);

      when(mongoTemplate.find(any(Query.class), eq(Product.class))).thenReturn(products);
      when(mongoTemplate.count(any(Query.class), eq(Product.class))).thenReturn(1L);

      // When
      Page<Product> result = productService.searchProducts(searchRequest);

      // Then
      assertNotNull(result);
      assertEquals(1, result.getTotalElements());
      verify(mongoTemplate, times(1)).find(any(Query.class), eq(Product.class));
    }

    @Test
    @DisplayName("Should handle first page pagination")
    void testSearchProductsFirstPage() {
      // Given
      ProductSearchRequest searchRequest =
          ProductSearchRequest.builder().pagination(new Pagination(0, 10)).build();

      List<Product> products = Arrays.asList(testProduct);

      when(mongoTemplate.find(any(Query.class), eq(Product.class))).thenReturn(products);
      when(mongoTemplate.count(any(Query.class), eq(Product.class))).thenReturn(1L);

      // When
      Page<Product> result = productService.searchProducts(searchRequest);

      // Then
      assertNotNull(result);
      assertEquals(0, result.getNumber());
      assertTrue(result.isFirst());
      verify(mongoTemplate, times(1)).find(any(Query.class), eq(Product.class));
    }
  }


  @Nested
  @DisplayName("Integration Tests")
  class IntegrationTests {

    @Test
    @DisplayName("Should create and retrieve product")
    void testCreateAndRetrieveProduct() {
      // Given
      Product newProduct = Product.builder()
          .name("Integration Test Product")
          .description("Integration Description")
          .price(199.99)
          .build();

      Product savedProduct = Product.builder()
          .id(testProductId)
          .name("Integration Test Product")
          .description("Integration Description")
          .price(199.99)
          .createdDate(LocalDateTime.now())
          .lastModifiedDate(LocalDateTime.now())
          .build();

      when(productRepository.save(newProduct)).thenReturn(savedProduct);
      when(productRepository.findById(testProductId.toHexString())).thenReturn(Optional.of(
          savedProduct));

      // When
      Product created = productService.createProduct(newProduct);
      Product retrieved = productService.getProductDetail(testProductId.toHexString());

      // Then
      assertNotNull(created);
      assertNotNull(retrieved);
      assertEquals(created.getId(), retrieved.getId());
      assertEquals(created.getName(), retrieved.getName());
      verify(productRepository, times(1)).save(newProduct);
      verify(productRepository, times(1)).findById(testProductId.toHexString());
    }

    @Test
    @DisplayName("Should verify all mocks are used correctly")
    void testMockInteractions() {
      // Given
      String productId = testProductId.toHexString();
      when(productRepository.findById(productId)).thenReturn(Optional.of(testProduct));

      // When
      productService.getProductDetail(productId);

      // Then
      verify(productRepository).findById(productId);
      verifyNoMoreInteractions(productRepository);
      verifyNoInteractions(mongoTemplate);
    }
  }
}