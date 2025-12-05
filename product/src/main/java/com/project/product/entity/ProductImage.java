package com.project.product.entity;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ProductImage {
    private String mainImage;

    /**
     * Additional product images for gallery
     */
    @Builder.Default
    private List<String> galleryImages = new ArrayList<>();
    private String thumbnail;
}
