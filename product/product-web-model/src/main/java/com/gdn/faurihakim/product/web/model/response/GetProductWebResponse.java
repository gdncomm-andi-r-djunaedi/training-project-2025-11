package com.gdn.faurihakim.product.web.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class GetProductWebResponse {
    private String productId;
    private String productName;
    private String description;
    private Double price;
    private Long version;
    private Long createdDate;
    private String createdBy;
    private Long lastModifiedDate;
    private String lastModifiedBy;
}
