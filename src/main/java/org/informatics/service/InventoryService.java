package org.informatics.service;

import org.informatics.exception.ExpiredProductException;
import org.informatics.exception.InsufficientStockException;
import org.informatics.exception.InvalidProductException;
import org.informatics.model.Product;

import java.util.*;

/**
 * Service for managing inventory (stock levels)
 */
public class InventoryService {
    private final Map<String, Product> products;

    public InventoryService() {
        this.products = new HashMap<>();
    }

    /**
     * Adds a new product to the inventory
     */
    public void addProduct(Product product) throws InvalidProductException {
        if (product == null) {
            throw new InvalidProductException("Product cannot be null");
        }
        if (products.containsKey(product.getId())) {
            throw new InvalidProductException("Product with ID " + product.getId() + " already exists");
        }
        products.put(product.getId(), product);
    }

    /**
     * Restocks an additional quantity of an existing product
     */
    public void restockProduct(String productId, int quantity) throws InvalidProductException {
        Product product = products.get(productId);
        if (product == null) {
            throw new InvalidProductException("Product with ID " + productId + " does not exist");
        }
        if (quantity <= 0) {
            throw new InvalidProductException("Quantity must be a positive number");
        }
        product.addStock(quantity);
    }

    /**
     * Reduces quantity on sale
     */
    public void reduceStock(String productId, int quantity)
            throws InsufficientStockException, ExpiredProductException, InvalidProductException {
        Product product = products.get(productId);

        if (product == null) {
            throw new InvalidProductException("Product with ID " + productId + " does not exist");
        }

        // Check for expiration
        if (product.isExpired()) {
            throw new ExpiredProductException(product.getId(), product.getName(),
                    product.getExpirationDate());
        }

        // Check for availability
        if (product.getQuantityInStock() < quantity) {
            throw new InsufficientStockException(product.getId(), product.getName(),
                    quantity, product.getQuantityInStock());
        }

        product.reduceStock(quantity);
    }

    /**
     * Returns a product by ID
     */
    public Product getProduct(String productId) throws InvalidProductException {
        Product product = products.get(productId);
        if (product == null) {
            throw new InvalidProductException("Product with ID " + productId + " does not exist");
        }
        return product;
    }

    /**
     * Returns all products
     */
    public List<Product> getAllProducts() {
        return new ArrayList<>(products.values());
    }

    /**
     * Returns all products that have not expired
     */
    public List<Product> getAvailableProducts() {
        return products.values().stream()
                .filter(p -> !p.isExpired())
                .toList();
    }

    /**
     * Returns the total value of purchased goods (supply expenses)
     */
    public double getTotalPurchaseValue() {
        return products.values().stream()
                .mapToDouble(p -> p.getPurchasePrice() * p.getQuantityInStock())
                .sum();
    }

    /**
     * Checks if a product is available in sufficient quantity
     */
    public boolean isAvailable(String productId, int quantity) {
        Product product = products.get(productId);
        return product != null &&
                !product.isExpired() &&
                product.getQuantityInStock() >= quantity;
    }

    /**
     * Returns the number of products in inventory
     */
    public int getProductCount() {
        return products.size();
    }
}