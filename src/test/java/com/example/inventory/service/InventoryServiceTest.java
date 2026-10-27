package com.example.inventory.service;

import static org.junit.jupiter.api.Assertions.*;

import com.example.inventory.exception.InsufficientStockException;
import com.example.inventory.exception.ProductNotFoundException;
import com.example.inventory.model.Product;
import com.example.inventory.repository.InMemoryProductRepository;
import java.math.BigDecimal;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

class InventoryServiceTest {

    private InventoryService service;

    @BeforeEach
    void setUp() {
        service = new InventoryService(new InMemoryProductRepository());
        service.addProduct(new Product("LAP-001", "Laptop", new BigDecimal("899.99"), 10));
        service.addProduct(new Product("MOU-001", "Mouse", new BigDecimal("19.50"), 50));
        service.addProduct(new Product("KEY-001", "Keyboard", new BigDecimal("45.00"), 3));
    }

    @Test
    void addDuplicateProductThrows() {
        Product duplicate = new Product("LAP-001", "Other", BigDecimal.ONE, 1);
        assertThrows(IllegalStateException.class, () -> service.addProduct(duplicate));
    }

    @Test
    void restockIncreasesQuantity() {
        Product updated = service.restock("KEY-001", 7);
        assertEquals(10, updated.quantity());
    }

    @Test
    void restockWithNonPositiveAmountThrows() {
        assertThrows(IllegalArgumentException.class, () -> service.restock("KEY-001", 0));
    }

    @Test
    void sellReducesStockAndReturnsTotal() {
        BigDecimal total = service.sell("LAP-001", 2);
        assertEquals(0, new BigDecimal("1799.98").compareTo(total));
        assertEquals(8, service.getProduct("LAP-001").quantity());
    }

    @Test
    void sellMoreThanAvailableThrows() {
        assertThrows(InsufficientStockException.class, () -> service.sell("KEY-001", 4));
    }

    @Test
    void unknownSkuThrows() {
        assertThrows(ProductNotFoundException.class, () -> service.getProduct("NOPE"));
    }

    @Test
    void findLowStockReturnsOnlyItemsAtOrBelowThreshold() {
        List<Product> low = service.findLowStock(5);
        assertEquals(1, low.size());
        assertEquals("KEY-001", low.get(0).sku());
    }

    @Test
    void totalInventoryValueIsSumOfAllStock() {
        BigDecimal total = service.totalInventoryValue();
        assertEquals(0, new BigDecimal("10109.90").compareTo(total));
    }

    @ParameterizedTest
    @CsvSource({
            ",Laptop,10.00,1",
            "SKU1,,10.00,1",
            "SKU1,Laptop,-1.00,1",
            "SKU1,Laptop,10.00,-5"
    })
    void invalidProductsAreRejected(String sku, String name, String price, int quantity) {
        assertThrows(IllegalArgumentException.class,
                () -> new Product(sku, name, new BigDecimal(price), quantity));
    }
}
