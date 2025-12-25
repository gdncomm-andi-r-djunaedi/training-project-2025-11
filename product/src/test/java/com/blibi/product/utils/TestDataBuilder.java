package com.blibi.product.utils;

import com.blibi.product.dto.ProductDTO;
import com.blibi.product.entity.Product;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.util.Arrays;
import java.util.List;

/**
 * Test data builder utility class for creating test objects.
 * 
 * This class provides factory methods to create consistent test data
 * for Product entities and DTOs, reducing code duplication in tests.
 */
public class TestDataBuilder {

    /**
     * Creates a sample Product entity for testing.
     * @return Product entity with predefined test data
     */
    public static Product createTestProduct() {
        Product product = new Product();
        product.setId("507f1f77bcf86cd799439011");
        product.setProductId("MTA-000001");
        product.setProductName("Test Laptop");
        product.setDescription("High-performance test laptop");
        product.setPrice(999.99);
        product.setCategory("Electronics");
        product.setImages(Arrays.asList("image1.jpg", "image2.jpg"));
        return product;
    }

    // Creates a sample Product entity with custom product ID.(eg. MTA-xxxxxx)

    public static Product createTestProduct(String productId) {
        Product product = createTestProduct();
        product.setProductId(productId);
        return product;
    }

    // Creates a sample Product entity with custom name and category.

    public static Product createTestProduct(String productName, String category) {
        Product product = createTestProduct();
        product.setProductName(productName);
        product.setCategory(category);
        return product;
    }

    // Creates a sample ProductDTO for testing.

    public static ProductDTO createTestProductDTO() {
        ProductDTO dto = new ProductDTO();
        dto.setProductId("MTA-000001");
        dto.setProductName("Test Laptop");
        dto.setDescription("High-performance test laptop");
        dto.setPrice(999.99);
        dto.setCategory("Electronics");
        dto.setImages(Arrays.asList("image1.jpg", "image2.jpg"));
        return dto;
    }

    // Creates a sample ProductDTO with custom product ID.

    public static ProductDTO createTestProductDTO(String productId) {
        ProductDTO dto = createTestProductDTO();
        dto.setProductId(productId);
        return dto;
    }

   // Creates a list of test Product entities.

    public static List<Product> createTestProductList() {
        return Arrays.asList(
                createTestProduct("MTA-000001"),
                createTestProduct("MTA-000002"),
                createTestProduct("MTA-000003"));
    }

   // Creates a list of test Product entities with custom names.

    public static List<Product> createTestProductListWithNames() {
        return Arrays.asList(
                createTestProduct("Gaming Laptop", "Electronics"),
                createTestProduct("Office Laptop", "Electronics"),
                createTestProduct("Student Laptop", "Electronics"));
    }

   // Creates a Page of test Product entities.

    public static Page<Product> createTestProductPage(Pageable pageable) {
        List<Product> products = createTestProductList();
        return new PageImpl<>(products, pageable, products.size());
    }

    // Creates an empty Page of Product entities.

    public static Page<Product> createEmptyProductPage(Pageable pageable) {
        return new PageImpl<>(Arrays.asList(), pageable, 0);
    }

   // Creates a default Pageable for testing.

    public static Pageable createDefaultPageable() {
        return PageRequest.of(0, 5);
    }
}
