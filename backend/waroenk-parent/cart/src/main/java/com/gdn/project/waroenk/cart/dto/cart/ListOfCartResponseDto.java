package com.gdn.project.waroenk.cart.dto.cart;

import java.util.List;

public record ListOfCartResponseDto(
    List<CartResponseDto> data,
    String nextToken,
    Integer total
) {}




