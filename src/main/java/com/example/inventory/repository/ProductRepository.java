package com.example.inventory.repository;

import com.example.inventory.model.Product;
import java.util.List;
import java.util.Optional;

public interface ProductRepository {
    Product save(Product product);
    Optional<Product> findBySku(String sku);
    List<Product> findAll();
    void deleteBySku(String sku);
}
