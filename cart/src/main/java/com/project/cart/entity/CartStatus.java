package com.project.cart.entity;

public enum CartStatus {
    ACTIVE,      // User actively shopping
    ABANDONED,   // No activity for extended period
    CONVERTED    // Converted to order
}
