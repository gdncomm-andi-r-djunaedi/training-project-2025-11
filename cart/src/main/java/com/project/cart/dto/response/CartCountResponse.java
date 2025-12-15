package com.project.cart.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Cart item count response")
public class CartCountResponse {

    @Schema(description = "Total number of items in cart", example = "5")
    private Integer count;
}
