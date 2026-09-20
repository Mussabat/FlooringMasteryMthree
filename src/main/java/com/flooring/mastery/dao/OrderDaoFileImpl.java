package com.flooring.mastery.dao;

import com.flooring.mastery.model.Order;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import java.util.stream.Collectors;

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
    private static final String ORDER_FILE_NAME_PATTERN = "Orders_[0-9]{8}[.]txt";

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

    // saves a new order into the file of its date: load the day, add the order, write the day back
    @Override
    public Order addOrder(Order order) throws FlooringPersistenceException {
        Map<Integer, Order> orders = loadOrders(order.getOrderDate());
        orders.put(order.getOrderNumber(), order);
        writeOrders(order.getOrderDate(), orders);
        return order;
    }

    @Override
    public Order editOrder(Order order) throws FlooringPersistenceException {
        throw new UnsupportedOperationException("");
    }

    // removes one order from its day: load the day, remove the order, write the day back.
    // Returns the removed order, or null if there was no such order (then nothing is written).
    // If it was the last order of the day, writeOrders deletes the day's file.
    @Override
    public Order removeOrder(LocalDate date, int orderNumber) throws FlooringPersistenceException {
        Map<Integer, Order> orders = loadOrders(date);
        Order removedOrder = orders.remove(orderNumber);

        if (removedOrder != null) {
            writeOrders(date, orders);
        }
        return removedOrder;
    }

    // looks through the order files of ALL days and returns the biggest order number (0 if there are no orders)
    @Override
    public int getHighestOrderNumber() throws FlooringPersistenceException {
        int highest = 0;
        // a normal for loop (not a lambda): loadOrders throws a checked exception, which a lambda cannot pass on
        for (LocalDate date : getAllOrderDates()) {
            int highestOfDay = loadOrders(date).keySet().stream()
                    .mapToInt(Integer::intValue)
                    .max()
                    .orElse(0);
            highest = Math.max(highest, highestOfDay);
        }
        return highest;
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

    // returns the dates of all days that have an order file, oldest first.
    // Only files named Orders_MMDDYYYY.txt count. No folder yet -> empty list.
    private List<LocalDate> getAllOrderDates() throws FlooringPersistenceException {
        File[] files = new File(ordersFolder).listFiles();
        if (files == null) {
            return new ArrayList<>(); // listFiles() gives null when the folder does not exist
        }

        try {
            return Arrays.stream(files)
                    .map(File::getName)
                    .filter(name -> name.matches(ORDER_FILE_NAME_PATTERN))
                    // "Orders_06012013.txt": the 8 date digits are at positions 7 to 15
                    .map(name -> LocalDate.parse(name.substring(7, 15), FILE_DATE_FORMAT))
                    .sorted()
                    .collect(Collectors.toList());
        } catch (DateTimeParseException e) {
            // 8 digits that are not a real date, e.g. Orders_13452013.txt
            throw new FlooringPersistenceException("Bad order file name in the " + ordersFolder + " folder.", e);
        }
    }

    // saves all orders of one day into that day's file (this replaces the old content of the file).
    // No orders left -> delete the file, so a day without orders has no file.
    private void writeOrders(LocalDate date, Map<Integer, Order> orders) throws FlooringPersistenceException {
        File orderFile = getOrderFile(date);

        if (orders.isEmpty()) {
            if (orderFile.exists() && !orderFile.delete()) {
                throw new FlooringPersistenceException("Could not delete the empty order file.");
            }
            return;
        }

        orderFile.getParentFile().mkdirs(); // create the Orders folder if it does not exist yet

        // FileWriter without "true" = overwrite. try-with-resources closes the file for us.
        try (PrintWriter writer = new PrintWriter(new FileWriter(orderFile))) {
            writer.println(HEADER);
            for (Order order : orders.values()) {
                writer.println(marshallOrder(order));
            }
        } catch (IOException e) {
            throw new FlooringPersistenceException("Could not save order data.", e);
        }
    }

    // turns an Order into one text line (the opposite of unmarshallOrder).
    private String marshallOrder(Order order) {
        return String.join(DELIMITER,
                String.valueOf(order.getOrderNumber()),
                order.getCustomerName(),
                order.getState(),
                order.getTaxRate().toPlainString(),
                order.getProductType(),
                order.getArea().toPlainString(),
                order.getCostPerSquareFoot().toPlainString(),
                order.getLaborCostPerSquareFoot().toPlainString(),
                order.getMaterialCost().toPlainString(),
                order.getLaborCost().toPlainString(),
                order.getTax().toPlainString(),
                order.getTotal().toPlainString());
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
