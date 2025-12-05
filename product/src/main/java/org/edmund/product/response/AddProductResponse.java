package org.edmund.product.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AddProductResponse {
    private String sku;
    private String name;
    private String merchant;
    private String description;
    private Long price;
    private Integer stock;
}
