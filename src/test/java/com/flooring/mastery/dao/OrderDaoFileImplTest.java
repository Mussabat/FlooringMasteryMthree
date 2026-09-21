package com.flooring.mastery.dao;

import com.flooring.mastery.model.Order;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.io.File;
import java.io.IOException;
import java.math.BigDecimal;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDate;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

// Stateful tests for OrderDaoFileImpl: they use real files in TestData/, never the real Orders/ folder.
public class OrderDaoFileImplTest {

    private static final String TEST_FOLDER = "TestData";
    private static final String TEST_ORDERS_FOLDER = "TestData/Orders";
    private static final String TEST_EXPORT_FILE = "TestData/Backup/DataExport.txt";

    private static final LocalDate DATE_ONE = LocalDate.of(2030, 1, 15);
    private static final LocalDate DATE_TWO = LocalDate.of(2030, 2, 1);

    private OrderDao orderDao;

    // runs before EVERY test: deletes the whole TestData folder, so no old file can affect a test
    @BeforeEach
    void setUp() throws IOException {
        deleteFolder(Paths.get(TEST_FOLDER));

        orderDao = new OrderDaoFileImpl(TEST_ORDERS_FOLDER, TEST_EXPORT_FILE);
    }

    @Test
    void addThenGetReturnsEqualOrder() throws Exception {
        Order order = buildOrder(1, DATE_ONE, "Ada Lovelace");

        orderDao.addOrder(order);
        Order loaded = orderDao.getOrder(DATE_ONE, 1);

        assertNotNull(loaded);
        assertEquals(order, loaded);
    }

    @Test
    void addOrderCreatesFileNamedAfterTheDate() throws Exception {
        orderDao.addOrder(buildOrder(1, DATE_ONE, "Ada Lovelace"));

        assertTrue(new File(TEST_ORDERS_FOLDER, "Orders_01152030.txt").exists());
    }

    // "Acme, Inc." has a comma inside the name: it must come back in one piece
    @Test
    void customerNameWithCommaSurvivesSaveAndLoad() throws Exception {
        orderDao.addOrder(buildOrder(1, DATE_ONE, "Acme, Inc."));

        Order loaded = orderDao.getOrder(DATE_ONE, 1);

        assertNotNull(loaded);
        assertEquals("Acme, Inc.", loaded.getCustomerName());
    }

    // 3 orders saved (not in number order): only the 2 of DATE_ONE come back, sorted by order number
    @Test
    void getOrdersForDateReturnsOnlyThatDaySortedByNumber() throws Exception {
        orderDao.addOrder(buildOrder(5, DATE_ONE, "Second"));
        orderDao.addOrder(buildOrder(9, DATE_TWO, "Other day"));
        orderDao.addOrder(buildOrder(2, DATE_ONE, "First"));

        List<Order> orders = orderDao.getOrdersForDate(DATE_ONE);

        assertEquals(2, orders.size());
        assertEquals(2, orders.get(0).getOrderNumber());
        assertEquals(5, orders.get(1).getOrderNumber());
    }

    @Test
    void getOrdersForDateWithNoFileReturnsEmptyList() throws Exception {
        List<Order> orders = orderDao.getOrdersForDate(DATE_ONE);

        assertTrue(orders.isEmpty());
    }

    @Test
    void getOrderReturnsNullWhenNotFound() throws Exception {
        orderDao.addOrder(buildOrder(1, DATE_ONE, "Ada Lovelace"));

        assertNull(orderDao.getOrder(DATE_ONE, 99));
    }

    // order numbers are unique across ALL days, so the DAO must look at every file
    @Test
    void highestOrderNumberLooksAtAllDates() throws Exception {
        orderDao.addOrder(buildOrder(2, DATE_ONE, "Order two"));
        orderDao.addOrder(buildOrder(7, DATE_TWO, "Order seven"));
        orderDao.addOrder(buildOrder(4, DATE_ONE, "Order four"));

        assertEquals(7, orderDao.getHighestOrderNumber());
    }

    @Test
    void highestOrderNumberIsZeroWhenThereAreNoOrders() throws Exception {
        assertEquals(0, orderDao.getHighestOrderNumber());
    }

