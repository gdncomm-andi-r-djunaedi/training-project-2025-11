package com.blibli.product.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
public class CustomPageResponse<T> {
    public List<T> content;
    public int pageNumber;
    public int pageSize;
    public int totalElements;

    public CustomPageResponse(List<T> content, int number, int size, int totalElements) {
        this.content =content;
        this.pageNumber=number;
        this.pageSize =size;
        this.totalElements =totalElements;
    }
}
