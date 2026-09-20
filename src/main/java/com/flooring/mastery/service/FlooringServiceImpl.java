package com.flooring.mastery.service;

import com.flooring.mastery.dao.FlooringPersistenceException;
import com.flooring.mastery.dao.OrderDao;
import com.flooring.mastery.dao.ProductDao;
import com.flooring.mastery.dao.TaxDao;
import com.flooring.mastery.model.Order;
import com.flooring.mastery.model.Product;
import com.flooring.mastery.model.Tax;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Business logic of the program. It asks the DAOs for data and decides what "not found" means.
// It has no file code and never prints anything.
@Service
public class FlooringServiceImpl implements FlooringService {

    private static final DateTimeFormatter MESSAGE_DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    private final OrderDao orderDao;
    private final ProductDao productDao;
    private final TaxDao taxDao;

    // Spring gives us the three DAOs (interfaces), so tests can pass stubs instead
    @Autowired
    public FlooringServiceImpl(OrderDao orderDao, ProductDao productDao, TaxDao taxDao) {
        this.orderDao = orderDao;
        this.productDao = productDao;
        this.taxDao = taxDao;
    }

    // the DAO returns an empty list when there is no file; here that becomes an exception
    @Override
    public List<Order> getOrdersForDate(LocalDate date) throws FlooringPersistenceException, NoSuchOrderException {
        List<Order> orders = orderDao.getOrdersForDate(date);

        if (orders.isEmpty()) {
            throw new NoSuchOrderException("No orders exist for " + date.format(MESSAGE_DATE_FORMAT) + ".");
        }
        return orders;
    }

    // the DAO returns null when the order is missing; here that becomes an exception
    @Override
    public Order getOrder(LocalDate date, int orderNumber) throws FlooringPersistenceException, NoSuchOrderException {
        Order order = orderDao.getOrder(date, orderNumber);

        if (order == null) {
            throw new NoSuchOrderException("Order " + orderNumber + " does not exist for "
                    + date.format(MESSAGE_DATE_FORMAT) + ".");
        }
        return order;
    }

    // no rule to apply, so we just pass the call through to the DAO
    @Override
    public List<Product> getAllProducts() throws FlooringPersistenceException {
        return productDao.getAllProducts();
    }

    // no rule to apply, so we just pass the call through to the DAO
    @Override
    public List<Tax> getAllTaxes() throws FlooringPersistenceException {
        return taxDao.getAllTaxes();
    }
}
