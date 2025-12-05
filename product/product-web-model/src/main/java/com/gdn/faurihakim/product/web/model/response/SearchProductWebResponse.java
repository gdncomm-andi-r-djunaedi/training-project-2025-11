package com.gdn.faurihakim.product.web.model.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@AllArgsConstructor
@NoArgsConstructor
public class SearchProductWebResponse {
    private List<GetProductWebResponse> products;
    private long totalElements;
    private int totalPages;
    private int currentPage;
}
