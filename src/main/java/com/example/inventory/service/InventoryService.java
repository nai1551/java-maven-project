package com.example.inventory.service;

import com.example.inventory.exception.InsufficientStockException;
import com.example.inventory.exception.ProductNotFoundException;
import com.example.inventory.model.Product;
import com.example.inventory.repository.ProductRepository;
import java.math.BigDecimal;
import java.util.Comparator;
import java.util.List;
import java.util.Objects;

public class InventoryService {

    private final ProductRepository repository;

    public InventoryService(ProductRepository repository) {
        this.repository = Objects.requireNonNull(repository);
    }

    public Product addProduct(Product product) {
        if (repository.findBySku(product.sku()).isPresent()) {
            throw new IllegalStateException("Product already exists: " + product.sku());
        }
        return repository.save(product);
    }

    public Product getProduct(String sku) {
        return repository.findBySku(sku).orElseThrow(() -> new ProductNotFoundException(sku));
    }

    public Product restock(String sku, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        Product product = getProduct(sku);
        return repository.save(product.withQuantity(product.quantity() + amount));
    }

    public BigDecimal sell(String sku, int amount) {
        if (amount <= 0) throw new IllegalArgumentException("Amount must be positive");
        Product product = getProduct(sku);
        if (product.quantity() < amount) {
            throw new InsufficientStockException(sku, product.quantity(), amount);
        }
        repository.save(product.withQuantity(product.quantity() - amount));
        return product.price().multiply(BigDecimal.valueOf(amount));
    }

    public List<Product> findLowStock(int threshold) {
        return repository.findAll().stream()
                .filter(p -> p.quantity() <= threshold)
                .sorted(Comparator.comparingInt(Product::quantity).thenComparing(Product::sku))
                .toList();
    }

    public BigDecimal totalInventoryValue() {
        return repository.findAll().stream()
                .map(Product::stockValue)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
