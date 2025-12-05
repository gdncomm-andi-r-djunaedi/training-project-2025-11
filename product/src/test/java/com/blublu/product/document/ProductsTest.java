package com.blublu.product.document;

import org.junit.jupiter.api.Test;

import java.math.BigDecimal;
import java.util.Collections;

import static org.junit.jupiter.api.Assertions.*;

class ProductsTest {

    @Test
    void testProductsLombok() {
        Products product1 = Products.builder()
                .id("1")
                .skuCode("SKU1")
                .name("Product 1")
                .description("Desc")
                .price(BigDecimal.TEN)
                .originalPrice(BigDecimal.valueOf(20))
                .categories(Collections.singletonList("Cat1"))
                .build();

        Products product2 = new Products();
        product2.setId("1");
        product2.setSkuCode("SKU1");
        product2.setName("Product 1");
        product2.setDescription("Desc");
        product2.setPrice(BigDecimal.TEN);
        product2.setOriginalPrice(BigDecimal.valueOf(20));
        product2.setCategories(Collections.singletonList("Cat1"));

        assertEquals(product1, product2);
        assertEquals(product1.hashCode(), product2.hashCode());
        assertEquals(product1.toString(), product2.toString());

        assertEquals("1", product1.getId());
        assertEquals("SKU1", product1.getSkuCode());
        assertEquals("Product 1", product1.getName());
        assertEquals("Desc", product1.getDescription());
        assertEquals(BigDecimal.TEN, product1.getPrice());
        assertEquals(BigDecimal.valueOf(20), product1.getOriginalPrice());
        assertEquals(Collections.singletonList("Cat1"), product1.getCategories());
    }
}
