package com.flooring.mastery.service;

// Thrown by the service layer when the user's input breaks a business rule
// (e.g. date not in the future, unknown state, area below 100).
public class OrderValidationException extends Exception {

    public OrderValidationException(String message) {
        super(message);
    }
}
