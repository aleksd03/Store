package model;

import org.informatics.model.FoodProduct;
import org.informatics.model.NonFoodProduct;
import org.junit.jupiter.api.Test;
import java.time.LocalDate;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Unit tests for products
 */
class ProductTest {
    @Test
    void testFoodProductPriceCalculation() {
        // Milk - same as Main.java
        FoodProduct milk = new FoodProduct(
                "P001", "Milk", 2.50,
                LocalDate.now().plusDays(10), 50, 30.0
        );

        // Price without discount: 2.50 * 1.30 = 3.25
        double price = milk.calculateSalePrice(10, 5, 20.0);
        assertEquals(3.25, price, 0.01);
    }

    @Test
    void testFoodProductWithDiscount() {
        // Bread - same as Main.java
        FoodProduct bread = new FoodProduct(
                "P002", "Bread", 1.20,
                LocalDate.now().plusDays(3), 100, 25.0
        );

        // Price with markup: 1.20 * 1.25 = 1.50
        // Price with discount: 1.50 * 0.80 = 1.20
        double price = bread.calculateSalePrice(3, 5, 20.0);
        assertEquals(1.20, price, 0.01);
    }

    @Test
    void testExpiredProduct() {
        // Expired Ham - same as Main.java
        FoodProduct expiredHam = new FoodProduct(
                "P008", "Expired Ham", 4.20,
                LocalDate.now().minusDays(5), 12, 28.0
        );

        assertTrue(expiredHam.isExpired());
        assertTrue(expiredHam.getDaysUntilExpiration() < 0);
    }

    @Test
    void testNonFoodProduct() {
        // Soap - same as Main.java
        NonFoodProduct soap = new NonFoodProduct(
                "P004", "Soap", 3.00,
                LocalDate.now().plusMonths(12), 40, 50.0
        );

        // Price: 3.00 * 1.50 = 4.50
        double price = soap.calculateSalePrice(365, 5, 20.0);
        assertEquals(4.50, price, 0.01);
    }

    @Test
    void testStockManagement() {
        // Milk - same as Main.java
        FoodProduct milk = new FoodProduct(
                "P001", "Milk", 2.50,
                LocalDate.now().plusDays(10), 50, 30.0
        );

        assertEquals(50, milk.getQuantityInStock());

        milk.reduceStock(10);
        assertEquals(40, milk.getQuantityInStock());

        milk.addStock(20);
        assertEquals(60, milk.getQuantityInStock());
    }

    @Test
    void testProductEquality() {
        FoodProduct milk1 = new FoodProduct(
                "P001", "Milk", 2.50,
                LocalDate.now().plusDays(10), 50, 30.0
        );

        FoodProduct milk2 = new FoodProduct(
                "P001", "Different Milk", 3.00,
                LocalDate.now().plusDays(5), 20, 25.0
        );

        // Products are equal if they have the same ID
        assertEquals(milk1, milk2);
    }
}