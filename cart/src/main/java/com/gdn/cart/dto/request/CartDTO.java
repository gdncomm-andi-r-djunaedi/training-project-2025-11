package com.gdn.cart.dto.request;


import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CartDTO {
    private String memberId;
    private List<CartItemDTO> items;
    private Double totalAmount;
}
