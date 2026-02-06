package org.informatics.model;

import java.io.Serializable;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.Objects;

/**
 * Abstract base class for all products in the store
 */
public abstract class Product implements Serializable {
    private static final long serialVersionUID = 1L;

    private final String id;
    private final String name;
    private final double purchasePrice;
    private final ProductCategory category;
    private final LocalDate expirationDate;
    private int quantityInStock;

    public Product(String id, String name, double purchasePrice,
                   ProductCategory category, LocalDate expirationDate, int quantityInStock) {
        this.id = id;
        this.name = name;
        this.purchasePrice = purchasePrice;
        this.category = category;
        this.expirationDate = expirationDate;
        this.quantityInStock = quantityInStock;
    }

    /**
     * Abstract method for calculating sale price
     * Each product type has its own pricing logic
     */
    public abstract double calculateSalePrice(int daysUntilExpiration, int expirationThreshold,
                                              double discountPercentage);

    /**
     * Checks if the product has expired
     */
    public boolean isExpired() {
        return LocalDate.now().isAfter(expirationDate);
    }

    /**
     * Calculates how many days remain until expiration
     */
    public int getDaysUntilExpiration() {
        return (int) ChronoUnit.DAYS.between(LocalDate.now(), expirationDate);
    }

    /**
     * Reduces available quantity on sale
     */
    public void reduceStock(int quantity) {
        this.quantityInStock -= quantity;
    }

    /**
     * Increases available quantity on restock
     */
    public void addStock(int quantity) {
        this.quantityInStock += quantity;
    }

    // Getters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public double getPurchasePrice() {
        return purchasePrice;
    }

    public ProductCategory getCategory() {
        return category;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }

    public int getQuantityInStock() {
        return quantityInStock;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return Objects.equals(id, product.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "Product{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                ", category=" + category +
                ", quantityInStock=" + quantityInStock +
                ", expirationDate=" + expirationDate +
                '}';
    }
}