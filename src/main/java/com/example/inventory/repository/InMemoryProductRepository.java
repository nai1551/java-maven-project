package com.example.inventory.repository;

import com.example.inventory.model.Product;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;

public class InMemoryProductRepository implements ProductRepository {

    private final Map<String, Product> store = new ConcurrentHashMap<>();

    @Override
    public Product save(Product product) {
        store.put(product.sku(), product);
        return product;
    }

    @Override
    public Optional<Product> findBySku(String sku) {
        return Optional.ofNullable(store.get(sku));
    }

    @Override
    public List<Product> findAll() {
        return List.copyOf(store.values());
    }

    @Override
    public void deleteBySku(String sku) {
        store.remove(sku);
    }
}
