package com.flooring.mastery.dao;

// Thrown by the DAO layer when a data file cannot be read or written, or holds bad data.
// It is a checked exception (extends Exception)
public class FlooringPersistenceException extends Exception {

    // used when we detect the problem ourselves
    public FlooringPersistenceException(String message) {
        super(message);
    }

    // used when we catch another exception (IOException, NumberFormatException) and wrap it,
    public FlooringPersistenceException(String message, Throwable cause) {
        super(message, cause);
    }
}
