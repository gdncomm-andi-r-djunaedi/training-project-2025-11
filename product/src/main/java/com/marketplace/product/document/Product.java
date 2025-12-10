package com.marketplace.product.document;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.index.Indexed;
import org.springframework.data.mongodb.core.index.TextIndexed;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;

/**
 * Product document for MongoDB.
 * Indexes are configured for optimal search performance with 50,000+ products.
 */
@Document(collection = "products")
@CompoundIndexes({
        @CompoundIndex(name = "name_category_idx", def = "{'name': 1, 'category': 1}")
})
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class Product {

    @Id
    private String id;

    @TextIndexed
    @Indexed
    private String name;

    private String description;

    private BigDecimal price;

    @Indexed
    private String category;

    private Integer stock; // Assuming unlimited for cart, but good to have
}
