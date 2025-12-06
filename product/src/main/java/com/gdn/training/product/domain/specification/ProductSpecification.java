package com.gdn.training.product.domain.specification;

public final class ProductSpecification {
    // Attributes
    private final String keyword;
    private final String category;
    private final double minPrice;
    private final double maxPrice;

    // Constructor
    public ProductSpecification(String keyword, String category, double minPrice, double maxPrice) {
        this.keyword = keyword;
        this.category = category;
        this.minPrice = minPrice;
        this.maxPrice = maxPrice;
    }

    // Getters
    public String keyword() {
        return keyword;
    }

    public String category() {
        return category;
    }

    public double minPrice() {
        return minPrice;
    }

    public double maxPrice() {
        return maxPrice;
    }

}
