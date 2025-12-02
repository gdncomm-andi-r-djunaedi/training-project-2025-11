package com.example.product.service.impl;

import com.example.product.dto.request.ProductDTO;
import com.example.product.entity.Product;
import com.example.product.repository.ProductRepository;
import org.bson.types.ObjectId;
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

import java.util.Arrays;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("ProductService Unit Tests")
class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;

    @InjectMocks
    private ProductServiceImpl productService;

    private ProductDTO productDTO;
    private Product product;
    private ObjectId productId;

    @BeforeEach
    void setUp() {
        productId = new ObjectId();
        
        productDTO = new ProductDTO();
        productDTO.setProductName("Laptop");
        productDTO.setDescription("High-performance laptop");
        productDTO.setPrice(999.99);
        productDTO.setCategory("Electronics");
        productDTO.setImages(Arrays.asList("image1.jpg", "image2.jpg"));

        product = new Product();
        product.setProductId(productId);
        product.setProductName("Laptop");
        product.setDescription("High-performance laptop");
        product.setPrice(999.99);
        product.setCategory("Electronics");
        product.setImages(Arrays.asList("image1.jpg", "image2.jpg"));
    }

    @Test
    @DisplayName("Should create product successfully")
    void testCreateProduct_Success() {
        // Given
        Product savedProduct = new Product();
        savedProduct.setProductId(productId);
        savedProduct.setProductName(productDTO.getProductName());
        savedProduct.setDescription(productDTO.getDescription());
        savedProduct.setPrice(productDTO.getPrice());
        savedProduct.setCategory(productDTO.getCategory());
        savedProduct.setImages(productDTO.getImages());

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // When
        ProductDTO result = productService.createProduct(productDTO);

        // Then
        assertNotNull(result);
        assertEquals(productDTO.getProductName(), result.getProductName());
        assertEquals(productDTO.getDescription(), result.getDescription());
        assertEquals(productDTO.getPrice(), result.getPrice());
        assertEquals(productDTO.getCategory(), result.getCategory());
        assertEquals(productDTO.getImages(), result.getImages());
        
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should get product by ID when product exists")
    void testGetProduct_WhenProductExists() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.of(product));

        // When
        ProductDTO result = productService.getProduct(productId);

        // Then
        assertNotNull(result);
        assertEquals(product.getProductName(), result.getProductName());
        assertEquals(product.getDescription(), result.getDescription());
        assertEquals(product.getPrice(), result.getPrice());
        assertEquals(product.getCategory(), result.getCategory());
        
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("Should return null when product does not exist")
    void testGetProduct_WhenProductDoesNotExist() {
        // Given
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When
        ProductDTO result = productService.getProduct(productId);

        // Then
        assertNull(result);
        verify(productRepository, times(1)).findById(productId);
    }

    @Test
    @DisplayName("Should update product successfully when product exists")
    void testUpdateProduct_WhenProductExists() {
        // Given
        ProductDTO updatedDTO = new ProductDTO();
        updatedDTO.setProductName("Updated Laptop");
        updatedDTO.setDescription("Updated description");
        updatedDTO.setPrice(1299.99);
        updatedDTO.setCategory("Electronics");
        updatedDTO.setImages(Arrays.asList("new-image.jpg"));

        Product updatedProduct = new Product();
        updatedProduct.setProductId(productId);
        updatedProduct.setProductName(updatedDTO.getProductName());
        updatedProduct.setDescription(updatedDTO.getDescription());
        updatedProduct.setPrice(updatedDTO.getPrice());
        updatedProduct.setCategory(updatedDTO.getCategory());
        updatedProduct.setImages(updatedDTO.getImages());

        when(productRepository.findById(productId)).thenReturn(Optional.of(product));
        when(productRepository.save(any(Product.class))).thenReturn(updatedProduct);

        // When
        ProductDTO result = productService.updateProduct(productId, updatedDTO);

        // Then
        assertNotNull(result);
        assertEquals(updatedDTO.getProductName(), result.getProductName());
        assertEquals(updatedDTO.getDescription(), result.getDescription());
        assertEquals(updatedDTO.getPrice(), result.getPrice());
        
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, times(1)).save(any(Product.class));
    }

    @Test
    @DisplayName("Should return null when updating non-existent product")
    void testUpdateProduct_WhenProductDoesNotExist() {
        // Given
        ProductDTO updatedDTO = new ProductDTO();
        updatedDTO.setProductName("Updated Laptop");
        
        when(productRepository.findById(productId)).thenReturn(Optional.empty());

        // When
        ProductDTO result = productService.updateProduct(productId, updatedDTO);

        // Then
        assertNull(result);
        verify(productRepository, times(1)).findById(productId);
        verify(productRepository, never()).save(any(Product.class));
    }

    @Test
    @DisplayName("Should delete product successfully")
    void testDeleteProduct_Success() {
        // Given
        doNothing().when(productRepository).deleteById(productId);

        // When
        productService.deleteProduct(productId);

        // Then
        verify(productRepository, times(1)).deleteById(productId);
    }

    @Test
    @DisplayName("Should search products by name successfully")
    void testSearchProducts_ByName_Success() {
        // Given
        String productName = "Laptop";
        Pageable pageable = PageRequest.of(0, 10);
        
        List<Product> products = Arrays.asList(
            product,
            new Product(new ObjectId(), "Laptop Pro", "Pro laptop", 1499.99, "Electronics", Arrays.asList("img1.jpg"))
        );
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        when(productRepository.findByProductName(eq(productName), eq(pageable))).thenReturn(productPage);

        // When
        List<ProductDTO> result = productService.searchProducts(productName, pageable);

        // Then
        assertNotNull(result);
        assertEquals(2, result.size());
        assertTrue(result.stream().allMatch(dto -> dto.getProductName().contains("Laptop")));
        
        verify(productRepository, times(1)).findByProductName(eq(productName), eq(pageable));
    }

    @Test
    @DisplayName("Should return empty list when no products found by name")
    void testSearchProducts_ByName_NoResults() {
        // Given
        String productName = "NonExistent";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(productRepository.findByProductName(eq(productName), eq(pageable))).thenReturn(emptyPage);

        // When
        List<ProductDTO> result = productService.searchProducts(productName, pageable);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(productRepository, times(1)).findByProductName(eq(productName), eq(pageable));
    }

    @Test
    @DisplayName("Should search products by name and category successfully")
    void testSearchProducts_ByNameAndCategory_Success() {
        // Given
        String productName = "Laptop";
        String category = "Electronics";
        Pageable pageable = PageRequest.of(0, 10);
        
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, products.size());

        when(productRepository.findByProductNameAndCategory(eq(productName), eq(category), eq(pageable)))
                .thenReturn(productPage);

        // When
        List<ProductDTO> result = productService.searchProducts(productName, category, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        assertEquals(productName, result.get(0).getProductName());
        assertEquals(category, result.get(0).getCategory());
        
        verify(productRepository, times(1))
                .findByProductNameAndCategory(eq(productName), eq(category), eq(pageable));
    }

    @Test
    @DisplayName("Should return empty list when no products found by name and category")
    void testSearchProducts_ByNameAndCategory_NoResults() {
        // Given
        String productName = "Laptop";
        String category = "Clothing";
        Pageable pageable = PageRequest.of(0, 10);
        Page<Product> emptyPage = new PageImpl<>(List.of(), pageable, 0);

        when(productRepository.findByProductNameAndCategory(eq(productName), eq(category), eq(pageable)))
                .thenReturn(emptyPage);

        // When
        List<ProductDTO> result = productService.searchProducts(productName, category, pageable);

        // Then
        assertNotNull(result);
        assertTrue(result.isEmpty());
        
        verify(productRepository, times(1))
                .findByProductNameAndCategory(eq(productName), eq(category), eq(pageable));
    }

    @Test
    @DisplayName("Should handle pagination correctly in search by name")
    void testSearchProducts_ByName_WithPagination() {
        // Given
        String productName = "Laptop";
        Pageable pageable = PageRequest.of(1, 5); // Second page, 5 items per page
        
        List<Product> products = Arrays.asList(product);
        Page<Product> productPage = new PageImpl<>(products, pageable, 10); // Total 10 items

        when(productRepository.findByProductName(eq(productName), eq(pageable))).thenReturn(productPage);

        // When
        List<ProductDTO> result = productService.searchProducts(productName, pageable);

        // Then
        assertNotNull(result);
        assertEquals(1, result.size());
        verify(productRepository, times(1)).findByProductName(eq(productName), eq(pageable));
    }

    @Test
    @DisplayName("Should handle null images in product DTO")
    void testCreateProduct_WithNullImages() {
        // Given
        ProductDTO dtoWithNullImages = new ProductDTO();
        dtoWithNullImages.setProductName("Phone");
        dtoWithNullImages.setDescription("Smartphone");
        dtoWithNullImages.setPrice(599.99);
        dtoWithNullImages.setCategory("Electronics");
        dtoWithNullImages.setImages(null);

        Product savedProduct = new Product();
        savedProduct.setProductId(productId);
        savedProduct.setProductName(dtoWithNullImages.getProductName());
        savedProduct.setDescription(dtoWithNullImages.getDescription());
        savedProduct.setPrice(dtoWithNullImages.getPrice());
        savedProduct.setCategory(dtoWithNullImages.getCategory());
        savedProduct.setImages(null);

        when(productRepository.save(any(Product.class))).thenReturn(savedProduct);

        // When
        ProductDTO result = productService.createProduct(dtoWithNullImages);

        // Then
        assertNotNull(result);
        assertEquals(dtoWithNullImages.getProductName(), result.getProductName());
        assertNull(result.getImages());
        verify(productRepository, times(1)).save(any(Product.class));
    }
}

