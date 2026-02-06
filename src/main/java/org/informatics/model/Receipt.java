package org.informatics.model;

import java.io.Serializable;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a receipt
 */
public class Receipt implements Serializable {
    private static final long serialVersionUID = 1L;
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("dd.MM.yyyy HH:mm:ss");

    private final int receiptNumber;
    private final Cashier cashier;
    private final LocalDateTime issuedAt;
    private final List<ReceiptItem> items;
    private final double totalAmount;

    private Receipt(Builder builder) {
        this.receiptNumber = builder.receiptNumber;
        this.cashier = builder.cashier;
        this.issuedAt = builder.issuedAt;
        this.items = new ArrayList<>(builder.items);
        this.totalAmount = calculateTotal();
    }

    private double calculateTotal() {
        return items.stream()
                .mapToDouble(ReceiptItem::getTotalPrice)
                .sum();
    }

    public int getReceiptNumber() {
        return receiptNumber;
    }

    public Cashier getCashier() {
        return cashier;
    }

    public LocalDateTime getIssuedAt() {
        return issuedAt;
    }

    public List<ReceiptItem> getItems() {
        return Collections.unmodifiableList(items);
    }

    public double getTotalAmount() {
        return totalAmount;
    }

    /**
     * Formats the receipt for display/storage
     */
    public String format() {
        StringBuilder sb = new StringBuilder();
        sb.append("=".repeat(50)).append("\n");
        sb.append("RECEIPT #").append(receiptNumber).append("\n");
        sb.append("=".repeat(50)).append("\n");
        sb.append("Cashier: ").append(cashier.getName()).append(" (").append(cashier.getId()).append(")\n");
        sb.append("Date and time: ").append(issuedAt.format(DATE_FORMATTER)).append("\n");
        sb.append("-".repeat(50)).append("\n");
        sb.append("ARTICLES:\n");
        sb.append("-".repeat(50)).append("\n");

        for (ReceiptItem item : items) {
            sb.append(item.toString()).append("\n");
        }

        sb.append("-".repeat(50)).append("\n");
        sb.append(String.format("SUM: %.2f EUR\n", totalAmount));
        sb.append("=".repeat(50)).append("\n");

        return sb.toString();
    }

    @Override
    public String toString() {
        return "Receipt{" +
                "receiptNumber=" + receiptNumber +
                ", cashier=" + cashier.getName() +
                ", issuedAt=" + issuedAt +
                ", totalAmount=" + totalAmount +
                ", itemsCount=" + items.size() +
                '}';
    }

    /**
     * Builder for creating Receipt objects
     */
    public static class Builder {
        private int receiptNumber;
        private Cashier cashier;
        private LocalDateTime issuedAt;
        private List<ReceiptItem> items = new ArrayList<>();

        public Builder receiptNumber(int receiptNumber) {
            this.receiptNumber = receiptNumber;
            return this;
        }

        public Builder cashier(Cashier cashier) {
            this.cashier = cashier;
            return this;
        }

        public Builder issuedAt(LocalDateTime issuedAt) {
            this.issuedAt = issuedAt;
            return this;
        }

        public Builder addItem(ReceiptItem item) {
            this.items.add(item);
            return this;
        }

        public Builder items(List<ReceiptItem> items) {
            this.items = new ArrayList<>(items);
            return this;
        }

        public Receipt build() {
            if (cashier == null) {
                throw new IllegalStateException("Cashier is required");
            }
            if (issuedAt == null) {
                issuedAt = LocalDateTime.now();
            }
            if (items.isEmpty()) {
                throw new IllegalStateException("Receipt must have at least one item");
            }
            return new Receipt(this);
        }
    }
}