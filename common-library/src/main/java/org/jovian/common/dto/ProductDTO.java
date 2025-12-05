package org.jovian.common.dto;

import lombok.Builder;
import lombok.Data;

@Builder
@Data
public class ProductDTO {
    private String id;
    private String productName;
    private String productCategory;
    private Double productPrice;
    private String productCode;
    private String variationCode;
    private String producerCode;
    private String producerName;
}