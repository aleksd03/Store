package org.informatics.model;

/**
 * Product categories in the store
 */
public enum ProductCategory {
    FOOD("Хранителни стоки"),
    NON_FOOD("Нехранителни стоки");

    private final String displayName;

    ProductCategory(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }
}