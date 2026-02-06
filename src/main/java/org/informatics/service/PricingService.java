package org.informatics.service;

import org.informatics.model.Product;

/**
 * Service for calculating product prices
 */
public class PricingService {
    private final int expirationThresholdDays;
    private final double expirationDiscountPercentage;

    /**
     * @param expirationThresholdDays Days before expiration when discount is applied
     * @param expirationDiscountPercentage Discount percentage for nearing expiration
     */
    public PricingService(int expirationThresholdDays, double expirationDiscountPercentage) {
        this.expirationThresholdDays = expirationThresholdDays;
        this.expirationDiscountPercentage = expirationDiscountPercentage;
    }

    /**
     * Calculates the sale price of a product according to store rules
     */
    public double calculateSalePrice(Product product) {
        int daysUntilExpiration = product.getDaysUntilExpiration();
        return product.calculateSalePrice(daysUntilExpiration,
                expirationThresholdDays,
                expirationDiscountPercentage);
    }

    public int getExpirationThresholdDays() {
        return expirationThresholdDays;
    }

    public double getExpirationDiscountPercentage() {
        return expirationDiscountPercentage;
    }
}