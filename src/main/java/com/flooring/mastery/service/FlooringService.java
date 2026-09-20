package com.flooring.mastery.service;

import com.flooring.mastery.dao.FlooringPersistenceException;
import com.flooring.mastery.model.Order;
import com.flooring.mastery.model.Product;
import com.flooring.mastery.model.Tax;

import java.time.LocalDate;
import java.util.List;

// The controller only knows this interface, so it never sees files or DAOs.
public interface FlooringService {

    // returns every order of this date; throws NoSuchOrderException if there are none
    List<Order> getOrdersForDate(LocalDate date) throws FlooringPersistenceException, NoSuchOrderException;

    // returns one order; throws NoSuchOrderException if it does not exist
    Order getOrder(LocalDate date, int orderNumber) throws FlooringPersistenceException, NoSuchOrderException;

    // returns every product we sell
    List<Product> getAllProducts() throws FlooringPersistenceException;

    // returns every state (with its tax rate) we sell in
    List<Tax> getAllTaxes() throws FlooringPersistenceException;
}
