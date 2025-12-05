package com.gdn.product.dto.request;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class SearchProductDTO {
    private String keyword;
    private Integer page;
    private Integer size;
    private Integer sort;

}
