package com.flooring.mastery.service;

import com.flooring.mastery.dao.FlooringPersistenceException;
import com.flooring.mastery.model.Order;
import com.flooring.mastery.model.Product;
import com.flooring.mastery.model.Tax;

import java.math.BigDecimal;
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

    // the order date must be after today; returns the date if it is fine
    LocalDate validateOrderDate(LocalDate date) throws OrderValidationException;

    // the name may not be blank and may only use letters, digits, spaces, periods and commas;
    // returns the name without spaces at the start/end
    String validateCustomerName(String customerName) throws OrderValidationException;

    // the state must exist in the tax data (ignoring case); returns its Tax
    Tax validateState(String state) throws FlooringPersistenceException, OrderValidationException;

    // the product must exist in the product data (ignoring case); returns its Product
    Product validateProductType(String productType) throws FlooringPersistenceException, OrderValidationException;

    // the area must be at least 100 sq ft; returns it with 2 decimal places
    BigDecimal validateArea(BigDecimal area) throws OrderValidationException;

    // fills in the state, tax rate, product, prices and all costs of the order; returns the same order
    Order calculateOrder(Order order, Tax tax, Product product);

    // gives the order the next order number and saves it
    Order addOrder(Order order) throws FlooringPersistenceException;

    // saves the edited order (same date + order number as the saved one); throws NoSuchOrderException if it does not exist
    Order editOrder(Order order) throws FlooringPersistenceException, NoSuchOrderException;

    // removes the order and returns it; throws NoSuchOrderException if it does not exist
    Order removeOrder(LocalDate date, int orderNumber) throws FlooringPersistenceException, NoSuchOrderException;
}
