package com.sc.cartservice.dto;

import lombok.Data;

@Data
public class AddToCartDto {
    private String memberId;
    private CartItemDto cartItemDto;
}
