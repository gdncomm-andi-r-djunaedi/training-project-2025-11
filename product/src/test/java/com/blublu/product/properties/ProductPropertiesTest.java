package com.blublu.product.properties;

import org.junit.jupiter.api.Test;

import java.util.HashMap;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ProductPropertiesTest {

    @Test
    void testProductPropertiesLombok() {
        HashMap<String, String> flags = new HashMap<>();
        flags.put("key", "value");

        ProductProperties properties1 = new ProductProperties();
        properties1.setFlag(flags);

        ProductProperties properties2 = new ProductProperties();
        properties2.setFlag(flags);

        assertEquals(properties1, properties2);
        assertEquals(properties1.hashCode(), properties2.hashCode());
        assertEquals(properties1.toString(), properties2.toString());
        assertEquals(flags, properties1.getFlag());
    }
}
