package org.informatics.model;

import java.io.Serial;
import java.time.LocalDate;

/**
 * Represents a food product in the store
 */
public class FoodProduct extends Product {
    private static final long serialVersionUID = 1L;

    private final double markupPercentage;

    public FoodProduct(String id, String name, double purchasePrice,
                       LocalDate expirationDate, int quantityInStock, double markupPercentage) {
        super(id, name, purchasePrice, ProductCategory.FOOD, expirationDate, quantityInStock);
        this.markupPercentage = markupPercentage;
    }

    @Override
    public double calculateSalePrice(int daysUntilExpiration, int expirationThreshold,
                                     double discountPercentage) {
        // Base price with markup
        double basePrice = getPurchasePrice() * (1 + markupPercentage / 100.0);

        // Apply discount if nearing expiration
        if (daysUntilExpiration <= expirationThreshold && daysUntilExpiration > 0) {
            basePrice = basePrice * (1 - discountPercentage / 100.0);
        }

        return basePrice;
    }

    public double getMarkupPercentage() {
        return markupPercentage;
    }

    @Override
    public String toString() {
        return "FoodProduct{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", markupPercentage=" + markupPercentage +
                ", quantityInStock=" + getQuantityInStock() +
                ", expirationDate=" + getExpirationDate() +
                '}';
    }
}