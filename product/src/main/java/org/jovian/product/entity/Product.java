package org.jovian.product.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Data
@Builder
@Entity
public class Product {

    @Id
    private String id;

    private String productName;
    private String productCategory;
    private Double productPrice;
    private String productCode;
    private String variationCode;

    private String producerCode;
    private String producerName;
}
