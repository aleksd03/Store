package service;

import org.informatics.exception.ExpiredProductException;
import org.informatics.exception.InsufficientStockException;
import org.informatics.exception.InvalidProductException;
import org.informatics.model.FoodProduct;
import org.informatics.model.NonFoodProduct;
import org.informatics.model.Product;
import org.informatics.service.InventoryService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for InventoryService using Mockito
 */

@ExtendWith(MockitoExtension.class)
class InventoryServiceTest {
    @InjectMocks
    private InventoryService inventoryService;
    @Mock
    private Product mockProduct;

    private FoodProduct milk;
    private FoodProduct bread;
    private FoodProduct expiredHam;
    private NonFoodProduct soap;

    @BeforeEach
    void setUp() {
        milk = new FoodProduct(
                "P001", "Milk", 2.50,
                LocalDate.now().plusDays(10), 50, 30.0
        );

        bread = new FoodProduct(
                "P002", "Bread", 1.20,
                LocalDate.now().plusDays(3), 100, 25.0
        );

        expiredHam = new FoodProduct(
                "P008", "Expired Ham", 4.20,
                LocalDate.now().minusDays(5), 12, 28.0
        );

        soap = new NonFoodProduct(
                "P004", "Soap", 3.00,
                LocalDate.now().plusMonths(12), 40, 50.0
        );
    }

    @Test
    void testAddProduct_Milk_Success() throws InvalidProductException {
        inventoryService.addProduct(milk);

        assertEquals(1, inventoryService.getProductCount());
        assertEquals(milk, inventoryService.getProduct("P001"));
    }

    @Test
    void testAddProduct_NullProduct_ThrowsException() {
        InvalidProductException exception = assertThrows(
                InvalidProductException.class,
                () -> inventoryService.addProduct(null)
        );

        assertEquals("Product cannot be null", exception.getMessage());
    }

    @Test
    void testAddProduct_DuplicateId_ThrowsException() throws InvalidProductException {
        inventoryService.addProduct(milk);

        FoodProduct duplicateMilk = new FoodProduct(
                "P001", "Duplicate Milk", 3.00,
                LocalDate.now().plusDays(5), 20, 25.0
        );

        InvalidProductException exception = assertThrows(
                InvalidProductException.class,
                () -> inventoryService.addProduct(duplicateMilk)
        );

        assertTrue(exception.getMessage().contains("already exists"));
    }

    @Test
    void testRestockProduct_Milk_Success() throws InvalidProductException {
        inventoryService.addProduct(milk);
        int initialStock = milk.getQuantityInStock();

        inventoryService.restockProduct("P001", 20);

        assertEquals(initialStock + 20, milk.getQuantityInStock());
    }

    @Test
    void testReduceStock_Milk_Success() throws Exception {
        inventoryService.addProduct(milk);
        int initialStock = milk.getQuantityInStock();

        inventoryService.reduceStock("P001", 10);

        assertEquals(initialStock - 10, milk.getQuantityInStock());
    }

    @Test
    void testReduceStock_InsufficientQuantity_ThrowsException() throws InvalidProductException {
        inventoryService.addProduct(milk);

        InsufficientStockException exception = assertThrows(
                InsufficientStockException.class,
                () -> inventoryService.reduceStock("P001", 100)
        );

        assertEquals("P001", exception.getProductId());
        assertEquals("Milk", exception.getProductName());
        assertEquals(100, exception.getRequestedQuantity());
        assertEquals(50, exception.getAvailableQuantity());
        assertEquals(50, exception.getShortageQuantity());
    }

    @Test
    void testReduceStock_ExpiredHam_ThrowsException() throws InvalidProductException {
        inventoryService.addProduct(expiredHam);

        ExpiredProductException exception = assertThrows(
                ExpiredProductException.class,
                () -> inventoryService.reduceStock("P008", 5)
        );

        assertEquals("P008", exception.getProductId());
        assertEquals("Expired Ham", exception.getProductName());
        assertTrue(exception.getMessage().contains("expired"));
    }

    @Test
    void testGetProduct_NotFound_ThrowsException() {
        InvalidProductException exception = assertThrows(
                InvalidProductException.class,
                () -> inventoryService.getProduct("NONEXISTENT")
        );

        assertTrue(exception.getMessage().contains("does not exist"));
    }

    @Test
    void testGetAvailableProducts_FiltersExpired() throws InvalidProductException {
        FoodProduct yogurt = new FoodProduct(
                "P003", "Yogurt", 1.80,
                LocalDate.now().plusDays(15), 30, 30.0
        );

        inventoryService.addProduct(yogurt);
        inventoryService.addProduct(expiredHam);

        var available = inventoryService.getAvailableProducts();

        // Should only have yogurt, not expired ham
        assertEquals(1, available.size());
        assertEquals("P003", available.get(0).getId());
    }

    @Test
    void testIsAvailable_Milk_ValidProduct_ReturnsTrue() throws InvalidProductException {
        inventoryService.addProduct(milk);

        assertTrue(inventoryService.isAvailable("P001", 10));
        assertTrue(inventoryService.isAvailable("P001", 50));
    }

    @Test
    void testIsAvailable_Milk_InsufficientQuantity_ReturnsFalse() throws InvalidProductException {
        inventoryService.addProduct(milk);

        assertFalse(inventoryService.isAvailable("P001", 100));
    }

    @Test
    void testIsAvailable_ExpiredHam_ReturnsFalse() throws InvalidProductException {
        inventoryService.addProduct(expiredHam);

        assertFalse(inventoryService.isAvailable("P008", 5));
    }

    @Test
    void testGetTotalPurchaseValue() throws InvalidProductException {
        inventoryService.addProduct(milk);    // 2.50 * 50 = 125.00
        inventoryService.addProduct(bread);   // 1.20 * 100 = 120.00
        inventoryService.addProduct(soap);    // 3.00 * 40 = 120.00

        double totalValue = inventoryService.getTotalPurchaseValue();

        // 125 + 120 + 120 = 365
        assertEquals(365.0, totalValue, 0.01);
    }

    @Test
    void testGetTotalPurchaseValue_AfterSale() throws Exception {
        inventoryService.addProduct(milk);
        inventoryService.reduceStock("P001", 7);  // Sell 7, left 43

        double totalValue = inventoryService.getTotalPurchaseValue();

        // 2.50 * 43 = 107.50
        assertEquals(107.50, totalValue, 0.01);
    }

    @Test
    void testMockProduct_IsExpired() {
        when(mockProduct.isExpired()).thenReturn(true);

        boolean expired = mockProduct.isExpired();

        assertTrue(expired);
        verify(mockProduct, times(1)).isExpired();
    }

    @Test
    void testMockProduct_ReduceStock() {
        doNothing().when(mockProduct).reduceStock(10);

        mockProduct.reduceStock(10);

        verify(mockProduct, times(1)).reduceStock(10);
    }

    @Test
    void testAddMultipleProducts_FromMain() throws InvalidProductException {
        inventoryService.addProduct(milk);
        inventoryService.addProduct(bread);
        inventoryService.addProduct(soap);

        FoodProduct yogurt = new FoodProduct(
                "P003", "Yogurt", 1.80,
                LocalDate.now().plusDays(15), 30, 30.0
        );
        inventoryService.addProduct(yogurt);

        assertEquals(4, inventoryService.getProductCount());
        assertEquals(4, inventoryService.getAllProducts().size());
    }
}