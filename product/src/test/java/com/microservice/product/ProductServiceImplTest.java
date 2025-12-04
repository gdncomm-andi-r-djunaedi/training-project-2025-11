package com.microservice.product;

import com.microservice.product.service.serviceImpl.ProductServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.microservice.product.dto.ProductDto;
import com.microservice.product.dto.ProductResponseDto;
import com.microservice.product.entity.Product;
import com.microservice.product.exception.ResourceNotFoundException;
import com.microservice.product.exception.ValidationException;
import com.microservice.product.repository.ProductRepository;
import org.junit.jupiter.api.BeforeEach;
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
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
public class ProductServiceImplTest {

	@Mock
	ProductRepository productRepository;

	@Mock
	ObjectMapper objectMapper;

	@InjectMocks
	ProductServiceImpl productService;

	private Product testProduct;
	private Product testProduct2;
	private ProductDto testProductDto;
	private ProductResponseDto testProductResponseDto;
	private ProductResponseDto testProductResponseDto2;
	private Pageable pageable;

	@BeforeEach
	void setUp() {
		pageable = PageRequest.of(0, 10);

		// Setup test Product entity
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

		// Setup second test product
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

		// Setup test ProductDto
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

		// Setup test ProductResponseDto
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

		// Setup second ProductResponseDto
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
		when(objectMapper.convertValue(testProduct, ProductResponseDto.class)).thenReturn(testProductResponseDto);

		Page<ProductResponseDto> result = productService.getProducts(pageable);
		assertNotNull(result);
		assertEquals(1, result.getTotalElements());
		assertEquals(1, result.getContent().size());
		assertEquals("MTA-123-45679", result.getContent().get(0).getSkuId());
		verify(productRepository, times(1)).findAll(pageable);
		verify(objectMapper, times(1)).convertValue(testProduct, ProductResponseDto.class);
	}

	@Test
	@DisplayName("getProductsBySearch should return filtered products successfully")
	void testGetProductsBySearch_Success() {
		String searchTerm = "Test";
		List<Product> productList = new ArrayList<>();
		productList.add(testProduct);
		Page<Product> productPage = new PageImpl<>(productList, pageable, 1);
		when(productRepository.findBySearchTerm(searchTerm, pageable)).thenReturn(productPage);
		when(objectMapper.convertValue(testProduct, ProductResponseDto.class)).thenReturn(testProductResponseDto);

		Page<ProductResponseDto> result = productService.getProductsBySearch(searchTerm, pageable);
		assertNotNull(result);
		assertEquals(1, result.getTotalElements());
		assertEquals("MTA-123-45679", result.getContent().get(0).getSkuId());
		verify(productRepository, times(1)).findBySearchTerm(searchTerm, pageable);
		verify(objectMapper, times(1)).convertValue(testProduct, ProductResponseDto.class);
	}

	@Test
	@DisplayName("getProductsById should return product when product exists")
	void testGetProductsById_Success() {
		String skuId = "MTA-123-45679";  // Changed from Long to String
		when(productRepository.existsBySkuId(skuId)).thenReturn(true);  // Changed from existsById to existsBySkuId
		when(productRepository.findBySkuId(skuId)).thenReturn(Optional.of(testProduct));  // Changed from findById to findBySkuId
		when(objectMapper.convertValue(testProduct, ProductResponseDto.class)).thenReturn(testProductResponseDto);

		ProductResponseDto result = productService.getProductsById(skuId);
		assertNotNull(result);
		assertEquals("MTA-123-45679", result.getSkuId());
		assertEquals("Test Product", result.getName());
		verify(productRepository, times(1)).existsBySkuId(skuId);  // Changed from existsById to existsBySkuId
		verify(productRepository, times(1)).findBySkuId(skuId);  // Changed from findById to findBySkuId
		verify(objectMapper, times(1)).convertValue(testProduct, ProductResponseDto.class);
	}

	@Test
	@DisplayName("getProductsById should throw ResourceNotFoundException when product does not exist")
	void testGetProductsById_ProductNotFound() {
		String skuId = "MTA-999-99999";  // Changed from Long to String
		when(productRepository.existsBySkuId(skuId)).thenReturn(false);  // Changed from existsById to existsBySkuId
		assertThrows(ResourceNotFoundException.class, () -> {
			productService.getProductsById(skuId);
		});
		verify(productRepository, times(1)).existsBySkuId(skuId);  // Changed from existsById to existsBySkuId
		verify(productRepository, never()).findBySkuId(any());  // Changed from findById to findBySkuId
	}

