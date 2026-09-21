package com.flooring.mastery.service;

import com.flooring.mastery.dao.OrderDao;
import com.flooring.mastery.model.Order;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

// A fake OrderDao for service tests. It holds ONE known order in memory (no files),
// so every test starts from exactly the same data.
public class OrderDaoStubImpl implements OrderDao {

    // the known order: number 4, date 2013-06-01, Ada Lovelace sample values
    public static final LocalDate KNOWN_DATE = LocalDate.of(2013, 6, 1);
    public static final int KNOWN_ORDER_NUMBER = 4;

    private final Order knownOrder;

    public OrderDaoStubImpl() {
        knownOrder = new Order();
        knownOrder.setOrderNumber(KNOWN_ORDER_NUMBER);
        knownOrder.setOrderDate(KNOWN_DATE);
        knownOrder.setCustomerName("Ada Lovelace");
        knownOrder.setState("CA");
        knownOrder.setTaxRate(new BigDecimal("25.00"));
        knownOrder.setProductType("Tile");
        knownOrder.setArea(new BigDecimal("249.00"));
        knownOrder.setCostPerSquareFoot(new BigDecimal("3.50"));
        knownOrder.setLaborCostPerSquareFoot(new BigDecimal("4.15"));
        knownOrder.setMaterialCost(new BigDecimal("871.50"));
        knownOrder.setLaborCost(new BigDecimal("1033.35"));
        knownOrder.setTax(new BigDecimal("476.21"));
        knownOrder.setTotal(new BigDecimal("2381.06"));
    }

    // only the known date has an order; every other date gives an empty list
    @Override
    public List<Order> getOrdersForDate(LocalDate date) {
        List<Order> orders = new ArrayList<>();
        if (date.equals(KNOWN_DATE)) {
            orders.add(knownOrder);
        }
        return orders;
    }

    // only the known date + known number match; everything else is "not found" (null)
    @Override
    public Order getOrder(LocalDate date, int orderNumber) {
        if (date.equals(KNOWN_DATE) && orderNumber == KNOWN_ORDER_NUMBER) {
            return knownOrder;
        }
        return null;
    }

    // the stub does not store anything: it just gives the order back
    @Override
    public Order addOrder(Order order) {
        return order;
    }

    // the stub does not store anything: it just gives the order back
    @Override
    public Order editOrder(Order order) {
        return order;
    }

    // same rule as getOrder: only the known order "exists", so only it can be removed
    @Override
    public Order removeOrder(LocalDate date, int orderNumber) {
        return getOrder(date, orderNumber);
    }

    // the highest number in use is the known order's number
    @Override
    public int getHighestOrderNumber() {
        return KNOWN_ORDER_NUMBER;
    }

    // nothing to export in the stub
    @Override
    public void exportAllData() {
    }
}
