package org.informatics.model;

import java.io.Serializable;

/**
 * Represents a single item in a receipt
 */
public class ReceiptItem implements Serializable {
    private static final long serialVersionUID = 1L;

    private final Product product;
    private final int quantity;
    private final double unitPrice;
    private final double totalPrice;

    public ReceiptItem(Product product, int quantity, double unitPrice) {
        this.product = product;
        this.quantity = quantity;
        this.unitPrice = unitPrice;
        this.totalPrice = quantity * unitPrice;
    }

    public Product getProduct() {
        return product;
    }

    public int getQuantity() {
        return quantity;
    }

    public double getUnitPrice() {
        return unitPrice;
    }

    public double getTotalPrice() {
        return totalPrice;
    }

    @Override
    public String toString() {
        return String.format("%s x %d @ %.2f EUR = %.2f EUR",
                product.getName(), quantity, unitPrice, totalPrice);
    }
}