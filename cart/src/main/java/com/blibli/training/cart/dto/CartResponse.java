package com.blibli.training.cart.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class CartResponse implements Serializable {
    private static final long serialVersionUID = 1L;
    private Long id;
    private String username;
    private List<CartItemResponse> items;
}
