package com.example.inventory.exception;

public class InsufficientStockException extends RuntimeException {
    public InsufficientStockException(String sku, int available, int requested) {
        super("Insufficient stock for " + sku + ": available=" + available + ", requested=" + requested);
    }
}
