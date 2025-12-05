package com.blibli.gdn.productService.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class VariantDocument {
    @Field(type = FieldType.Keyword)
    private String sku;
    
    @Field(type = FieldType.Keyword)
    private String color;
    
    @Field(type = FieldType.Keyword)
    private String size;
    
    @Field(type = FieldType.Double)
    private Double price;
    
    @Field(type = FieldType.Integer)
    private Integer stock;
}

