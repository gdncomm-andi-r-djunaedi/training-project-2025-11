package com.microservice.product;

import com.microservice.product.dto.ProductDto;
import com.microservice.product.dto.ProductResponseDto;
import com.microservice.product.entity.Product;
import com.microservice.product.exception.ResourceNotFoundException;
import com.microservice.product.exception.ValidationException;
import com.microservice.product.repository.ProductRepository;
import com.microservice.product.service.ProductEventPublisher;
import com.microservice.product.service.serviceImpl.ProductServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

	@Mock
	private ProductRepository productRepository;

	@Mock
	private ProductEventPublisher productEventPublisher;

	@InjectMocks
	private ProductServiceImpl productService;

	private Product testProduct;
	private Product testProduct2;
	private ProductDto testProductDto;
	private ProductResponseDto testProductResponseDto;
	private ProductResponseDto testProductResponseDto2;
	private Pageable pageable;


	@BeforeEach
	void setUp() {
		pageable = PageRequest.of(0, 10);

		testProduct = new Product();
		testProduct.setId(1L);
		testProduct.setSkuId("MTA-123-45679");
		testProduct.setStoreId(10001);
		testProduct.setName("Test Product");
		testProduct.setDescription("Test Description");
		testProduct.setCategory("Electronics");
		testProduct.setBrand("Test Brand");
		testProduct.setPrice(1000L);
		testProduct.setItemCode(12345L);
		testProduct.setIsActive(true);
		testProduct.setLength(10L);
		testProduct.setHeight(5L);
		testProduct.setWidth(8L);
		testProduct.setWeight(2L);
		testProduct.setDangerousLevel(1);
		testProduct.setCreatedAt(LocalDateTime.now());
		testProduct.setUpdatedAt(LocalDateTime.now());

		testProduct2 = new Product();
		testProduct2.setId(2L);
		testProduct2.setSkuId("MTA-456-78901");
		testProduct2.setStoreId(10001);
		testProduct2.setName("Test Product 2");
		testProduct2.setDescription("Test Description 2");
		testProduct2.setCategory("Electronics");
		testProduct2.setBrand("Test Brand 2");
		testProduct2.setPrice(2000L);
		testProduct2.setItemCode(12346L);
		testProduct2.setIsActive(true);
		testProduct2.setLength(15L);
		testProduct2.setHeight(6L);
		testProduct2.setWidth(9L);
		testProduct2.setWeight(3L);
		testProduct2.setDangerousLevel(2);
		testProduct2.setCreatedAt(LocalDateTime.now());
		testProduct2.setUpdatedAt(LocalDateTime.now());

		testProductDto = new ProductDto();
		testProductDto.setSkuId("MTA-123-45679");
		testProductDto.setName("Test Product");
		testProductDto.setDescription("Test Description");
		testProductDto.setCategory("Electronics");
		testProductDto.setBrand("Test Brand");
		testProductDto.setPrice(1000L);
		testProductDto.setItemCode(12345L);
		testProductDto.setLength(10L);
		testProductDto.setHeight(5L);
		testProductDto.setWidth(8L);
		testProductDto.setWeight(2L);
		testProductDto.setDangerousLevel(1);

		testProductResponseDto = new ProductResponseDto();
		testProductResponseDto.setSkuId("MTA-123-45679");
		testProductResponseDto.setStoreId(10001);
		testProductResponseDto.setName("Test Product");
		testProductResponseDto.setDescription("Test Description");
		testProductResponseDto.setCategory("Electronics");
		testProductResponseDto.setBrand("Test Brand");
		testProductResponseDto.setPrice(1000L);
		testProductResponseDto.setItemCode(12345L);
		testProductResponseDto.setIsActive(true);
		testProductResponseDto.setLength(10L);
		testProductResponseDto.setHeight(5L);
		testProductResponseDto.setWidth(8L);
		testProductResponseDto.setWeight(2L);
		testProductResponseDto.setDangerousLevel(1);

		testProductResponseDto2 = new ProductResponseDto();
		testProductResponseDto2.setSkuId("MTA-456-78901");
		testProductResponseDto2.setStoreId(10001);
		testProductResponseDto2.setName("Test Product 2");
		testProductResponseDto2.setDescription("Test Description 2");
		testProductResponseDto2.setCategory("Electronics");
		testProductResponseDto2.setBrand("Test Brand 2");
		testProductResponseDto2.setPrice(2000L);
		testProductResponseDto2.setItemCode(12346L);
		testProductResponseDto2.setIsActive(true);
		testProductResponseDto2.setLength(15L);
		testProductResponseDto2.setHeight(6L);
		testProductResponseDto2.setWidth(9L);
		testProductResponseDto2.setWeight(3L);
		testProductResponseDto2.setDangerousLevel(2);
	}

	@Test
	@DisplayName("getProducts should return paginated products successfully")
	void testGetProducts_Success() {
		List<Product> productList = new ArrayList<>();
		productList.add(testProduct);
		Page<Product> productPage = new PageImpl<>(productList, pageable, 1);
		when(productRepository.findAll(pageable)).thenReturn(productPage);

		Page<ProductResponseDto> result = productService.getProducts(pageable);

		assertNotNull(result, "Result should not be null");
		assertEquals(1, result.getTotalElements(), "Should have 1 product");
		assertEquals(1, result.getContent().size(), "Content size should be 1");
		assertEquals("MTA-123-45679", result.getContent().get(0).getSkuId(), "SKU ID should match");
		assertEquals("Test Product", result.getContent().get(0).getName(), "Name should match");
		assertEquals(10001, result.getContent().get(0).getStoreId(), "Store ID should match");
		verify(productRepository, times(1)).findAll(pageable);
	}

	@Test
	@DisplayName("getProducts should return empty page when no products exist")
	void testGetProducts_EmptyResult() {
		Page<Product> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);
		when(productRepository.findAll(pageable)).thenReturn(emptyPage);

		Page<ProductResponseDto> result = productService.getProducts(pageable);
		assertNotNull(result);
		assertEquals(0, result.getTotalElements(), "Should have 0 products");
		assertEquals(0, result.getContent().size(), "Content should be empty");
		verify(productRepository, times(1)).findAll(pageable);
	}

	@Test
	@DisplayName("getProductsBySearch should return filtered products successfully")
	void testGetProductsBySearch_Success() {
		String searchTerm = "Test";
		List<Product> productList = new ArrayList<>();
		productList.add(testProduct);
		Page<Product> productPage = new PageImpl<>(productList, pageable, 1);
		when(productRepository.findBySearchTerm(searchTerm, pageable)).thenReturn(productPage);

		Page<ProductResponseDto> result = productService.getProductsBySearch(searchTerm, pageable);
		assertNotNull(result);
		assertEquals(1, result.getTotalElements(), "Should find 1 product");
		assertEquals("MTA-123-45679", result.getContent().get(0).getSkuId());
		assertEquals("Test Product", result.getContent().get(0).getName());
		verify(productRepository, times(1)).findBySearchTerm(searchTerm, pageable);
	}

	@Test
	@DisplayName("getProductsBySearch should return empty page when no matching products found")
	void testGetProductsBySearch_EmptyResult() {
		String searchTerm = "NonExistent";
		Page<Product> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);
		when(productRepository.findBySearchTerm(searchTerm, pageable)).thenReturn(emptyPage);

		Page<ProductResponseDto> result = productService.getProductsBySearch(searchTerm, pageable);
		assertNotNull(result);
		assertEquals(0, result.getTotalElements());
		assertEquals(0, result.getContent().size());
		verify(productRepository, times(1)).findBySearchTerm(searchTerm, pageable);
	}

	@Test
	@DisplayName("getProductsById should return product when product exists")
	void testGetProductsById_Success() {
		String skuId = "MTA-123-45679";
		when(productRepository.existsBySkuId(skuId)).thenReturn(true);
		when(productRepository.findBySkuId(skuId)).thenReturn(Optional.of(testProduct));

		ProductResponseDto result = productService.getProductsById(skuId);
		assertNotNull(result);
		assertEquals("MTA-123-45679", result.getSkuId());
		assertEquals("Test Product", result.getName());
		assertEquals(10001, result.getStoreId());
		assertEquals(1000L, result.getPrice());
		verify(productRepository, times(1)).existsBySkuId(skuId);
		verify(productRepository, times(1)).findBySkuId(skuId);
	}

	@Test
	@DisplayName("getProductsById should throw ResourceNotFoundException when product does not exist")
	void testGetProductsById_ProductNotFound() {
		String skuId = "MTA-999-99999";
		when(productRepository.existsBySkuId(skuId)).thenReturn(false);

		assertThrows(ResourceNotFoundException.class, () -> {
			productService.getProductsById(skuId);
		}, "Should throw ResourceNotFoundException when product doesn't exist");
		verify(productRepository, times(1)).existsBySkuId(skuId);
		verify(productRepository, never()).findBySkuId(any());
	}

	@Test
	@DisplayName("addProduct should save and return product successfully")
	void testAddProduct_Success() {
		when(productRepository.save(any(Product.class))).thenReturn(testProduct);
		doNothing().when(productEventPublisher).publishProductCreated(any(ProductResponseDto.class));

		ProductResponseDto result = productService.addProduct(testProductDto);
		assertNotNull(result, "Result should not be null");
		assertEquals("MTA-123-45679", result.getSkuId(), "SKU ID should match");
		assertEquals("Test Product", result.getName(), "Name should match");
		assertEquals(10001, result.getStoreId(), "Store ID should match");
		assertEquals(1000L, result.getPrice(), "Price should match");
		assertEquals("Test Description", result.getDescription(), "Description should match");
		verify(productRepository, times(1)).save(any(Product.class));
		verify(productEventPublisher, times(1)).publishProductCreated(any(ProductResponseDto.class));
	}

	@Test
	@DisplayName("updateProduct should update and return product when product exists")
	void testUpdateProduct_Success() {
		String skuId = "MTA-123-45679";
		ProductDto updatedDto = new ProductDto();
		updatedDto.setSkuId("MTA-123-45679");
		updatedDto.setName("Updated Product Name");
		updatedDto.setDescription("Updated Description");
		updatedDto.setCategory("Electronics");
		updatedDto.setBrand("Updated Brand");
		updatedDto.setPrice(2000L);
		updatedDto.setItemCode(12345L);
		updatedDto.setLength(10L);
		updatedDto.setHeight(5L);
		updatedDto.setWidth(8L);
		updatedDto.setWeight(2L);
		updatedDto.setDangerousLevel(1);

		Product updatedProduct = new Product();
		updatedProduct.setId(1L);
		updatedProduct.setSkuId("MTA-123-45679");
		updatedProduct.setStoreId(10001); // Keep original storeId
		updatedProduct.setName("Updated Product Name");
		updatedProduct.setDescription("Updated Description");
		updatedProduct.setCategory("Electronics");
		updatedProduct.setBrand("Updated Brand");
		updatedProduct.setPrice(2000L);
		updatedProduct.setItemCode(12345L);
		updatedProduct.setIsActive(true); // Keep original isActive
		updatedProduct.setLength(10L);
		updatedProduct.setHeight(5L);
		updatedProduct.setWidth(8L);
		updatedProduct.setWeight(2L);
		updatedProduct.setDangerousLevel(1);
		updatedProduct.setCreatedAt(testProduct.getCreatedAt()); // Keep original createdAt
		updatedProduct.setUpdatedAt(LocalDateTime.now());

		when(productRepository.findBySkuId(skuId)).thenReturn(Optional.of(testProduct));
		when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
		doNothing().when(productEventPublisher).publishProductUpdated(any(ProductResponseDto.class));

		ProductResponseDto result = productService.updateProduct(skuId, updatedDto);

		assertNotNull(result);
		assertEquals("Updated Product Name", result.getName());
		assertEquals(2000L, result.getPrice());
		assertEquals("MTA-123-45679", result.getSkuId());
		assertEquals("Updated Description", result.getDescription());
		assertEquals("Updated Brand", result.getBrand());

		verify(productRepository, times(1)).findBySkuId(skuId);
		verify(productRepository, times(1)).save(any(Product.class));
		verify(productEventPublisher, times(1)).publishProductUpdated(any(ProductResponseDto.class));
	}

	@Test
	@DisplayName("updateProduct should throw ResourceNotFoundException when product does not exist")
	void testUpdateProduct_ProductNotFound() {
		String skuId = "MTA-999-99999";
		when(productRepository.findBySkuId(skuId)).thenReturn(Optional.empty());

		assertThrows(ResourceNotFoundException.class, () -> {
			productService.updateProduct(skuId, testProductDto);
		}, "Should throw ResourceNotFoundException when product doesn't exist");

		verify(productRepository, times(1)).findBySkuId(skuId);
		verify(productRepository, never()).save(any());
	}

	@Test
	@DisplayName("deleteById should delete product successfully")
	void testDeleteById_Success() {
		String skuId = "MTA-123-45679";
		when(productRepository.existsBySkuId(skuId)).thenReturn(true);
		doNothing().when(productRepository).deleteBySkuId(skuId);
		doNothing().when(productEventPublisher).publishProductDeleted(skuId);

		productService.deleteById(skuId);

		verify(productRepository, times(1)).existsBySkuId(skuId);
		verify(productRepository, times(1)).deleteBySkuId(skuId);
		verify(productEventPublisher, times(1)).publishProductDeleted(skuId);
	}

	@Test
	@DisplayName("deleteById should throw ResourceNotFoundException when product does not exist")
	void testDeleteById_ProductNotFound() {
		String skuId = "MTA-999-99999";
		when(productRepository.existsBySkuId(skuId)).thenReturn(false);

		assertThrows(ResourceNotFoundException.class, () -> {
			productService.deleteById(skuId);
		}, "Should throw ResourceNotFoundException when product doesn't exist");
		verify(productRepository, times(1)).existsBySkuId(skuId);
		verify(productRepository, never()).deleteBySkuId(any());
	}

	@Test
	@DisplayName("isProductIdPresent should return true when product exists")
	void testIsProductIdPresent_ProductExists() {
		String skuId = "MTA-123-45679";
		when(productRepository.existsBySkuId(skuId)).thenReturn(true);

		Boolean result = productService.isProductIdPresent(skuId);
		assertTrue(result, "Should return true when product exists");
		verify(productRepository, times(1)).existsBySkuId(skuId);
	}

	@Test
	@DisplayName("isProductIdPresent should return false when product does not exist")
	void testIsProductIdPresent_ProductDoesNotExist() {
		String skuId = "MTA-999-99999";
		when(productRepository.existsBySkuId(skuId)).thenReturn(false);

		Boolean result = productService.isProductIdPresent(skuId);
		assertFalse(result, "Should return false when product doesn't exist");
		verify(productRepository, times(1)).existsBySkuId(skuId);
	}

	@Test
	@DisplayName("getProductsBySkuIds should return list of products when SKU IDs exist")
	void testGetProductsBySkuIds_Success() {
		List<String> skuIds = Arrays.asList("MTA-123-45679", "MTA-456-78901");
		List<Product> products = Arrays.asList(testProduct, testProduct2);
		when(productRepository.findBySkuIdIn(skuIds)).thenReturn(products);

		List<ProductResponseDto> result = productService.getProductsBySkuIds(skuIds);

		assertNotNull(result);
		assertEquals(2, result.size(), "Should return 2 products");
		assertEquals("MTA-123-45679", result.get(0).getSkuId());
		assertEquals("MTA-456-78901", result.get(1).getSkuId());
		assertEquals("Test Product", result.get(0).getName());
		assertEquals("Test Product 2", result.get(1).getName());
		verify(productRepository, times(1)).findBySkuIdIn(skuIds);
	}

	@Test
	@DisplayName("getProductsBySkuIds should return single product when one SKU ID provided")
	void testGetProductsBySkuIds_SingleProduct() {
		List<String> skuIds = Arrays.asList("MTA-123-45679");
		List<Product> products = Arrays.asList(testProduct);
		when(productRepository.findBySkuIdIn(skuIds)).thenReturn(products);

		List<ProductResponseDto> result = productService.getProductsBySkuIds(skuIds);
		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals("MTA-123-45679", result.get(0).getSkuId());
		assertEquals("Test Product", result.get(0).getName());
		verify(productRepository, times(1)).findBySkuIdIn(skuIds);
	}

	@Test
	@DisplayName("getProductsBySkuIds should throw ValidationException when SKU IDs list is null")
	void testGetProductsBySkuIds_NullList() {
		assertThrows(ValidationException.class, () -> {
			productService.getProductsBySkuIds(null);
		}, "Should throw ValidationException when list is null");
		verify(productRepository, never()).findBySkuIdIn(any());
	}

	@Test
	@DisplayName("getProductsBySkuIds should throw ValidationException when SKU IDs list is empty")
	void testGetProductsBySkuIds_EmptyList() {
		List<String> emptyList = new ArrayList<>();
		assertThrows(ValidationException.class, () -> {
			productService.getProductsBySkuIds(emptyList);
		}, "Should throw ValidationException when list is empty");
		verify(productRepository, never()).findBySkuIdIn(any());
	}

	@Test
	@DisplayName("getProductsBySkuIds should throw ResourceNotFoundException when no products found")
	void testGetProductsBySkuIds_NoProductsFound() {
		List<String> skuIds = Arrays.asList("MTA-999-99999", "MTA-888-88888");
		List<Product> emptyList = new ArrayList<>();
		when(productRepository.findBySkuIdIn(skuIds)).thenReturn(emptyList);
		assertThrows(ResourceNotFoundException.class, () -> {
			productService.getProductsBySkuIds(skuIds);
		}, "Should throw ResourceNotFoundException when no products found");
		verify(productRepository, times(1)).findBySkuIdIn(skuIds);
	}

	@Test
	void testGetProductsBySkuIds_PartialMatch() {
		List<String> skuIds = Arrays.asList("MTA-123-45679", "MTA-NONEXISTENT");
		List<Product> products = Arrays.asList(testProduct);
		when(productRepository.findBySkuIdIn(skuIds)).thenReturn(products);

		List<ProductResponseDto> result = productService.getProductsBySkuIds(skuIds);
		assertNotNull(result);
		assertEquals(1, result.size(), "Should return 1 product even if one SKU doesn't exist");
		assertEquals("MTA-123-45679", result.get(0).getSkuId());
		assertEquals("Test Product", result.get(0).getName());
		verify(productRepository, times(1)).findBySkuIdIn(skuIds);
	}

	@Test
	@DisplayName("getProductsBySkuIds should handle duplicate SKU IDs in input")
	void testGetProductsBySkuIds_DuplicateSkuIds() {
		List<String> skuIds = Arrays.asList("MTA-123-45679", "MTA-123-45679", "MTA-456-78901");
		List<Product> products = Arrays.asList(testProduct, testProduct2);
		when(productRepository.findBySkuIdIn(skuIds)).thenReturn(products);

		List<ProductResponseDto> result = productService.getProductsBySkuIds(skuIds);
		assertNotNull(result);
		assertEquals(2, result.size(), "Should return 2 unique products even with duplicate SKU IDs");
		assertEquals("MTA-123-45679", result.get(0).getSkuId());
		assertEquals("MTA-456-78901", result.get(1).getSkuId());
		verify(productRepository, times(1)).findBySkuIdIn(skuIds);
	}
}