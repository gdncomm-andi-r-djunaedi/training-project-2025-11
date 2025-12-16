package com.example.search.entity;

import lombok.Data;
import org.apache.solr.client.solrj.beans.Field;



@Data
public class ProductDocument {

    @Field("id")
    private String id;

    @Field("productId")
    private long productId;

    @Field("title")
    private String title;

    @Field("description")
    private String description;

    @Field("price")
    private Double price;

    @Field("imageUrl")
    String imageUrl;

    @Field("category")
    private String category;

    @Field("markForDelete")
    private boolean markForDelete;
}
