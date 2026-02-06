package org.informatics.exception;

import java.time.LocalDate;

/**
 * Thrown when attempting to sell a product with expired expiration date
 */
public class ExpiredProductException extends Exception {
    private final String productId;
    private final String productName;
    private final LocalDate expirationDate;

    public ExpiredProductException(String productId, String productName, LocalDate expirationDate) {
        super(String.format(
                "Product '%s' (ID: %s) has expired (expired on: %s)",
                productName, productId, expirationDate));
        this.productId = productId;
        this.productName = productName;
        this.expirationDate = expirationDate;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public LocalDate getExpirationDate() {
        return expirationDate;
    }
}