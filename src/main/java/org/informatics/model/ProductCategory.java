package org.informatics.model;

/**
 * Product categories in the store
 */
public enum ProductCategory {
    FOOD("Food products"),
    NON_FOOD("Non-Food products"),;

    private final String displayName;

    ProductCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}