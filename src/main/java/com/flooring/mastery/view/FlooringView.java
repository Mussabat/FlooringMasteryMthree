package com.flooring.mastery.view;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.flooring.mastery.model.Order;
import com.flooring.mastery.model.Product;
import com.flooring.mastery.model.Tax;

// Everything the user sees on screen goes through this class.
// It never prints by itself. It always asks UserIO to do the raw input/output.
@Component
public class FlooringView {

    // date shown to the user in the table title
    private static final DateTimeFormatter DISPLAY_DATE_FORMAT = DateTimeFormatter.ofPattern("MM/dd/yyyy");

    // one row of the order table: left-aligned text columns, right-aligned number columns
    private static final String ROW_FORMAT = "%-4s %-22s %-5s %-10s %10s %10s %10s %10s %10s";

    // one row of the product list: product name on the left, the two prices on the right
    private static final String PRODUCT_ROW_FORMAT = "  %-10s %10s %10s";

    private final UserIO io;

    // Spring gives us a UserIO here (constructor injection)
    @Autowired
    public FlooringView(UserIO io) {
        this.io = io;
    }

    // prints the main menu and returns the number (1-6) the user picked
    public int displayMenuAndGetSelection() {
        io.print("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");
        io.print("* <<Flooring Program>>");
        io.print("* 1. Display Orders");
        io.print("* 2. Add an Order");
        io.print("* 3. Edit an Order");
        io.print("* 4. Remove an Order");
        io.print("* 5. Export All Data");
        io.print("* 6. Quit");
        io.print("*");
        io.print("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");

        return io.readInt("Please select from the above choices (1-6):", 1, 6);
    }

    // shown for a menu choice the program does not know
    public void displayUnknownCommand() {
        io.print("Unknown command.");
    }

    public void displayGoodbye() {
        io.print("Goodbye!");
    }

    // prints a title line so the user can see which feature they are in
    public void displayBanner(String title) {
        io.print("=== " + title + " ===");
    }

    public void displayErrorMessage(String message) {
        io.print("ERROR: " + message);
    }

    public void displaySuccess(String message) {
        io.print(message);
    }

    // asks the user for a date and returns it (UserIO already made sure the format is right)
    public LocalDate getOrderDate() {
        return io.readDate("Please enter the order date (MM/DD/YYYY):");
    }

    // prints all orders of one date as a table: a title, a header row, then one row per order
    public void displayOrderList(LocalDate date, List<Order> orders) {
        io.print("Orders for " + date.format(DISPLAY_DATE_FORMAT) + ":");
        io.print(String.format(ROW_FORMAT,
                "No.", "Customer", "State", "Product", "Area", "Material", "Labor", "Tax", "Total"));
        orders.forEach(order -> io.print(String.format(ROW_FORMAT,
                order.getOrderNumber(),
                order.getCustomerName(),
                order.getState(),
                order.getProductType(),
                order.getArea().toPlainString(),
                order.getMaterialCost().toPlainString(),
                order.getLaborCost().toPlainString(),
                order.getTax().toPlainString(),
                order.getTotal().toPlainString())));
    }

    // asks for the customer name (the service checks the rules)
    public String getCustomerName() {
        return io.readString("Please enter the customer name:");
    }

    // prints the states we sell in, then asks for one
    public String getState(List<Tax> taxes) {
        io.print("We sell in these states:");
        taxes.forEach(tax -> io.print("  " + tax.getStateAbbreviation() + " - " + tax.getStateName()));
        return io.readString("Please enter the state abbreviation:");
    }

    // prints the products with their prices, then asks for one
    public String getProductType(List<Product> products) {
        io.print("We sell these products (prices per square foot):");
        io.print(String.format(PRODUCT_ROW_FORMAT, "Product", "Material", "Labor"));
        products.forEach(product -> io.print(String.format(PRODUCT_ROW_FORMAT,
                product.getProductType(),
                product.getCostPerSquareFoot().toPlainString(),
                product.getLaborCostPerSquareFoot().toPlainString())));
        return io.readString("Please enter the product type:");
    }

    // asks for the area in square feet (UserIO makes sure it is a number)
    public BigDecimal getArea() {
        return io.readBigDecimal("Please enter the area in square feet (minimum 100):");
    }

    // prints every detail of one order; the order number is only shown once the order has one (> 0)
    public void displayOrderSummary(Order order) {
        io.print("=== Order Summary ===");
        if (order.getOrderNumber() > 0) {
            io.print("Order number:   " + order.getOrderNumber());
        }
        io.print("Order date:     " + order.getOrderDate().format(DISPLAY_DATE_FORMAT));
        io.print("Customer:       " + order.getCustomerName());
        io.print("State:          " + order.getState());
        io.print("Tax rate:       " + order.getTaxRate().toPlainString() + "%");
        io.print("Product:        " + order.getProductType());
        io.print("Area:           " + order.getArea().toPlainString() + " sq ft");
        io.print("Material/sq ft: " + order.getCostPerSquareFoot().toPlainString());
        io.print("Labor/sq ft:    " + order.getLaborCostPerSquareFoot().toPlainString());
        io.print("Material cost:  " + order.getMaterialCost().toPlainString());
        io.print("Labor cost:     " + order.getLaborCost().toPlainString());
        io.print("Tax:            " + order.getTax().toPlainString());
        io.print("Total:          " + order.getTotal().toPlainString());
    }

    // asks a yes/no question and returns true for Y
    public boolean confirm(String question) {
        return io.readYesNo(question + " (Y/N)");
    }
}
