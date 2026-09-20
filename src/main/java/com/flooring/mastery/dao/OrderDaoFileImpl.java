package com.flooring.mastery.dao;

import com.flooring.mastery.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

// Reads and writes orders in text files: one file per day, named Orders_MMDDYYYY.txt.
// The date is NOT inside a line, it comes from the file name.
@Repository
public class OrderDaoFileImpl implements OrderDao {

    private static final String DEFAULT_ORDERS_FOLDER = "Orders";
    private static final String DEFAULT_EXPORT_FILE = "Backup/DataExport.txt";
    private static final String HEADER = "OrderNumber,CustomerName,State,TaxRate,ProductType,Area,"
            + "CostPerSquareFoot,LaborCostPerSquareFoot,MaterialCost,LaborCost,Tax,Total";
    private static final String DELIMITER = ",";
    private static final DateTimeFormatter FILE_DATE_FORMAT = DateTimeFormatter.ofPattern("MMddyyyy");

    // a line has: 1 order number + at least 1 name token + 10 fixed columns after the name
    private static final int FIXED_COLUMNS_AFTER_NAME = 10;
    private static final int MINIMUM_TOKENS = 1 + 1 + FIXED_COLUMNS_AFTER_NAME;

    private final String ordersFolder;
    private final String exportFile;

    // Spring uses this one: it points at the real folders
    @Autowired
    public OrderDaoFileImpl() {
        this(DEFAULT_ORDERS_FOLDER, DEFAULT_EXPORT_FILE);
    }

    // tests use this one: they point at folders in TestData/
    public OrderDaoFileImpl(String ordersFolder, String exportFile) {
        this.ordersFolder = ordersFolder;
        this.exportFile = exportFile;
    }

    // returns every order of this date sorted by order number, or an empty list if there are none
    @Override
    public List<Order> getOrdersForDate(LocalDate date) throws FlooringPersistenceException {
        return new ArrayList<>(loadOrders(date).values());
    }

    // returns the order with this number on this date, or null if there is none
    @Override
    public Order getOrder(LocalDate date, int orderNumber) throws FlooringPersistenceException {
        return loadOrders(date).get(orderNumber);
    }

    @Override
    public Order addOrder(Order order) throws FlooringPersistenceException {
        throw new UnsupportedOperationException("");
    }

    @Override
    public Order editOrder(Order order) throws FlooringPersistenceException {
        throw new UnsupportedOperationException("");
    }

    @Override
    public Order removeOrder(LocalDate date, int orderNumber) throws FlooringPersistenceException {
        throw new UnsupportedOperationException("");
    }

    @Override
    public int getHighestOrderNumber() throws FlooringPersistenceException {
        throw new UnsupportedOperationException("");
    }

    @Override
    public void exportAllData() throws FlooringPersistenceException {
        throw new UnsupportedOperationException("");
    }

    // builds the file for one day, e.g. 2013-06-01 -> Orders/Orders_06012013.txt
    private File getOrderFile(LocalDate date) {
        return new File(ordersFolder, "Orders_" + date.format(FILE_DATE_FORMAT) + ".txt");
    }

    // reads all orders of one day into a map (order number -> order).
    // A TreeMap keeps the orders sorted by order number.
    // If the day has no file, that is normal (no orders that day): return an empty map.
    private Map<Integer, Order> loadOrders(LocalDate date) throws FlooringPersistenceException {
        Map<Integer, Order> orders = new TreeMap<>();
        File orderFile = getOrderFile(date);

        if (!orderFile.exists()) {
            return orders;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(orderFile))) {
            reader.readLine(); // skip the header row

            String line;
            while ((line = reader.readLine()) != null) {
                if (line.isBlank()) {
                    continue; // ignore empty lines
                }
                Order order = unmarshallOrder(line, date);
                orders.put(order.getOrderNumber(), order);
            }
        } catch (IOException e) {
            throw new FlooringPersistenceException("Could not load order data.", e);
        }

        return orders;
    }

    // turns one text line into an Order object.
    // The customer name may contain commas ("Acme, Inc."), so we cannot count columns from the left.
    // Instead: first token = order number, LAST 10 tokens = fixed columns,
    // everything in between = the name (joined back together with commas).
    private Order unmarshallOrder(String line, LocalDate date) throws FlooringPersistenceException {
        String[] tokens = line.split(DELIMITER);
        int count = tokens.length;

        if (count < MINIMUM_TOKENS) {
            throw new FlooringPersistenceException("Bad order data in line: " + line);
        }

        try {
            Order order = new Order();
            order.setOrderNumber(Integer.parseInt(tokens[0].trim()));
            order.setCustomerName(String.join(DELIMITER, Arrays.copyOfRange(tokens, 1, count - FIXED_COLUMNS_AFTER_NAME)));
            order.setState(tokens[count - 10].trim());
            order.setTaxRate(new BigDecimal(tokens[count - 9].trim()));
            order.setProductType(tokens[count - 8].trim());
            order.setArea(new BigDecimal(tokens[count - 7].trim()));
            order.setCostPerSquareFoot(new BigDecimal(tokens[count - 6].trim()));
            order.setLaborCostPerSquareFoot(new BigDecimal(tokens[count - 5].trim()));
            order.setMaterialCost(new BigDecimal(tokens[count - 4].trim()));
            order.setLaborCost(new BigDecimal(tokens[count - 3].trim()));
            order.setTax(new BigDecimal(tokens[count - 2].trim()));
            order.setTotal(new BigDecimal(tokens[count - 1].trim()));
            order.setOrderDate(date); // the date is not in the line, it comes from the file name
            return order;
        } catch (NumberFormatException e) {
            // a bad number in the order number or in one of the money/area columns
            throw new FlooringPersistenceException("Bad order data in line: " + line, e);
        }
    }
}
