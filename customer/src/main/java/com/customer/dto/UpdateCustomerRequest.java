package com.customer.dto;

import lombok.Data;

@Data
public class UpdateCustomerRequest {
    private String name;
    private String phone;
    private String address;
}
