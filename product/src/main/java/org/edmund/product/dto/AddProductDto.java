package org.edmund.product.dto;

import lombok.Data;

@Data
public class AddProductDto {
    private String name;
    private String merchant;
    private String description;
    private Long price;
    private Integer stock;
}
