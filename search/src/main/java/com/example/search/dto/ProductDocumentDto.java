package com.example.search.dto;

import lombok.Builder;
import lombok.Data;
import org.apache.solr.client.solrj.beans.Field;

import java.math.BigDecimal;

@Data
@Builder
public class ProductDocumentDto {

    private long productId;
    private String title;
    private String description;
    private Double price;
    String imageUrl;
    private String category;
    private boolean markForDelete;
}
