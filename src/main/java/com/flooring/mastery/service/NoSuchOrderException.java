package com.flooring.mastery.service;

// Thrown by the service layer when there are no orders for a date, or an order number does not exist.
// It is a checked exception (extends Exception), so the controller must handle it.
public class NoSuchOrderException extends Exception {

    public NoSuchOrderException(String message) {
        super(message);
    }
}
