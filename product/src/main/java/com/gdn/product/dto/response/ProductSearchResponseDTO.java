package com.gdn.product.dto.response;


import com.gdn.product.dto.request.ProductDTO;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class ProductSearchResponseDTO {
    private List<ProductDTO> content;
    private int page;
    private int size;
    private long totalElements;
    private int totalPages;
}
