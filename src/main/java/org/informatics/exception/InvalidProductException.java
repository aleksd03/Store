package org.informatics.exception;

/**
 * Thrown for invalid product operations
 */
public class InvalidProductException extends Exception {
    public InvalidProductException(String message) {
        super(message);
    }

    public InvalidProductException(String message, Throwable cause) {
        super(message, cause);
    }
}