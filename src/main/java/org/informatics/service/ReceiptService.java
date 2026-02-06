package org.informatics.service;

import org.informatics.model.Receipt;
import org.informatics.util.FileManager;
import org.informatics.util.SerializationUtil;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Service for managing receipts
 */
public class ReceiptService {
    private final List<Receipt> receipts;
    private int nextReceiptNumber;
    private double totalRevenue;
    private final String receiptsDirectory;

    public ReceiptService(String receiptsDirectory) {
        this.receipts = new ArrayList<>();
        this.nextReceiptNumber = 1;
        this.totalRevenue = 0.0;
        this.receiptsDirectory = receiptsDirectory;
    }

    /**
     * Issues a new receipt
     */
    public Receipt issueReceipt(Receipt receipt) throws IOException {
        // Add receipt to the list
        receipts.add(receipt);
        totalRevenue += receipt.getTotalAmount();

        // Write to file
        String fileName = String.format("receipt_%d.txt", receipt.getReceiptNumber());
        String filePath = receiptsDirectory + "/" + fileName;
        FileManager.writeToFile(filePath, receipt.format());

        // Serialize the receipt
        String serializedFileName = String.format("receipt_%d.ser", receipt.getReceiptNumber());
        String serializedFilePath = receiptsDirectory + "/" + serializedFileName;
        SerializationUtil.serialize(receipt, serializedFilePath);

        return receipt;
    }

    /**
     * Returns the next receipt number
     */
    public int getNextReceiptNumber() {
        return nextReceiptNumber;
    }

    /**
     * Increments the receipt counter
     */
    public void incrementReceiptNumber() {
        nextReceiptNumber++;
    }

    /**
     * Returns the total number of issued receipts
     */
    public int getTotalReceiptsCount() {
        return receipts.size();
    }

    /**
     * Returns the total turnover from issued receipts
     */
    public double getTotalRevenue() {
        return totalRevenue;
    }

    /**
     * Returns all issued receipts
     */
    public List<Receipt> getAllReceipts() {
        return Collections.unmodifiableList(receipts);
    }

    /**
     * Reads a receipt from file
     */
    public String readReceiptFromFile(int receiptNumber) throws IOException {
        String fileName = String.format("receipt_%d.txt", receiptNumber);
        String filePath = receiptsDirectory + "/" + fileName;
        return FileManager.readFromFile(filePath);
    }

    /**
     * Deserializes a receipt
     */
    public Receipt deserializeReceipt(int receiptNumber) throws IOException, ClassNotFoundException {
        String serializedFileName = String.format("receipt_%d.ser", receiptNumber);
        String serializedFilePath = receiptsDirectory + "/" + serializedFileName;
        return (Receipt) SerializationUtil.deserialize(serializedFilePath);
    }
}