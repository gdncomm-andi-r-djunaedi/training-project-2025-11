package CartService.CartService.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class CartItemResponseDto {
    private String productId;
    private String productName;
    private double price;
    private int quantity;
}



