package ProductService.ProductService.controller;

import ProductService.ProductService.common.ApiResponse;
import ProductService.ProductService.dto.ProductRequestDto;
import ProductService.ProductService.dto.ProductResponseDto;
import ProductService.ProductService.service.ProductService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;

@ExtendWith(MockitoExtension.class)
public class ProductControllerTest {

    @Mock
    private ProductService productService;

    @InjectMocks
    private ProductController productController;

    private ProductRequestDto productRequestDto;
    private ProductResponseDto productResponseDto;

    @BeforeEach
    void setUp() {
        productRequestDto = new ProductRequestDto();
        productRequestDto.setName("Laptop");
        productRequestDto.setPrice(1000.0);

        productResponseDto = new ProductResponseDto();
        productResponseDto.setId("p1");
        productResponseDto.setName("Laptop");
        productResponseDto.setPrice(1000.0);
    }

    @Test
    void testAddProduct() {
        Mockito.when(productService.addProduct(any(ProductRequestDto.class)))
                .thenReturn(productResponseDto);

        ResponseEntity<ApiResponse<?>> response = productController.addProduct(productRequestDto);

        assertEquals(200, response.getStatusCodeValue());
        ProductResponseDto returned = (ProductResponseDto) response.getBody().getData();
        assertEquals("Laptop", returned.getName());
    }

    @Test
    void testGetProductById() {
        Mockito.when(productService.getProductById("p1")).thenReturn(productResponseDto);

        ResponseEntity<ApiResponse<?>> response = productController.getProductById("p1");

        assertEquals(200, response.getStatusCodeValue());
        ProductResponseDto returned = (ProductResponseDto) response.getBody().getData();
        assertEquals("p1", returned.getId());
    }

    @Test
    void testGetProducts() {
        Page<ProductResponseDto> page = new PageImpl<>(List.of(productResponseDto));
        Mockito.when(productService.getProducts(0, 10)).thenReturn(page);

        ResponseEntity<ApiResponse<?>> response = productController.getProducts(0, 10);

        assertEquals(200, response.getStatusCodeValue());
        Page<ProductResponseDto> returned = (Page<ProductResponseDto>) response.getBody().getData();
        assertEquals(1, returned.getTotalElements());
    }

    @Test
    void testSearchProducts() {
        Page<ProductResponseDto> page = new PageImpl<>(List.of(productResponseDto));
        Mockito.when(productService.searchProducts(eq("Laptop"), anyInt(), anyInt())).thenReturn(page);

        ResponseEntity<ApiResponse<?>> response = productController.searchProducts("Laptop", 0, 10);

        assertEquals(200, response.getStatusCodeValue());
        Page<ProductResponseDto> returned = (Page<ProductResponseDto>) response.getBody().getData();
        assertEquals("Laptop", returned.getContent().get(0).getName());
    }

    @Test
    void testUpdateProduct() {
        Mockito.when(productService.updateProduct(eq("p1"), any(ProductRequestDto.class)))
                .thenReturn(productResponseDto);

        ResponseEntity<ApiResponse<?>> response = productController.updateProduct("p1", productRequestDto);

        assertEquals(200, response.getStatusCodeValue());
        ProductResponseDto returned = (ProductResponseDto) response.getBody().getData();
        assertEquals("Laptop", returned.getName());
    }

    @Test
    void testDeleteProduct() {
        Mockito.doNothing().when(productService).deleteProduct("p1");

        ResponseEntity<ApiResponse<?>> response = productController.deleteProduct("p1");

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Product deleted successfully", response.getBody().getData());
    }

    @Test
    void testGenerateBulkProducts() {
        Mockito.when(productService.generateBulkProducts(50000))
                .thenReturn("Bulk products generated");

        ResponseEntity<ApiResponse<?>> response = productController.generateBulkProducts(50000);

        assertEquals(200, response.getStatusCodeValue());
        assertEquals("Bulk products generated", response.getBody().getData());
    }

    @Test
    void testGetProductForCart() {
        Mockito.when(productService.getProductById("p1")).thenReturn(productResponseDto);

        ResponseEntity<ApiResponse<ProductResponseDto>> response = productController.getProductForCart("p1");

        assertEquals(200, response.getStatusCodeValue());
        ProductResponseDto returned = response.getBody().getData();
        assertEquals("Laptop", returned.getName());
        assertEquals(1000.0, returned.getPrice());
    }
}

