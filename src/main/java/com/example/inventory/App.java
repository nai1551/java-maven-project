package com.example.inventory;

import com.example.inventory.model.Product;
import com.example.inventory.repository.InMemoryProductRepository;
import com.example.inventory.service.InventoryService;
import java.math.BigDecimal;

public class App {
    public static void main(String[] args) {
        InventoryService service = new InventoryService(new InMemoryProductRepository());

        service.addProduct(new Product("LAP-001", "Laptop", new BigDecimal("899.99"), 10));
        service.addProduct(new Product("MOU-001", "Mouse", new BigDecimal("19.50"), 50));
        service.addProduct(new Product("KEY-001", "Keyboard", new BigDecimal("45.00"), 3));

        System.out.println("Sold 2 laptops for: " + service.sell("LAP-001", 2));
        System.out.println("Low stock items: " + service.findLowStock(5));
        System.out.println("Total inventory value: " + service.totalInventoryValue());
    }
}
