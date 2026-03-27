package com.example.product.serviceImplTest;

import com.example.product.dto.ProductListResponse;
import com.example.product.dto.ProductRequest;
import com.example.product.dto.ProductResponse;
import com.example.product.dto.UpdateProductRequest;
import com.example.product.entity.ProductEntity;
import com.example.product.exception.BusinessException;
import com.example.product.repository.ProductRepository;
import com.example.product.service.ProductServiceImp;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;
import static org.mockito.MockitoAnnotations.initMocks;

public class ProductServiceImplTest {

    @Mock
    private ProductRepository productRepository;
    @Mock
    private KafkaTemplate kafkaTemplate;
    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ProductServiceImp productServiceImp;

    @BeforeEach
    public void setUp(){
        initMocks(this);
    }

    @Test
    public void addProductTest(){
        List<ProductRequest> productRequests = new ArrayList<>();
        ProductRequest productRequest = new ProductRequest();
        productRequest.setProductName("laptop");
        productRequest.setProductDescription("new version of mac");
        productRequest.setProductPrice(12.3);
        productRequest.setItemSku("MYU-16636");
        productRequests.add(productRequest);
        when(this.productRepository.existsByItemSku("MYU-16636")).thenReturn(false);
        when(this.productRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<ProductEntity> entities = invocation.getArgument(0);
            return entities;
        });
        List<ProductResponse> productResponses1 = this.productServiceImp.addProducts(productRequests);
        assertEquals("MYU-16636", productResponses1.getFirst().getItemSku());
        assertEquals("laptop", productResponses1.getFirst().getProductName());
        assertEquals("new version of mac", productResponses1.getFirst().getProductDescription());
        assertEquals(12.3, productResponses1.getFirst().getProductPrice());
    }

    @Test
    public void addDuplicateProductTest(){
        List<ProductRequest> productRequests = new ArrayList<>();
        ProductRequest productRequest = new ProductRequest();
        productRequest.setProductName("laptop");
        productRequest.setProductDescription("new version of mac");
        productRequest.setProductPrice(12.3);
        productRequest.setItemSku("MYU-16636");
        productRequests.add(productRequest);
        when(this.productRepository.existsByItemSku("MYU-16636")).thenReturn(true);
        when(this.productRepository.saveAll(anyList())).thenAnswer(invocation -> {
            List<ProductEntity> entities = invocation.getArgument(0);
            return entities;
        });
        List<ProductResponse> productResponses1 = this.productServiceImp.addProducts(productRequests);
        assertEquals(0,productResponses1.stream().count());
    }

    @Test
    public void getProductsListingTest(){
        List<ProductEntity> productEntities = new ArrayList<>();
        ProductEntity productEntity = new ProductEntity();
        productEntity.setId("test-id-1");
        productEntity.setProductName("laptop");
        productEntity.setProductDescription("new version of mac");
        productEntity.setProductPrice(12.3);
        productEntity.setItemSku("MYU-16636");
        productEntities.add(productEntity);
        Page<ProductEntity> productPage = new PageImpl<>(productEntities, PageRequest.of(0, 10), 1);
        when(this.productRepository.findAll(any(Pageable.class))).thenReturn(productPage);
        ProductListResponse response = this.productServiceImp.getProductsListing(0, 10);
        assertEquals(1, response.getProducts().size());
        assertEquals("MYU-16636", response.getProducts().getFirst().getItemSku());
        assertEquals(0, response.getCurrentPage());
        assertEquals(10, response.getPageSize());
        assertEquals(1, response.getTotalElements());
    }

    @Test
    public void getProductsListingWithNegativePageNumberTest(){
        assertThrows(IllegalArgumentException.class, () -> {
            this.productServiceImp.getProductsListing(-1, 10);
        });
    }

    @Test
    public void getProductsListingWithInvalidPageSizeTest(){
        assertThrows(IllegalArgumentException.class, () -> {
            this.productServiceImp.getProductsListing(0, 0);
        });
    }

    @Test
    public void getProductDetailByItemSkuTest(){
        ProductEntity productEntity = new ProductEntity();
        productEntity.setId("test-id-1");
        productEntity.setProductName("laptop");
        productEntity.setProductDescription("new version of mac");
        productEntity.setProductPrice(12.3);
        productEntity.setItemSku("MYU-16636");
        when(this.productRepository.findByItemSku("MYU-16636")).thenReturn(Optional.of(productEntity));
        ProductResponse response = this.productServiceImp.getProductDetailByItemSku("MYU-16636");
        assertEquals("MYU-16636", response.getItemSku());
        assertEquals("laptop", response.getProductName());
        assertEquals("new version of mac", response.getProductDescription());
        assertEquals(12.3, response.getProductPrice());
    }

    @Test
    public void getProductDetailByItemSkuNotFoundTest(){
        when(this.productRepository.findByItemSku("INVALID-SKU")).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> {
            this.productServiceImp.getProductDetailByItemSku("INVALID-SKU");
        });
    }

    @Test
    public void getProductDetailByItemSkuWithNullItemSkuTest(){
        assertThrows(IllegalArgumentException.class, () -> {
            this.productServiceImp.getProductDetailByItemSku(null);
        });
    }

    @Test
    public void getProductDetailByItemSkuWithEmptyItemSkuTest(){
        assertThrows(IllegalArgumentException.class, () -> {
            this.productServiceImp.getProductDetailByItemSku("   ");
        });
    }

    @Test
    public void getProductsBySearchTermTest(){
        List<ProductEntity> productEntities = new ArrayList<>();
        ProductEntity productEntity = new ProductEntity();
        productEntity.setId("test-id-1");
        productEntity.setProductName("laptop");
        productEntity.setProductDescription("new version of mac");
        productEntity.setProductPrice(12.3);
        productEntity.setItemSku("MYU-16636");
        productEntities.add(productEntity);
        Page<ProductEntity> productPage = new PageImpl<>(productEntities, PageRequest.of(0, 10), 1);
        when(this.productRepository.findBySearchTerm(anyString(), any(Pageable.class))).thenReturn(productPage);
        ProductListResponse response = this.productServiceImp.getProductsBySearchTerm("laptop", 0, 10);
        assertEquals(1, response.getProducts().size());
        assertEquals("MYU-16636", response.getProducts().getFirst().getItemSku());
    }

    @Test
    public void getProductsBySearchTermWithNullSearchTermTest(){
        assertThrows(IllegalArgumentException.class, () -> {
            this.productServiceImp.getProductsBySearchTerm(null, 0, 10);
        });
    }

    @Test
    public void getProductsBySearchTermWithEmptySearchTermTest(){
        assertThrows(IllegalArgumentException.class, () -> {
            this.productServiceImp.getProductsBySearchTerm("   ", 0, 10);
        });
    }

    @Test
    public void buildWildcardRegexWithAsteriskTest(){
        String result = this.productServiceImp.buildWildcardRegex("lap*top");
        assertEquals("lap.*top", result);
    }

    @Test
    public void buildWildcardRegexWithQuestionMarkTest(){
        String result = this.productServiceImp.buildWildcardRegex("lap?top");
        assertEquals("lap.top", result);
    }

    @Test
    public void buildWildcardRegexWithoutWildcardsTest(){
        String result = this.productServiceImp.buildWildcardRegex("laptop");
        assertEquals(".*laptop.*", result);
    }

    @Test
    public void buildWildcardRegexWithSpecialCharactersTest(){
        String result = this.productServiceImp.buildWildcardRegex("lap.top");
        assertEquals(".*lap\\.top.*", result);
    }

    @Test
    public void buildProductListResponseTest(){
        List<ProductEntity> productEntities = new ArrayList<>();
        ProductEntity productEntity = new ProductEntity();
        productEntity.setId("test-id-1");
        productEntity.setProductName("laptop");
        productEntity.setProductDescription("new version of mac");
        productEntity.setProductPrice(12.3);
        productEntity.setItemSku("MYU-16636");
        productEntities.add(productEntity);
        Page<ProductEntity> productPage = new PageImpl<>(productEntities, PageRequest.of(0, 10), 1);
        ProductListResponse response = this.productServiceImp.buildProductListResponse(productPage);
        assertEquals(1, response.getProducts().size());
        assertEquals(0, response.getCurrentPage());
        assertEquals(10, response.getPageSize());
        assertEquals(1, response.getTotalElements());
        assertEquals(1, response.getTotalPages());
        assertFalse(response.isHasNext());
        assertFalse(response.isHasPrevious());
    }

    @Test
    public void convertToEntityTest(){
        ProductRequest productRequest = new ProductRequest();
        productRequest.setProductName("laptop");
        productRequest.setProductDescription("new version of mac");
        productRequest.setProductPrice(12.3);
        productRequest.setItemSku("  MYU-16636  ");
        ProductEntity entity = this.productServiceImp.convertToEntity(productRequest);
        assertEquals("MYU-16636", entity.getItemSku());
        assertEquals("laptop", entity.getProductName());
        assertEquals("new version of mac", entity.getProductDescription());
        assertEquals(12.3, entity.getProductPrice());
        assertNotNull(entity.getId());
    }

    @Test
    public void convertToResponseTest(){
        ProductEntity productEntity = new ProductEntity();
        productEntity.setId("test-id-1");
        productEntity.setProductName("laptop");
        productEntity.setProductDescription("new version of mac");
        productEntity.setProductPrice(12.3);
        productEntity.setItemSku("MYU-16636");
        ProductResponse response = this.productServiceImp.convertToResponse(productEntity);
        assertEquals("MYU-16636", response.getItemSku());
        assertEquals("laptop", response.getProductName());
        assertEquals("new version of mac", response.getProductDescription());
        assertEquals(12.3, response.getProductPrice());
    }

    @Test
    public void updateProductTest() throws Exception {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setId("test-id-1");
        productEntity.setProductName("laptop");
        productEntity.setProductDescription("old description");
        productEntity.setProductPrice(12.3);
        productEntity.setItemSku("MYU-16636");
        UpdateProductRequest updateRequest = new UpdateProductRequest();
        updateRequest.setProductName("updated laptop");
        updateRequest.setProductDescription("updated description");
        updateRequest.setProductPrice(15.5);
        when(this.productRepository.findByItemSku("MYU-16636")).thenReturn(Optional.of(productEntity));
        when(this.productRepository.save(any(ProductEntity.class))).thenAnswer(invocation -> invocation.getArgument(0));
        when(this.objectMapper.writeValueAsString(any())).thenReturn("{}");
        doNothing().when(this.kafkaTemplate).send(anyString(), anyString());
        ProductResponse response = this.productServiceImp.updateProduct("MYU-16636", updateRequest);
        assertEquals("MYU-16636", response.getItemSku());
        assertEquals("updated laptop", response.getProductName());
        assertEquals("updated description", response.getProductDescription());
        assertEquals(15.5, response.getProductPrice());
    }

    @Test
    public void updateProductNotFoundTest(){
        UpdateProductRequest updateRequest = new UpdateProductRequest();
        updateRequest.setProductName("updated laptop");
        updateRequest.setProductDescription("updated description");
        updateRequest.setProductPrice(15.5);
        when(this.productRepository.findByItemSku("INVALID-SKU")).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> {
            this.productServiceImp.updateProduct("INVALID-SKU", updateRequest);
        });
    }

    @Test
    public void updateProductWithNullItemSkuTest(){
        UpdateProductRequest updateRequest = new UpdateProductRequest();
        updateRequest.setProductName("updated laptop");
        updateRequest.setProductDescription("updated description");
        updateRequest.setProductPrice(15.5);
        assertThrows(IllegalArgumentException.class, () -> {
            this.productServiceImp.updateProduct(null, updateRequest);
        });
    }

    @Test
    public void updateProductWithNullUpdateRequestTest(){
        assertThrows(IllegalArgumentException.class, () -> {
            this.productServiceImp.updateProduct("MYU-16636", null);
        });
    }

    @Test
    public void deleteProductByItemSkuTest() throws Exception {
        ProductEntity productEntity = new ProductEntity();
        productEntity.setId("test-id-1");
        productEntity.setProductName("laptop");
        productEntity.setProductDescription("new version of mac");
        productEntity.setProductPrice(12.3);
        productEntity.setItemSku("MYU-16636");
        when(this.productRepository.findByItemSku("MYU-16636")).thenReturn(Optional.of(productEntity));
        doNothing().when(this.productRepository).deleteByItemSku("MYU-16636");
        when(this.objectMapper.writeValueAsString(any())).thenReturn("{}");
        doNothing().when(this.kafkaTemplate).send(anyString(), anyString());
        this.productServiceImp.deleteProductByItemSku("MYU-16636");
        verify(this.productRepository, times(1)).deleteByItemSku("MYU-16636");
    }

    @Test
    public void deleteProductByItemSkuNotFoundTest(){
        when(this.productRepository.findByItemSku("INVALID-SKU")).thenReturn(Optional.empty());
        assertThrows(BusinessException.class, () -> {
            this.productServiceImp.deleteProductByItemSku("INVALID-SKU");
        });
    }

    @Test
    public void deleteProductByItemSkuWithNullItemSkuTest(){
        assertThrows(IllegalArgumentException.class, () -> {
            this.productServiceImp.deleteProductByItemSku(null);
        });
    }

    @Test
    public void deleteProductByItemSkuWithEmptyItemSkuTest(){
        assertThrows(IllegalArgumentException.class, () -> {
            this.productServiceImp.deleteProductByItemSku("   ");
        });
    }

}
