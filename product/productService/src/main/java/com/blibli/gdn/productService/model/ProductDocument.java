package com.blibli.gdn.productService.model;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Document(indexName = "products_index")
public class ProductDocument {
    @Id
    private String id;
    
    @Field(type = FieldType.Keyword)
    private String productId;
    
    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String name;
    
    @Field(type = FieldType.Text, analyzer = "standard", searchAnalyzer = "standard")
    private String description;
    
    @Field(type = FieldType.Keyword)
    private String category;
    
    @Field(type = FieldType.Keyword)
    private String brand;
    
    @Field(type = FieldType.Keyword)
    private List<String> tags;
    
    @Field(type = FieldType.Nested)
    private List<VariantDocument> variants;
}

