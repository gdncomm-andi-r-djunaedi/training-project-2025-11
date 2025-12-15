package com.blibli.cart.response;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ErrorResponse extends RuntimeException {
    public ErrorResponse(String msg){
        super(msg);
    }
}
