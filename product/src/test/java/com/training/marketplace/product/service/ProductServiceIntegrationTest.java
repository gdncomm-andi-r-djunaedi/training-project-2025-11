package com.training.marketplace.product.service;

import com.training.marketplace.product.controller.modal.request.GetProductDetailRequest;
import com.training.marketplace.product.controller.modal.request.GetProductDetailResponse;
import com.training.marketplace.product.controller.modal.request.GetProductListRequest;
import com.training.marketplace.product.controller.modal.request.GetProductListResponse;
import com.training.marketplace.product.entity.ProductEntity;
import com.training.marketplace.product.repository.ProductRepository;
import io.grpc.StatusRuntimeException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = {
        "grpc.server.inProcessName=test",
        "grpc.server.port=-1",
        "grpc.client.inProcess.address=in-process:test"
})
@Testcontainers
class ProductServiceIntegrationTest {

    @Container
    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine");

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");
    }

    @Autowired
    private ProductServiceGrpc.ProductServiceBlockingStub productService;

    @Autowired
    private ProductRepository productRepository;

    private ProductEntity product1;
    private ProductEntity product2;

    @BeforeEach
    void setUp() {
        productRepository.deleteAll();
        product1 = ProductEntity.builder()
                .productId("P001")
                .productName("Laptop Pro")
                .productPrice(new BigDecimal("1200.00"))
                .productDetail("A powerful laptop for professionals.")
                .productNotes("Comes with a 3-year warranty.")
                .productImage("laptop-pro.jpg")
                .build();

        product2 = ProductEntity.builder()
                .productId("P002")
                .productName("Wireless Mouse")
                .productPrice(new BigDecimal("25.00"))
                .productDetail("An ergonomic wireless mouse.")
                .productNotes("Batteries included.")
                .productImage("wireless-mouse.jpg")
                .build();

        productRepository.saveAll(List.of(product1, product2));
    }

    @AfterEach
    void tearDown() {
        productRepository.deleteAll();
    }

    @Test
    void getProductDetail_whenProductExists_shouldReturnProduct() {
        GetProductDetailRequest request = GetProductDetailRequest.newBuilder().setProductId("P001").build();
        GetProductDetailResponse response = productService.getProductDetail(request);

        assertNotNull(response);
        assertEquals("P001", response.getProduct().getProductId());
        assertEquals("Laptop Pro", response.getProduct().getProductName());
    }

    @Test
    void getProductDetail_whenProductDoesNotExist_shouldThrowNotFound() {
        GetProductDetailRequest request = GetProductDetailRequest.newBuilder().setProductId("NON-EXISTENT").build();

        StatusRuntimeException exception = assertThrows(StatusRuntimeException.class, () -> {
            productService.getProductDetail(request);
        });

        assertEquals(io.grpc.Status.Code.NOT_FOUND, exception.getStatus().getCode());
        assertTrue(exception.getStatus().getDescription().contains("Product not found"));
    }

    @Test
    void getProductList_whenNoQuery_shouldReturnAllProducts() {
        GetProductListRequest request = GetProductListRequest.newBuilder()
                .setPage(0)
                .setItemPerPage(10)
                .build();

        GetProductListResponse response = productService.getProductList(request);

        assertNotNull(response);
        assertEquals(2, response.getProductListList().size());
    }

    @Test
    void getProductList_whenQueryMatches_shouldReturnFilteredProducts() {
        GetProductListRequest request = GetProductListRequest.newBuilder()
                .setPage(0)
                .setItemPerPage(10)
                .setQuery("Laptop")
                .build();

        GetProductListResponse response = productService.getProductList(request);

        assertNotNull(response);
        assertEquals(1, response.getProductListList().size());
        assertEquals("P001", response.getProductList(0).getProductId());
    }

    @Test
    void getProductList_withPagination_shouldReturnCorrectPage() {
        GetProductListRequest page1Request = GetProductListRequest.newBuilder()
                .setPage(0)
                .setItemPerPage(1)
                .build();
        GetProductListResponse page1Response = productService.getProductList(page1Request);

        assertNotNull(page1Response);
        assertEquals(1, page1Response.getProductListList().size());

        GetProductListRequest page2Request = GetProductListRequest.newBuilder()
                .setPage(1)
                .setItemPerPage(1)
                .build();
        GetProductListResponse page2Response = productService.getProductList(page2Request);

        assertNotNull(page2Response);
        assertEquals(1, page2Response.getProductListList().size());

        assertNotEquals(page1Response.getProductList(0).getProductId(), page2Response.getProductList(0).getProductId());
    }
}
