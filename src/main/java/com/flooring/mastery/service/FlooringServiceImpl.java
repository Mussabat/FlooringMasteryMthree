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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

// Business logic of the program. It asks the DAOs for data and decides what "not found" means.
// It has no file code and never prints anything.
@Service
public class FlooringServiceImpl implements FlooringService {

    private static final DateTimeFormatter MESSAGE_DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");
    private static final String CUSTOMER_NAME_PATTERN = "[a-zA-Z0-9., ]+";
    private static final BigDecimal MINIMUM_AREA = new BigDecimal("100");
    private static final BigDecimal ONE_HUNDRED = new BigDecimal("100");

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

    // today is NOT allowed, only dates after today
    @Override
    public LocalDate validateOrderDate(LocalDate date) throws OrderValidationException {
        if (date == null || !date.isAfter(LocalDate.now())) {
            throw new OrderValidationException("Order date must be in the future.");
        }
        return date;
    }

    // trim first, so "   " counts as blank; then check the allowed characters with the regex
    @Override
    public String validateCustomerName(String customerName) throws OrderValidationException {
        String trimmedName = (customerName == null) ? "" : customerName.trim();

        if (trimmedName.isEmpty()) {
            throw new OrderValidationException("Customer name may not be blank.");
        }
        if (!trimmedName.matches(CUSTOMER_NAME_PATTERN)) {
            throw new OrderValidationException(
                    "Customer name may only contain letters, numbers, spaces, periods and commas.");
        }
        return trimmedName;
    }

    // the DAO returns null when the state is not in Taxes.txt; here that becomes an exception
    @Override
    public Tax validateState(String state) throws FlooringPersistenceException, OrderValidationException {
        Tax tax = taxDao.getTax(state);

        if (tax == null) {
            throw new OrderValidationException("Sorry, we do not sell in " + state + ".");
        }
        return tax;
    }

    // the DAO returns null when the product is not in Products.txt; here that becomes an exception
    @Override
    public Product validateProductType(String productType)
            throws FlooringPersistenceException, OrderValidationException {
        Product product = productDao.getProduct(productType);

        if (product == null) {
            throw new OrderValidationException(productType + " is not an available product.");
        }
        return product;
    }

    // compareTo (not equals) ignores the scale: 100 and 100.00 count as the same number
    @Override
    public BigDecimal validateArea(BigDecimal area) throws OrderValidationException {
        if (area == null || area.compareTo(MINIMUM_AREA) < 0) {
            throw new OrderValidationException("Area must be at least 100 sq ft.");
        }
        return area.setScale(2, RoundingMode.HALF_UP);
    }

    // copies the state, tax rate, product and prices onto the order, then does the math:
    // material = area x cost, labor = area x labor cost, tax = (material + labor) x rate / 100
    @Override
    public Order calculateOrder(Order order, Tax tax, Product product) {
        order.setState(tax.getStateAbbreviation());
        order.setTaxRate(tax.getTaxRate());
        order.setProductType(product.getProductType());
        order.setCostPerSquareFoot(product.getCostPerSquareFoot());
        order.setLaborCostPerSquareFoot(product.getLaborCostPerSquareFoot());

        BigDecimal materialCost = order.getArea().multiply(product.getCostPerSquareFoot())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal laborCost = order.getArea().multiply(product.getLaborCostPerSquareFoot())
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal taxAmount = materialCost.add(laborCost)
                .multiply(tax.getTaxRate().divide(ONE_HUNDRED))
                .setScale(2, RoundingMode.HALF_UP);
        BigDecimal total = materialCost.add(laborCost).add(taxAmount);

        order.setMaterialCost(materialCost);
        order.setLaborCost(laborCost);
        order.setTax(taxAmount);
        order.setTotal(total);
        return order;
    }

    // the number is assigned only now (when the order is saved), so cancelled orders don't waste numbers
    @Override
    public Order addOrder(Order order) throws FlooringPersistenceException {
        order.setOrderNumber(orderDao.getHighestOrderNumber() + 1);
        return orderDao.addOrder(order);
    }

    // getOrder throws NoSuchOrderException when the order is missing, so we only reach the DAO for an existing order
    @Override
    public Order editOrder(Order order) throws FlooringPersistenceException, NoSuchOrderException {
        getOrder(order.getOrderDate(), order.getOrderNumber());
        return orderDao.editOrder(order);
    }

    // same idea as editOrder: check that the order exists first, then let the DAO remove it
    @Override
    public Order removeOrder(LocalDate date, int orderNumber) throws FlooringPersistenceException, NoSuchOrderException {
        getOrder(date, orderNumber);
        return orderDao.removeOrder(date, orderNumber);
    }
}
