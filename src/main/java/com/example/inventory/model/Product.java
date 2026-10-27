package com.example.inventory.model;

import java.math.BigDecimal;

public record Product(String sku, String name, BigDecimal price, int quantity) {

    public Product {
        if (sku == null || sku.isBlank()) throw new IllegalArgumentException("SKU is required");
        if (name == null || name.isBlank()) throw new IllegalArgumentException("Name is required");
        if (price == null || price.signum() < 0) throw new IllegalArgumentException("Price must be zero or positive");
        if (quantity < 0) throw new IllegalArgumentException("Quantity cannot be negative");
    }

    public Product withQuantity(int newQuantity) {
        return new Product(sku, name, price, newQuantity);
    }

    public BigDecimal stockValue() {
        return price.multiply(BigDecimal.valueOf(quantity));
    }
}
