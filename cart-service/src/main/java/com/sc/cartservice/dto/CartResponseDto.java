package com.sc.cartservice.dto;

import lombok.Data;

import java.util.List;

@Data
public class CartResponseDto {
    private String memberId;
    private List<CartItemDto> cartItemDtos;
    private double totalPrice;
}