	@Test
	@DisplayName("addProduct should save and return product successfully")
	void testAddProduct_Success() {
		when(objectMapper.convertValue(testProductDto, Product.class)).thenReturn(testProduct);
		when(productRepository.save(testProduct)).thenReturn(testProduct);
		when(objectMapper.convertValue(testProduct, ProductResponseDto.class)).thenReturn(testProductResponseDto);

		ProductResponseDto result = productService.addProduct(testProductDto);
		assertNotNull(result);
		assertEquals("MTA-123-45679", result.getSkuId());
		assertEquals("Test Product", result.getName());
		verify(objectMapper, times(1)).convertValue(testProductDto, Product.class);
		verify(productRepository, times(1)).save(testProduct);
		verify(objectMapper, times(1)).convertValue(testProduct, ProductResponseDto.class);
	}

	@Test
	@DisplayName("updateProduct should update and return product when product exists")
	void testUpdateProduct_Success() {
		String skuId = "MTA-123-45679";  // Changed from Long to String
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
		updatedProduct.setName("Updated Product Name");
		updatedProduct.setPrice(2000L);

		ProductResponseDto updatedResponseDto = new ProductResponseDto();
		updatedResponseDto.setSkuId("MTA-123-45679");
		updatedResponseDto.setName("Updated Product Name");
		updatedResponseDto.setPrice(2000L);

		when(productRepository.findBySkuId(skuId)).thenReturn(Optional.of(testProduct));  // Changed from findById to findBySkuId
		when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);
		when(objectMapper.convertValue(updatedProduct, ProductResponseDto.class)).thenReturn(updatedResponseDto);

		ProductResponseDto result = productService.updateProduct(skuId, updatedDto);
		assertNotNull(result);
		assertEquals("Updated Product Name", result.getName());
		assertEquals(2000L, result.getPrice());
		verify(productRepository, times(1)).findBySkuId(skuId);  // Changed from findById to findBySkuId
		verify(productRepository, times(1)).save(any(Product.class));
		verify(objectMapper, times(1)).convertValue(updatedProduct, ProductResponseDto.class);
	}

	@Test
	@DisplayName("updateProduct should throw ResourceNotFoundException when product does not exist")
	void testUpdateProduct_ProductNotFound() {
		String skuId = "MTA-999-99999";  // Changed from Long to String
		when(productRepository.findBySkuId(skuId)).thenReturn(Optional.empty());  // Changed from findById to findBySkuId

		assertThrows(ResourceNotFoundException.class, () -> {
			productService.updateProduct(skuId, testProductDto);
		});
		verify(productRepository, times(1)).findBySkuId(skuId);  // Changed from findById to findBySkuId
		verify(productRepository, never()).save(any());
	}

	@Test
	@DisplayName("deleteById should delete product successfully")
	void testDeleteById_Success() {
		String skuId = "MTA-123-45679";  // Changed from Long to String
		doNothing().when(productRepository).deleteBySkuId(skuId);  // Changed from deleteById to deleteBySkuId
		productService.deleteById(skuId);
		verify(productRepository, times(1)).deleteBySkuId(skuId);  // Changed from deleteById to deleteBySkuId
	}

	@Test
	@DisplayName("isProductIdPresent should return true when product exists")
	void testIsProductIdPresent_ProductExists() {
		String skuId = "MTA-123-45679";  // Changed from Long to String
		when(productRepository.existsBySkuId(skuId)).thenReturn(true);  // Changed from existsById to existsBySkuId
		Boolean result = productService.isProductIdPresent(skuId);
		assertTrue(result);
		verify(productRepository, times(1)).existsBySkuId(skuId);  // Changed from existsById to existsBySkuId
	}

	@Test
	@DisplayName("isProductIdPresent should return false when product does not exist")
	void testIsProductIdPresent_ProductDoesNotExist() {
		String skuId = "MTA-999-99999";  // Changed from Long to String
		when(productRepository.existsBySkuId(skuId)).thenReturn(false);  // Changed from existsById to existsBySkuId
		Boolean result = productService.isProductIdPresent(skuId);
		assertFalse(result);
		verify(productRepository, times(1)).existsBySkuId(skuId);  // Changed from existsById to existsBySkuId
	}

	@Test
	@DisplayName("getProducts should return empty page when no products exist")
	void testGetProducts_EmptyResult() {
		Page<Product> emptyPage = new PageImpl<>(new ArrayList<>(), pageable, 0);
		when(productRepository.findAll(pageable)).thenReturn(emptyPage);

		Page<ProductResponseDto> result = productService.getProducts(pageable);
		assertNotNull(result);
		assertEquals(0, result.getTotalElements());
		assertEquals(0, result.getContent().size());
		verify(productRepository, times(1)).findAll(pageable);
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

	// Test cases for getProductsBySkuIds method

	@Test
	@DisplayName("getProductsBySkuIds should return list of products when SKU IDs exist")
	void testGetProductsBySkuIds_Success() {
		List<String> skuIds = Arrays.asList("MTA-123-45679", "MTA-456-78901");
		List<Product> products = Arrays.asList(testProduct, testProduct2);

		when(productRepository.findBySkuIdIn(skuIds)).thenReturn(products);
		when(objectMapper.convertValue(testProduct, ProductResponseDto.class)).thenReturn(testProductResponseDto);
		when(objectMapper.convertValue(testProduct2, ProductResponseDto.class)).thenReturn(testProductResponseDto2);

		List<ProductResponseDto> result = productService.getProductsBySkuIds(skuIds);

		assertNotNull(result);
		assertEquals(2, result.size());
		assertEquals("MTA-123-45679", result.get(0).getSkuId());
		assertEquals("MTA-456-78901", result.get(1).getSkuId());
		verify(productRepository, times(1)).findBySkuIdIn(skuIds);
		verify(objectMapper, times(1)).convertValue(testProduct, ProductResponseDto.class);
		verify(objectMapper, times(1)).convertValue(testProduct2, ProductResponseDto.class);
	}

	@Test
	@DisplayName("getProductsBySkuIds should return single product when one SKU ID provided")
	void testGetProductsBySkuIds_SingleProduct() {
		List<String> skuIds = Arrays.asList("MTA-123-45679");
		List<Product> products = Arrays.asList(testProduct);

		when(productRepository.findBySkuIdIn(skuIds)).thenReturn(products);
		when(objectMapper.convertValue(testProduct, ProductResponseDto.class)).thenReturn(testProductResponseDto);

		List<ProductResponseDto> result = productService.getProductsBySkuIds(skuIds);

		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals("MTA-123-45679", result.get(0).getSkuId());
		assertEquals("Test Product", result.get(0).getName());
		verify(productRepository, times(1)).findBySkuIdIn(skuIds);
		verify(objectMapper, times(1)).convertValue(testProduct, ProductResponseDto.class);
	}

	@Test
	@DisplayName("getProductsBySkuIds should throw ValidationException when SKU IDs list is null")
	void testGetProductsBySkuIds_NullList() {
		assertThrows(ValidationException.class, () -> {
			productService.getProductsBySkuIds(null);
		});
		verify(productRepository, never()).findBySkuIdIn(any());
	}

	@Test
	@DisplayName("getProductsBySkuIds should throw ValidationException when SKU IDs list is empty")
	void testGetProductsBySkuIds_EmptyList() {
		List<String> emptyList = new ArrayList<>();
		assertThrows(ValidationException.class, () -> {
			productService.getProductsBySkuIds(emptyList);
		});
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
		});
		verify(productRepository, times(1)).findBySkuIdIn(skuIds);
