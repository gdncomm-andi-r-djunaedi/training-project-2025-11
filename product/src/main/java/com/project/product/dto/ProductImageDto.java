package com.project.product.dto;
import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * DTO for product images
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Schema(description = "Product image information")
public class ProductImageDto {

    @NotBlank(message = "Main image URL is required")
    @Schema(description = "Main product image URL",
            example = "https://example.com/images/product-main.jpg")
    private String mainImage;

    @Schema(description = "Additional gallery images")
    @Builder.Default
    private List<String> galleryImages = new ArrayList<>();

    @Schema(description = "Thumbnail image URL",
            example = "https://example.com/images/product-thumb.jpg")
    private String thumbnail;
}
