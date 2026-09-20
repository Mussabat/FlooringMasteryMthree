package com.flooring.mastery.dao;

import com.flooring.mastery.model.Order;

import java.time.LocalDate;
import java.util.List;

// The service layer only knows this interface, so the storage can be swapped (e.g. a stub in tests).
public interface OrderDao {

    // returns every order of this date, sorted by order number, or an empty list if there are none
    List<Order> getOrdersForDate(LocalDate date) throws FlooringPersistenceException;

    // returns the order with this number on this date, or null if there is none
    Order getOrder(LocalDate date, int orderNumber) throws FlooringPersistenceException;

    // saves a new order (its date is taken from order.getOrderDate()) and returns it
    Order addOrder(Order order) throws FlooringPersistenceException;

    // replaces the saved order that has the same date and order number, and returns the new version
    Order editOrder(Order order) throws FlooringPersistenceException;

    // removes the order and returns it, or returns null if there was no such order
    Order removeOrder(LocalDate date, int orderNumber) throws FlooringPersistenceException;

    // returns the highest order number across all dates, or 0 if there are no orders at all
    int getHighestOrderNumber() throws FlooringPersistenceException;

    // writes every order from every date into one export file (overwrites the old export)
    void exportAllData() throws FlooringPersistenceException;
}
