package com.sc.productservice;

import com.sc.productservice.config.MongoTestContainerConfig;
import com.sc.productservice.dto.ProductDto;
import com.sc.productservice.model.Product;
import com.sc.productservice.repository.ProductRepository;
import com.sc.productservice.service.ProductService;
import com.sc.utilsservice.ApiResponse;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class ProductServiceApplicationTests extends MongoTestContainerConfig {

    @Autowired
    private ProductService productService;

    @Autowired
    private ProductRepository productRepository;


    @BeforeEach
    void cleanDb() {
        productRepository.deleteAll();
    }

    @Test
    void testCreateProduct() {
        ProductDto dto = new ProductDto();
        dto.setProductCode("P1001");
        dto.setProductName("Laptop");
        dto.setProductPrice(50000);

        ApiResponse<ProductDto> response = productService.createProduct(dto);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getProductCode()).isEqualTo("P1001");

        Product saved = productRepository.findByProductCode("P1001").orElse(null);
        assertThat(saved).isNotNull();
    }

    @Test
    void testGetProduct() {
        productRepository.save(new Product(null, "P2002", "Monitor", 15000));

        ApiResponse<ProductDto> response = productService.getProduct("P2002");

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getProductName()).isEqualTo("Monitor");
    }

    @Test
    void testGetProduct_NotFound() {
        ApiResponse<ProductDto> response = productService.getProduct("INVALID");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorCode()).isEqualTo("PRODUCT_NOT_FOUND");
    }

    @Test
    void testGetAllProducts() {
        productRepository.save(new Product(null, "C1", "Charger", 500));
        productRepository.save(new Product(null, "C2", "Phone", 15000));

        Pageable pageable = PageRequest.of(0, 10);
        ApiResponse<Page<ProductDto>> response = productService.getAllProducts(pageable);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().toList().size()).isEqualTo(2);
    }

    @Test
    void testSearchProducts() {
        productRepository.save(new Product(null, "S1", "Mouse", 700));
        productRepository.save(new Product(null, "S2", "Bluetooth Mouse", 1200));

        Pageable pageable = PageRequest.of(0, 10);
        ApiResponse<Page<ProductDto>> response = productService.searchProducts("mouse", pageable);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().toList().size()).isEqualTo(2);
    }

    @Test
    void testUpdateProduct() {
        productRepository.save(new Product(null, "UP1", "Keyboard", 1000));

        ProductDto dto = new ProductDto();
        dto.setProductCode("UP1");
        dto.setProductName("Mechanical Keyboard");
        dto.setProductPrice(2000);

        ApiResponse<ProductDto> response = productService.updateProduct(dto);

        assertThat(response.isSuccess()).isTrue();
        assertThat(response.getData().getProductName()).isEqualTo("Mechanical Keyboard");
    }

    @Test
    void testUpdateProduct_NotFound() {
        ProductDto dto = new ProductDto();
        dto.setProductCode("XXX");
        dto.setProductName("Nothing");

        ApiResponse<ProductDto> response = productService.updateProduct(dto);

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorCode()).isEqualTo("PRODUCT_NOT_FOUND");
    }

    @Test
    void testDeleteProduct() {
        productRepository.save(new Product(null, "D1", "Desk", 3000));

        ApiResponse<String> response = productService.deleteProduct("D1");

        assertThat(response.isSuccess()).isTrue();
        assertThat(productRepository.findByProductCode("D1")).isEmpty();
    }

    @Test
    void testDeleteProduct_NotFound() {
        ApiResponse<String> response = productService.deleteProduct("NOT_EXIST");

        assertThat(response.isSuccess()).isFalse();
        assertThat(response.getErrorCode()).isEqualTo("PRODUCT_NOT_FOUND");
    }
}
