package com.gdn.training.product.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.Data;
import org.hibernate.annotations.UuidGenerator;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.math.BigDecimal;
import java.util.UUID;

@Entity
@Table(name = "products")
@Data
@EntityListeners(AuditingEntityListener.class)
public class Product {

    //what a product needs:
    //- product id
    //- name
    //- price
    //- description         : for product detail i guess?
    //assume stock unlimited so no stock?

    //what to do:
    //- search product
    //      - how do you paginate the search result?
    //- view product list
    //      - how do you paginate the list?
    //- view product detail

    @Id
    @Column(nullable = false, unique = true)
    private String product_id;

    @Column(nullable = false, unique = true)
    private String product_name;

    @Column(nullable = false, unique = true)
    private BigDecimal price;

    @Column(nullable = false, unique = true)
    private String description;

    public Product(){
    }

    public Product(String product_id, String product_name, BigDecimal price, String description){
        this.product_id = product_id;
        this.product_name = product_name;
        this.price = price;
        this.description = description;
    }
}
