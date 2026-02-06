package org.informatics;

import org.informatics.exception.ExpiredProductException;
import org.informatics.exception.InsufficientStockException;
import org.informatics.exception.InvalidProductException;
import org.informatics.model.*;
import org.informatics.service.Store;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

public class Main {
    public static void main(String[] args) {
        try {
            // Create shop
            Store store = new Store(
                    "Shop NBU",
                    5,      // When less than 5 days left until expiration then -->
                    20.0,   // --> 20% discount
                    "receipts"
            );

            System.out.println("=== CREATING STORE ===");
            System.out.println("Store: " + store.getName());
            System.out.println();

            // Add cashiers
            System.out.println("=== ADDING CASHIERS ===");
            Cashier cashier1 = new Cashier("C001", "Ivan Ivanov", 1500.0);
            Cashier cashier2 = new Cashier("C002", "Mariya Popova", 1600.0);
            store.addCashier(cashier1);
            store.addCashier(cashier2);
            System.out.println("Added cashiers: " + cashier1.getName() + ", " + cashier2.getName());
            System.out.println();

            // Add products
            System.out.println("=== LOADING PRODUCTS ===");

            // Food products
            Product milk = new FoodProduct(
                    "P001",
                    "Milk",
                    2.50,
                    LocalDate.now().plusDays(10),
                    50,
                    30.0
            );

            Product bread = new FoodProduct(
                    "P002",
                    "Bread",
                    1.20,
                    LocalDate.now().plusDays(3),  // Soon expiring - will have discount
                    100,
                    25.0
            );

            Product yogurt = new FoodProduct(
                    "P003",
                    "Yogurt",
                    1.80,
                    LocalDate.now().plusDays(15),  // Valid product
                    30,
                    30.0
            );

            Product cheese = new FoodProduct(
                    "P006",
                    "Cheese",
                    5.50,
                    LocalDate.now().plusDays(2),  // Soon expiring - discount
                    20,
                    35.0
            );

            Product expiredHam = new FoodProduct(
                    "P008",
                    "Expired Ham",
                    4.20,
                    LocalDate.now().minusDays(5),  // Expired 5 days ago
                    12,
                    28.0
            );

            // Non-food products
            Product soap = new NonFoodProduct(
                    "P004",
                    "Soap",
                    3.00,
                    LocalDate.now().plusMonths(12),
                    40,
                    50.0
            );

            Product shampoo = new NonFoodProduct(
                    "P005",
                    "Shampoo",
                    8.00,
                    LocalDate.now().plusMonths(18),
                    25,
                    45.0
            );

            Product toothpaste = new NonFoodProduct(
                    "P007",
                    "Toothpaste",
                    4.50,
                    LocalDate.now().plusMonths(24),
                    35,
                    40.0
            );

            store.addProduct(milk);
            store.addProduct(bread);
            store.addProduct(yogurt);
            store.addProduct(cheese);
            store.addProduct(expiredHam);
            store.addProduct(soap);
            store.addProduct(shampoo);
            store.addProduct(toothpaste);

            System.out.println("Added 8 products");
            System.out.println("Available products (without expired):");
            store.getAvailableProducts().forEach(p -> {
                double price = store.getPricingService().calculateSalePrice(p);
                boolean hasDiscount = p.getDaysUntilExpiration() <= store.getPricingService().getExpirationThresholdDays();
                System.out.printf("  - %s (Price: %.2f EUR)%s\n",
                        p.getName(),
                        price,
                        hasDiscount ? " [DISCOUNT]" : "");
            });
            System.out.println();

            // ============================================================
            // SALE #1 - Simple purchase with regular items
            // ============================================================
            System.out.println("=== SALE #1 - MORNING SHOPPING ===");
            System.out.println("Customer wants to buy:");
            System.out.println("  - 2 pcs. Milk");
            System.out.println("  - 1 pc. Soap");
            System.out.println("Cashier: " + cashier1.getName());
            System.out.println();

            Map<String, Integer> sale1 = new HashMap<>();
            sale1.put("P001", 2);  // Milk
            sale1.put("P004", 1);  // Soap

            Receipt receipt1 = store.makeSale("C001", sale1, 20.0);
            System.out.println("Sale completed! Change: " +
                    String.format("%.2f EUR", 20.0 - receipt1.getTotalAmount()));
            System.out.println();

            // ============================================================
            // SALE #2 - Purchase with discounted items (near expiration)
            // ============================================================
            System.out.println("=== SALE #2 - DISCOUNT SHOPPING ===");
            System.out.println("Customer wants to buy:");
            System.out.println("  - 3 pcs. Bread [DISCOUNT - expires soon]");
            System.out.println("  - 2 pcs. Cheese [DISCOUNT - expires soon]");
            System.out.println("  - 1 pc. Yogurt");
            System.out.println("Cashier: " + cashier2.getName());
            System.out.println();

            Map<String, Integer> sale2 = new HashMap<>();
            sale2.put("P002", 3);  // Bread (with discount)
            sale2.put("P006", 2);  // Cheese (with discount)
            sale2.put("P003", 1);  // Yogurt

            Receipt receipt2 = store.makeSale("C002", sale2, 30.0);
            System.out.println("Sale completed! Change: " +
                    String.format("%.2f EUR", 30.0 - receipt2.getTotalAmount()));
            System.out.println();

            // ============================================================
            // SALE #3 - Large purchase with mixed items
            // ============================================================
            System.out.println("=== SALE #3 - WEEKLY SHOPPING ===");
            System.out.println("Customer wants to buy:");
            System.out.println("  - 5 pcs. Milk");
            System.out.println("  - 5 pcs. Bread [DISCOUNT]");
            System.out.println("  - 3 pcs. Yogurt");
            System.out.println("  - 2 pcs. Shampoo");
            System.out.println("  - 1 pc. Toothpaste");
            System.out.println("Cashier: " + cashier1.getName());
            System.out.println();

            Map<String, Integer> sale3 = new HashMap<>();
            sale3.put("P001", 5);  // Milk
            sale3.put("P002", 5);  // Bread (with discount)
            sale3.put("P003", 3);  // Yogurt
            sale3.put("P005", 2);  // Shampoo
            sale3.put("P007", 1);  // Toothpaste

            Receipt receipt3 = store.makeSale("C001", sale3, 100.0);
            System.out.println("Sale completed! Change: " +
                    String.format("%.2f EUR", 100.0 - receipt3.getTotalAmount()));
            System.out.println();

            // ============================================================
            // ERROR DEMONSTRATION #1 - EXPIRED PRODUCT
            // ============================================================
            System.out.println("=== ERROR DEMONSTRATION #1 - EXPIRED PRODUCT ===");
            System.out.println("Customer attempts to buy expired ham...");
            try {
                Map<String, Integer> expiredItems = new HashMap<>();
                expiredItems.put("P008", 3);  // Expired Ham
                store.makeSale("C001", expiredItems, 20.0);
            } catch (ExpiredProductException e) {
                System.out.println("SALE REJECTED!");
                System.out.println("Reason: " + e.getMessage());
                System.out.println();
            }

            // ============================================================
            // ERROR DEMONSTRATION #2 - INSUFFICIENT STOCK
            // ============================================================
            System.out.println("=== ERROR DEMONSTRATION #2 - INSUFFICIENT STOCK ===");
            System.out.println("Customer wants to buy 100 bottles of Milk...");
            System.out.println("(Stock remaining after previous sales: 43 bottles)");
            try {
                Map<String, Integer> tooManyItems = new HashMap<>();
                tooManyItems.put("P001", 100);  // Want 100 milk, but only 43 available
                store.makeSale("C002", tooManyItems, 500.0);
            } catch (InsufficientStockException e) {
                System.out.println("SALE REJECTED!");
                System.out.println("Reason: " + e.getMessage());
                System.out.println("Available: " + e.getAvailableQuantity() + " bottles");
                System.out.println("Missing: " + e.getShortageQuantity() + " bottles");
                System.out.println();
            }

            // ============================================================
            // ERROR DEMONSTRATION #3 - INSUFFICIENT PAYMENT
            // ============================================================
            System.out.println("=== ERROR DEMONSTRATION #3 - INSUFFICIENT PAYMENT ===");
            System.out.println("Customer wants to buy:");
            System.out.println("  - 2 pcs. Shampoo (11.60 EUR each = 23.20 EUR total)");
            System.out.println("Customer pays: 10.00 EUR");
            try {
                Map<String, Integer> items2 = new HashMap<>();
                items2.put("P005", 2);  // Shampoo
                store.makeSale("C002", items2, 10.0);  // Customer gives only 10 EUR
            } catch (InvalidProductException e) {
                System.out.println("SALE REJECTED!");
                System.out.println("Reason: " + e.getMessage());
                System.out.println();
            }

            // ============================================================
            // FINANCIAL REPORT
            // ============================================================
            System.out.println("=== FINANCIAL REPORT ===");
            System.out.println(store.getFinancialReport());

            // ============================================================
            // FILE OPERATIONS DEMONSTRATION
            // ============================================================
            System.out.println("=== READING RECEIPT FROM FILE ===");
            System.out.println("Reading receipt #2 (Discount Shopping):");
            String receiptContent = store.getReceiptService().readReceiptFromFile(2);
            System.out.println(receiptContent);

            System.out.println("=== RECEIPT DESERIALIZATION ===");
            Receipt deserializedReceipt = store.getReceiptService().deserializeReceipt(3);
            System.out.println("Deserialized receipt #3:");
            System.out.println("  Receipt Number: " + deserializedReceipt.getReceiptNumber());
            System.out.println("  Cashier: " + deserializedReceipt.getCashier().getName());
            System.out.println("  Items: " + deserializedReceipt.getItems().size());
            System.out.println("  Total: " + String.format("%.2f EUR", deserializedReceipt.getTotalAmount()));
            System.out.println();

            // ============================================================
            // SUMMARY
            // ============================================================
            System.out.println("=== SESSION SUMMARY ===");
            System.out.println("Total receipts issued: " + store.getTotalReceiptsCount());
            System.out.println("Total revenue: " + String.format("%.2f EUR", store.calculateTotalRevenue()));
            System.out.println("Products in stock: " + store.getAvailableProducts().size());
            System.out.println();
            System.out.println("All sales completed successfully!");
            System.out.println("Receipt files created in /receipts directory");
            System.out.println("Data serialization working properly");

        } catch (Exception e) {
            System.err.println("ERROR: " + e.getMessage());
            e.printStackTrace();
        }
    }
}