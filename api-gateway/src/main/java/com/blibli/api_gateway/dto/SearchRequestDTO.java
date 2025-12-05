package com.blibli.api_gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SearchRequestDTO {
    private String searchKeyword;
    private Integer pageNo;
    private Integer size;
    private String sort;
}