//		check this
		verify(objectMapper, never()).convertValue((Object) any(), (Class<Object>) any());
	}

	@Test
	@DisplayName("getProductsBySkuIds should return partial results when some SKU IDs don't exist")
	void testGetProductsBySkuIds_PartialMatch() {
		List<String> skuIds = Arrays.asList("MTA-123-45679", "MTA-NONEXISTENT");
		List<Product> products = Arrays.asList(testProduct); // Only one product found

		when(productRepository.findBySkuIdIn(skuIds)).thenReturn(products);
		when(objectMapper.convertValue(testProduct, ProductResponseDto.class)).thenReturn(testProductResponseDto);

		List<ProductResponseDto> result = productService.getProductsBySkuIds(skuIds);

		assertNotNull(result);
		assertEquals(1, result.size());
		assertEquals("MTA-123-45679", result.get(0).getSkuId());
		verify(productRepository, times(1)).findBySkuIdIn(skuIds);
		verify(objectMapper, times(1)).convertValue(testProduct, ProductResponseDto.class);
	}

	@Test
	@DisplayName("getProductsBySkuIds should handle duplicate SKU IDs in input")
	void testGetProductsBySkuIds_DuplicateSkuIds() {
		List<String> skuIds = Arrays.asList("MTA-123-45679", "MTA-123-45679", "MTA-456-78901");
		List<Product> products = Arrays.asList(testProduct, testProduct2);

		when(productRepository.findBySkuIdIn(skuIds)).thenReturn(products);
		when(objectMapper.convertValue(testProduct, ProductResponseDto.class)).thenReturn(testProductResponseDto);
		when(objectMapper.convertValue(testProduct2, ProductResponseDto.class)).thenReturn(testProductResponseDto2);

		List<ProductResponseDto> result = productService.getProductsBySkuIds(skuIds);

		assertNotNull(result);
		assertEquals(2, result.size());
		verify(productRepository, times(1)).findBySkuIdIn(skuIds);
	}
}