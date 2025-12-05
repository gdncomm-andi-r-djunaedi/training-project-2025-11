package com.project.product.dto.response;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Paginated response wrapper")
public class PageResponse<T> {
    @Schema(description = "List of items in current page")
    private List<T> content;

    @Schema(description = "Current page number (0-indexed)", example = "0")
    private int pageNumber;

    @Schema(description = "Number of items per page", example = "20")
    private int pageSize;

    @Schema(description = "Total number of elements", example = "150")
    private long totalElements;

    @Schema(description = "Total number of pages", example = "8")
    private int totalPages;

    @Schema(description = "Is this the first page", example = "true")
    private boolean first;

    @Schema(description = "Is this the last page", example = "false")
    private boolean last;

    @Schema(description = "Does it have a next page", example = "true")
    private boolean hasNext;

    @Schema(description = "Does it have a previous page", example = "false")
    private boolean hasPrevious;
}
