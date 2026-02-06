package service;

import org.informatics.exception.ExpiredProductException;
import org.informatics.exception.InsufficientStockException;
import org.informatics.exception.InvalidProductException;
import org.informatics.model.*;
import org.informatics.service.Store;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.io.TempDir;

import java.nio.file.Path;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Integration tests for Store
 */
class StoreTest {
    @TempDir
    Path tempDir;

    private Store store;
    private Cashier cashier1;
    private Cashier cashier2;
    private FoodProduct milk;
    private FoodProduct bread;
    private FoodProduct yogurt;
    private FoodProduct cheese;
    private FoodProduct expiredHam;
    private NonFoodProduct soap;
    private NonFoodProduct shampoo;

    @BeforeEach
    void setUp() throws InvalidProductException {
        // Create store - same config as Main.java
        store = new Store(
                "Shop NBU",
                5,      // threshold days
                20.0,   // discount percentage
                tempDir.toString()
        );

        // Add cashiers - same as Main.java
        cashier1 = new Cashier("C001", "Ivan Ivanov", 1500.0);
        cashier2 = new Cashier("C002", "Mariya Popova", 1600.0);
        store.addCashier(cashier1);
        store.addCashier(cashier2);

        // Add products - same as Main.java
        milk = new FoodProduct("P001", "Milk", 2.50,
                LocalDate.now().plusDays(10), 50, 30.0);

        bread = new FoodProduct("P002", "Bread", 1.20,
                LocalDate.now().plusDays(3), 100, 25.0);

        yogurt = new FoodProduct("P003", "Yogurt", 1.80,
                LocalDate.now().plusDays(15), 30, 30.0);

        cheese = new FoodProduct("P006", "Cheese", 5.50,
                LocalDate.now().plusDays(2), 20, 35.0);

        expiredHam = new FoodProduct("P008", "Expired Ham", 4.20,
                LocalDate.now().minusDays(5), 12, 28.0);

        soap = new NonFoodProduct("P004", "Soap", 3.00,
                LocalDate.now().plusMonths(12), 40, 50.0);

        shampoo = new NonFoodProduct("P005", "Shampoo", 8.00,
                LocalDate.now().plusMonths(18), 25, 45.0);

        store.addProduct(milk);
        store.addProduct(bread);
        store.addProduct(yogurt);
        store.addProduct(cheese);
        store.addProduct(expiredHam);
        store.addProduct(soap);
        store.addProduct(shampoo);
    }

    @Test
    void testAddCashier() throws InvalidProductException {
        Cashier newCashier = new Cashier("C003", "New Cashier", 1550.0);
        store.addCashier(newCashier);

        assertEquals(newCashier, store.getCashier("C003"));
    }

    @Test
    void testAddDuplicateCashier() {
        Cashier duplicate = new Cashier("C001", "Duplicate", 1700.0);

        assertThrows(InvalidProductException.class,
                () -> store.addCashier(duplicate));
    }

    @Test
    void testRestockProduct() throws InvalidProductException {
        int initialStock = milk.getQuantityInStock();

        store.restockProduct("P001", 20);

        assertEquals(initialStock + 20, milk.getQuantityInStock());
    }

    @Test
    void testSuccessfulSale() throws Exception {
        Map<String, Integer> items = new HashMap<>();
        items.put("P001", 2);  // Milk
        items.put("P004", 1);  // Soap

        Receipt receipt = store.makeSale("C001", items, 20.0);

        assertNotNull(receipt);
        assertEquals(1, receipt.getReceiptNumber());
        assertEquals(2, receipt.getItems().size());

        // Expected: (2 * 3.25) + (1 * 4.50) = 6.50 + 4.50 = 11.00
        assertEquals(11.00, receipt.getTotalAmount(), 0.01);

        // Check that quantity has decreased
        assertEquals(48, milk.getQuantityInStock());
        assertEquals(39, soap.getQuantityInStock());
    }

    @Test
    void testSaleWithExpiredProduct() {
        Map<String, Integer> items = new HashMap<>();
        items.put("P008", 1);  // Expired Ham

        assertThrows(ExpiredProductException.class,
                () -> store.makeSale("C001", items, 10.0));
    }

    @Test
    void testSaleWithInsufficientStock() {
        Map<String, Integer> items = new HashMap<>();
        items.put("P001", 100);  // Want 100 milk, but have only 50

        InsufficientStockException exception = assertThrows(
                InsufficientStockException.class,
                () -> store.makeSale("C001", items, 500.0));

        assertEquals("P001", exception.getProductId());
        assertEquals(100, exception.getRequestedQuantity());
        assertEquals(50, exception.getAvailableQuantity());
        assertEquals(50, exception.getShortageQuantity());
    }

    @Test
    void testSaleWithInsufficientPayment() {
        Map<String, Integer> items = new HashMap<>();
        items.put("P005", 2);  // 2 Shampoo = 23.20 EUR

        assertThrows(InvalidProductException.class,
                () -> store.makeSale("C001", items, 10.0));  // Only 10 EUR
    }

    @Test
    void testReceiptNumberIncrement() throws Exception {
        Map<String, Integer> items1 = new HashMap<>();
        items1.put("P001", 1);  // Milk

        Map<String, Integer> items2 = new HashMap<>();
        items2.put("P002", 1);  // Bread

        Receipt receipt1 = store.makeSale("C001", items1, 10.0);
        Receipt receipt2 = store.makeSale("C002", items2, 10.0);

        assertEquals(1, receipt1.getReceiptNumber());
        assertEquals(2, receipt2.getReceiptNumber());
    }

    @Test
    void testGetAvailableProducts() {
        var available = store.getAvailableProducts();

        // Should have 6 products (all except expired ham)
        assertEquals(6, available.size());

        // Should not contain expired ham
        assertFalse(available.stream().anyMatch(p -> p.getId().equals("P008")));
    }

    @Test
    void testFinancialCalculations() throws Exception {
        Map<String, Integer> items = new HashMap<>();
        items.put("P001", 2);  // Milk
        items.put("P002", 3);  // Bread

        store.makeSale("C001", items, 20.0);

        // Salary expenses: 1500 + 1600 = 3100
        assertEquals(3100.0, store.calculateSalaryExpenses(), 0.01);

        // Revenue from sale: (2 * 3.25) + (3 * 1.20) = 10.10
        assertEquals(10.10, store.calculateTotalRevenue(), 0.01);

        // Receipts count
        assertEquals(1, store.getTotalReceiptsCount());
    }
}