package com.microservice.search.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.elasticsearch.annotations.DateFormat;
import org.springframework.data.elasticsearch.annotations.Document;
import org.springframework.data.elasticsearch.annotations.Field;
import org.springframework.data.elasticsearch.annotations.FieldType;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Document(indexName = "products")
public class ProductDocument {

    @Id
    private String skuId;

    @Field(type = FieldType.Integer)
    private Integer storeId;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String name;

    @Field(type = FieldType.Text, analyzer = "standard")
    private String description;

    @Field(type = FieldType.Keyword)
    private String category;

    @Field(type = FieldType.Keyword)
    private String brand;

    @Field(type = FieldType.Long)
    private Long price;

    @Field(type = FieldType.Long)
    private Long itemCode;

    @Field(type = FieldType.Boolean)
    private Boolean isActive;

    @Field(type = FieldType.Long)
    private Long length;

    @Field(type = FieldType.Long)
    private Long height;

    @Field(type = FieldType.Long)
    private Long width;

    @Field(type = FieldType.Long)
    private Long weight;

    @Field(type = FieldType.Integer)
    private Integer dangerousLevel;

    @Field(type = FieldType.Date, format = DateFormat.date)
    private LocalDate createdAt;

    @Field(type = FieldType.Date, format = DateFormat.date)
    private LocalDate updatedAt;
}