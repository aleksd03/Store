package org.informatics.exception;

/**
 * Thrown when there is insufficient stock quantity for a product
 */
public class InsufficientStockException extends Exception {
    private final String productId;
    private final String productName;
    private final int requestedQuantity;
    private final int availableQuantity;
    private final int shortageQuantity;

    public InsufficientStockException(String productId, String productName,
                                      int requestedQuantity, int availableQuantity) {
        super(String.format(
                "Insufficient quantity of product '%s' (ID: %s). " +
                        "Requested: %d, Available: %d, Missing: %d",
                productName, productId, requestedQuantity, availableQuantity,
                requestedQuantity - availableQuantity));
        this.productId = productId;
        this.productName = productName;
        this.requestedQuantity = requestedQuantity;
        this.availableQuantity = availableQuantity;
        this.shortageQuantity = requestedQuantity - availableQuantity;
    }

    public String getProductId() {
        return productId;
    }

    public String getProductName() {
        return productName;
    }

    public int getRequestedQuantity() {
        return requestedQuantity;
    }

    public int getAvailableQuantity() {
        return availableQuantity;
    }

    public int getShortageQuantity() {
        return shortageQuantity;
    }
}