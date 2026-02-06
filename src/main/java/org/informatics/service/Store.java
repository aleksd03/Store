package org.informatics.service;

import org.informatics.exception.ExpiredProductException;
import org.informatics.exception.InsufficientStockException;
import org.informatics.exception.InvalidProductException;
import org.informatics.model.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.util.*;

/**
 * Main class representing the store
 * Coordinates all operations between different services
 */
public class Store {
    private final String name;
    private final InventoryService inventoryService;
    private final PricingService pricingService;
    private final ReceiptService receiptService;
    private final Map<String, Cashier> cashiers;

    public Store(String name, int expirationThresholdDays,
                 double expirationDiscountPercentage, String receiptsDirectory) {
        this.name = name;
        this.inventoryService = new InventoryService();
        this.pricingService = new PricingService(expirationThresholdDays, expirationDiscountPercentage);
        this.receiptService = new ReceiptService(receiptsDirectory);
        this.cashiers = new HashMap<>();
    }

    // ==================== Cashier operations ====================

    /**
     * Adds a cashier to the store
     */
    public void addCashier(Cashier cashier) throws InvalidProductException {
        if (cashier == null) {
            throw new InvalidProductException("Cashier cannot be null");
        }
        if (cashiers.containsKey(cashier.getId())) {
            throw new InvalidProductException("Cashier with ID " + cashier.getId() + " already exists");
        }
        cashiers.put(cashier.getId(), cashier);
    }

    /**
     * Returns cashier by ID
     */
    public Cashier getCashier(String cashierId) throws InvalidProductException {
        Cashier cashier = cashiers.get(cashierId);
        if (cashier == null) {
            throw new InvalidProductException("Cashier with ID " + cashierId + " does not exist");
        }
        return cashier;
    }

    /**
     * Returns all cashiers
     */
    public List<Cashier> getAllCashiers() {
        return new ArrayList<>(cashiers.values());
    }

    // ==================== Product operations ====================

    /**
     * Adds product to the store
     */
    public void addProduct(Product product) throws InvalidProductException {
        inventoryService.addProduct(product);
    }

    /**
     * Restocks products in the store
     */
    public void restockProduct(String productId, int quantity) throws InvalidProductException {
        inventoryService.restockProduct(productId, quantity);
    }

    /**
     * Returns all available products (without expired ones)
     */
    public List<Product> getAvailableProducts() {
        return inventoryService.getAvailableProducts();
    }

    // ==================== Sale ====================

    /**
     * Performs product sale
     *
     * @param cashierId Cashier ID
     * @param items Map from productId to quantity
     * @param customerPayment Amount paid by customer
     * @return Issued receipt
     */
    public Receipt makeSale(String cashierId, Map<String, Integer> items, double customerPayment)
            throws InvalidProductException, InsufficientStockException,
            ExpiredProductException, IOException {

        // Get the cashier
        Cashier cashier = getCashier(cashierId);

        // Create list of items for the receipt
        List<ReceiptItem> receiptItems = new ArrayList<>();
        double totalAmount = 0.0;

        // Process each product
        for (Map.Entry<String, Integer> entry : items.entrySet()) {
            String productId = entry.getKey();
            int quantity = entry.getValue();

            // Get the product
            Product product = inventoryService.getProduct(productId);

            // Calculate the price
            double unitPrice = pricingService.calculateSalePrice(product);

            // Check availability and validity (throws exceptions on problem)
            inventoryService.reduceStock(productId, quantity);

            // Add item to receipt
            ReceiptItem receiptItem = new ReceiptItem(product, quantity, unitPrice);
            receiptItems.add(receiptItem);
            totalAmount += receiptItem.getTotalPrice();
        }

        // Check if customer has enough money
        if (customerPayment < totalAmount) {
            throw new InvalidProductException(
                    String.format("Insufficient payment amount. Required: %.2f EUR, Received: %.2f EUR",
                            totalAmount, customerPayment));
        }

        // Create the receipt
        Receipt receipt = new Receipt.Builder()
                .receiptNumber(receiptService.getNextReceiptNumber())
                .cashier(cashier)
                .issuedAt(LocalDateTime.now())
                .items(receiptItems)
                .build();

        // Issue the receipt
        receiptService.issueReceipt(receipt);
        receiptService.incrementReceiptNumber();

        // Display receipt in console
        System.out.println(receipt.format());

        return receipt;
    }

    // ==================== Financial reports ====================

    /**
     * Calculates total salary expenses
     */
    public double calculateSalaryExpenses() {
        return cashiers.values().stream()
                .mapToDouble(Cashier::getMonthlySalary)
                .sum();
    }

    /**
     * Calculates total supply expenses
     */
    public double calculateSupplyExpenses() {
        return inventoryService.getTotalPurchaseValue();
    }

    /**
     * Calculates total expenses
     */
    public double calculateTotalExpenses() {
        return calculateSalaryExpenses() + calculateSupplyExpenses();
    }

    /**
     * Calculates total revenue from sales
     */
    public double calculateTotalRevenue() {
        return receiptService.getTotalRevenue();
    }

    /**
     * Calculates profit (revenue - expenses)
     */
    public double calculateProfit() {
        return calculateTotalRevenue() - calculateTotalExpenses();
    }

    /**
     * Returns financial report
     */
    public String getFinancialReport() {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(60)).append("\n");
        sb.append("FINANCIAL REPORT - ").append(name).append("\n");
        sb.append("=".repeat(60)).append("\n");
        sb.append(String.format("Salary expenses: %.2f EUR\n", calculateSalaryExpenses()));
        sb.append(String.format("Supply expenses: %.2f EUR\n", calculateSupplyExpenses()));
        sb.append(String.format("Total expenses: %.2f EUR\n", calculateTotalExpenses()));
        sb.append("-".repeat(60)).append("\n");
        sb.append(String.format("Revenue from sales: %.2f EUR\n", calculateTotalRevenue()));
        sb.append("-".repeat(60)).append("\n");
        sb.append(String.format("PROFIT: %.2f EUR\n", calculateProfit()));
        sb.append("=".repeat(60)).append("\n");
        sb.append(String.format("Number of issued receipts: %d\n",
                receiptService.getTotalReceiptsCount()));
        sb.append("=".repeat(60)).append("\n");
        return sb.toString();
    }

    // ==================== Getters ====================

    public String getName() {
        return name;
    }

    public InventoryService getInventoryService() {
        return inventoryService;
    }

    public PricingService getPricingService() {
        return pricingService;
    }

    public ReceiptService getReceiptService() {
        return receiptService;
    }

    public int getTotalReceiptsCount() {
        return receiptService.getTotalReceiptsCount();
    }
}