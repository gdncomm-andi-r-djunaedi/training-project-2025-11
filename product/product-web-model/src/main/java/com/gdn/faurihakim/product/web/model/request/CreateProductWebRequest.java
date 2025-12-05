package com.gdn.faurihakim.product.web.model.request;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CreateProductWebRequest {

    private String productName;
    private String description;
    private Double price;
}
