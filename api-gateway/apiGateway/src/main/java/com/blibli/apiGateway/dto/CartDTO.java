package com.blibli.apiGateway.dto;


import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@NoArgsConstructor
@AllArgsConstructor
@ToString
public class CartDTO {

    private String id;
    private String memberId;
    private List<CartItemDTO> items;
    private int totalItems;

    public void setTotalItems(int totalItems) {
        this.totalItems = totalItems;
    }

    public int getTotalItems() {
        return items != null ? items.size():0;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getMemberId() {
        return memberId;
    }

    public void setMemberId(String memberId) {
        this.memberId = memberId;
    }

    public List<CartItemDTO> getItems() {
        return items;
    }

    public void setItems(List<CartItemDTO> items) {
        this.items = items;
    }
}
