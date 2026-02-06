package service;

import org.informatics.model.FoodProduct;
import org.informatics.model.NonFoodProduct;
import org.informatics.model.Product;
import org.informatics.service.PricingService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDate;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

/**
 * Unit tests for PricingService using Mockito
 */
@ExtendWith(MockitoExtension.class)
class PricingServiceTest {
    private PricingService pricingService;
    @Mock
    private Product mockProduct;

    @BeforeEach
    void setUp() {
        pricingService = new PricingService(5, 20.0);
    }

    @Test
    void testCalculateSalePrice_Milk_NoDiscount() {
        // Milk - expires in 10 days, no discount
        FoodProduct milk = new FoodProduct(
                "P001", "Milk", 2.50,
                LocalDate.now().plusDays(10), 50, 30.0
        );

        double price = pricingService.calculateSalePrice(milk);

        // 2.50 * 1.30 = 3.25 (no discount as expires in 10 days)
        assertEquals(3.25, price, 0.01);
    }

    @Test
    void testCalculateSalePrice_Bread_WithDiscount() {
        // Bread - expires in 3 days, has discount
        FoodProduct bread = new FoodProduct(
                "P002", "Bread", 1.20,
                LocalDate.now().plusDays(3), 100, 25.0
        );

        double price = pricingService.calculateSalePrice(bread);

        // Base: 1.20 * 1.25 = 1.50
        // With discount: 1.50 * 0.80 = 1.20
        assertEquals(1.20, price, 0.01);
    }

    @Test
    void testCalculateSalePrice_Cheese_WithDiscount() {
        // Cheese - expires in 2 days, has discount
        FoodProduct cheese = new FoodProduct(
                "P006", "Cheese", 5.50,
                LocalDate.now().plusDays(2), 20, 35.0
        );

        double price = pricingService.calculateSalePrice(cheese);

        // Base: 5.50 * 1.35 = 7.425
        // With discount: 7.425 * 0.80 = 5.94
        assertEquals(5.94, price, 0.01);
    }

    @Test
    void testCalculateSalePrice_Soap_NoDiscount() {
        // Soap - non-food, expires in 12 months
        NonFoodProduct soap = new NonFoodProduct(
                "P004", "Soap", 3.00,
                LocalDate.now().plusMonths(12), 40, 50.0
        );

        double price = pricingService.calculateSalePrice(soap);

        // 3.00 * 1.50 = 4.50
        assertEquals(4.50, price, 0.01);
    }

    @Test
    void testCalculateSalePrice_Shampoo_NoDiscount() {
        // Shampoo
        NonFoodProduct shampoo = new NonFoodProduct(
                "P005", "Shampoo", 8.00,
                LocalDate.now().plusMonths(18), 25, 45.0
        );

        double price = pricingService.calculateSalePrice(shampoo);

        // 8.00 * 1.45 = 11.60
        assertEquals(11.60, price, 0.01);
    }

    @Test
    void testCalculateSalePrice_Toothpaste_NoDiscount() {
        // Toothpaste
        NonFoodProduct toothpaste = new NonFoodProduct(
                "P007", "Toothpaste", 4.50,
                LocalDate.now().plusMonths(24), 35, 40.0
        );

        double price = pricingService.calculateSalePrice(toothpaste);

        // 4.50 * 1.40 = 6.30
        assertEquals(6.30, price, 0.01);
    }

    @Test
    void testCalculateSalePrice_ExactThreshold() {
        // Product expires exactly at threshold (5 days)
        FoodProduct product = new FoodProduct(
                "P999", "Test Product", 2.00,
                LocalDate.now().plusDays(5), 10, 30.0
        );

        double price = pricingService.calculateSalePrice(product);

        // Should apply discount when days == threshold
        // Base: 2.00 * 1.30 = 2.60
        // With discount: 2.60 * 0.80 = 2.08
        assertEquals(2.08, price, 0.01);
    }

    @Test
    void testCalculateSalePrice_OneDayBeforeExpiration() {
        FoodProduct product = new FoodProduct(
                "P999", "Expiring Soon", 1.00,
                LocalDate.now().plusDays(1), 5, 50.0
        );

        double price = pricingService.calculateSalePrice(product);

        // Base: 1.00 * 1.50 = 1.50
        // With discount: 1.50 * 0.80 = 1.20
        assertEquals(1.20, price, 0.01);
    }

    @Test
    void testGetExpirationThresholdDays() {
        assertEquals(5, pricingService.getExpirationThresholdDays());
    }

    @Test
    void testGetExpirationDiscountPercentage() {
        assertEquals(20.0, pricingService.getExpirationDiscountPercentage(), 0.01);
    }

    @Test
    void testMockProduct_CalculateSalePrice() {
        // Arrange
        when(mockProduct.getDaysUntilExpiration()).thenReturn(10);
        when(mockProduct.calculateSalePrice(10, 5, 20.0)).thenReturn(3.25);

        // Act
        double price = pricingService.calculateSalePrice(mockProduct);

        // Assert
        assertEquals(3.25, price, 0.01);
        verify(mockProduct, times(1)).getDaysUntilExpiration();
        verify(mockProduct, times(1)).calculateSalePrice(10, 5, 20.0);
    }

    @Test
    void testYogurt_NoDiscount() {
        // Yogurt - expires in 15 days, no discount
        FoodProduct yogurt = new FoodProduct(
                "P003", "Yogurt", 1.80,
                LocalDate.now().plusDays(15), 30, 30.0
        );

        double price = pricingService.calculateSalePrice(yogurt);

        // 1.80 * 1.30 = 2.34 (no discount)
        assertEquals(2.34, price, 0.01);
    }
}