    // editing changes the saved name, and the number of orders of that day stays the same
    @Test
    void editOrderChangesTheNameAndKeepsTheCount() throws Exception {
        orderDao.addOrder(buildOrder(1, DATE_ONE, "Old name"));
        orderDao.addOrder(buildOrder(2, DATE_ONE, "Other order"));

        Order edited = new Order(orderDao.getOrder(DATE_ONE, 1));
        edited.setCustomerName("New name");
        orderDao.editOrder(edited);

        assertEquals("New name", orderDao.getOrder(DATE_ONE, 1).getCustomerName());
        assertEquals("Other order", orderDao.getOrder(DATE_ONE, 2).getCustomerName());
        assertEquals(2, orderDao.getOrdersForDate(DATE_ONE).size());
    }

    // two orders on one day: removing one leaves the other, and the removed one is returned
    @Test
    void removeOrderRemovesOnlyThatOrder() throws Exception {
        orderDao.addOrder(buildOrder(1, DATE_ONE, "Keep me"));
        orderDao.addOrder(buildOrder(2, DATE_ONE, "Remove me"));

        Order removed = orderDao.removeOrder(DATE_ONE, 2);

        assertNotNull(removed);
        assertEquals("Remove me", removed.getCustomerName());
        assertEquals(1, orderDao.getOrdersForDate(DATE_ONE).size());
        assertNull(orderDao.getOrder(DATE_ONE, 2));
    }

    // a day without orders must have no file, so Display Orders says "no orders"
    @Test
    void removingTheLastOrderDeletesTheDayFile() throws Exception {
        orderDao.addOrder(buildOrder(1, DATE_ONE, "Only order"));

        orderDao.removeOrder(DATE_ONE, 1);

        assertFalse(new File(TEST_ORDERS_FOLDER, "Orders_01152030.txt").exists());
    }

    @Test
    void removeMissingOrderReturnsNullAndChangesNothing() throws Exception {
        orderDao.addOrder(buildOrder(1, DATE_ONE, "Ada Lovelace"));

        assertNull(orderDao.removeOrder(DATE_ONE, 99));
        assertEquals(1, orderDao.getOrdersForDate(DATE_ONE).size());
    }

    // 2 orders on 2 different days, exported TWICE: the file must hold the header + 2 lines (no duplicates),
    // every line has the date at the end, and the oldest day comes first
    @Test
    void exportAddsTheDateAndOverwritesTheOldExport() throws Exception {
        orderDao.addOrder(buildOrder(2, DATE_TWO, "Doctor Who"));
        orderDao.addOrder(buildOrder(1, DATE_ONE, "Ada Lovelace"));

        orderDao.exportAllData();
        orderDao.exportAllData();

        List<String> lines = Files.readAllLines(Paths.get(TEST_EXPORT_FILE));
        assertEquals(3, lines.size());
        assertTrue(lines.get(0).endsWith(",OrderDate"));
        assertTrue(lines.get(1).startsWith("1,Ada Lovelace,"));
        assertTrue(lines.get(1).endsWith(",01-15-2030"));
        assertTrue(lines.get(2).startsWith("2,Doctor Who,"));
        assertTrue(lines.get(2).endsWith(",02-01-2030"));
    }

    // builds an order with the Ada Lovelace sample values; only number, date and name change
    private Order buildOrder(int orderNumber, LocalDate date, String customerName) {
        Order order = new Order();
        order.setOrderNumber(orderNumber);
        order.setOrderDate(date);
        order.setCustomerName(customerName);
        order.setState("CA");
        order.setTaxRate(new BigDecimal("25.00"));
        order.setProductType("Tile");
        order.setArea(new BigDecimal("249.00"));
        order.setCostPerSquareFoot(new BigDecimal("3.50"));
        order.setLaborCostPerSquareFoot(new BigDecimal("4.15"));
        order.setMaterialCost(new BigDecimal("871.50"));
        order.setLaborCost(new BigDecimal("1033.35"));
        order.setTax(new BigDecimal("476.21"));
        order.setTotal(new BigDecimal("2381.06"));
        return order;
    }

    // deletes a folder with everything inside it (files first, the folder itself last).
    // If the folder does not exist there is nothing to delete.
    private void deleteFolder(Path folder) throws IOException {
        if (!Files.exists(folder)) {
            return;
        }
        try (Stream<Path> paths = Files.walk(folder)) {
            // reverse order: the contents of a folder come before the folder itself
            for (Path path : (Iterable<Path>) paths.sorted(Comparator.reverseOrder())::iterator) {
                Files.delete(path);
            }
        }
    }
}
