package org.informatics.model;

import java.time.LocalDate;

/**
 * Represents a non-food product in the store
 */
public class NonFoodProduct extends Product {
    private static final long serialVersionUID = 1L;

    private final double markupPercentage;

    public NonFoodProduct(String id, String name, double purchasePrice,
                          LocalDate expirationDate, int quantityInStock, double markupPercentage) {
        super(id, name, purchasePrice, ProductCategory.NON_FOOD, expirationDate, quantityInStock);
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
        return "NonFoodProduct{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", markupPercentage=" + markupPercentage +
                ", quantityInStock=" + getQuantityInStock() +
                ", expirationDate=" + getExpirationDate() +
                '}';
    }
}