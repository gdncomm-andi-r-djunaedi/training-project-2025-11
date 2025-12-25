package com.kailash.cart.dto;

import lombok.*;
import org.springframework.data.annotation.Id;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Getter
@Setter
public class CartResponse {

    @Id
    private String id;
    private String memberId;
    private int totalItems;
    private Double totalPrice;
}
