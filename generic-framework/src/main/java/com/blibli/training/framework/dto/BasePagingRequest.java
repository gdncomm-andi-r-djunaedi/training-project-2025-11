package com.blibli.training.framework.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import com.blibli.training.framework.constant.SortDirection;

import java.io.Serializable;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class BasePagingRequest implements Serializable {
    private static final long serialVersionUID = 1L;

    @Builder.Default
    private int page = 0;

    @Builder.Default
    private int size = 10;

    private String sortBy;

    @Builder.Default
    private SortDirection sortDirection = SortDirection.ASC;

    private String search;
}
