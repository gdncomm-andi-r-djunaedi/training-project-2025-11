package com.blibli.cart.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum CategoryType {
    FASHION("fashion"),
    ELECTRONIC("electronic");

    private final String value;

    CategoryType(String value) {
        this.value = value;
    }

    @JsonValue
    public String getValue() {
        return value;
    }

    @JsonCreator
    public static CategoryType fromValue(String value) {
        for (CategoryType category : CategoryType.values()) {
            if (category.value.equalsIgnoreCase(value)) {
                return category;
            }
        }
        throw new IllegalArgumentException("Invalid category: " + value + ". Allowed values are: fashion, electronic");
    }

    @Override
    public String toString() {
        return this.value;
    }
}
