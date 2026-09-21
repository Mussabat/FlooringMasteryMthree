package com.flooring.mastery.controller;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.flooring.mastery.dao.FlooringPersistenceException;
import com.flooring.mastery.model.Order;
import com.flooring.mastery.model.Product;
import com.flooring.mastery.model.Tax;
import com.flooring.mastery.service.FlooringService;
import com.flooring.mastery.service.NoSuchOrderException;
import com.flooring.mastery.service.OrderValidationException;
import com.flooring.mastery.view.FlooringView;

// The "conductor": runs the menu, asks the view for input and asks the service to do the work.
// It never prints by itself and never touches files.
@Component
public class FlooringController {
    private final FlooringView view;
    private final FlooringService service;

    // Constructor injection
    @Autowired
    public FlooringController(FlooringView view, FlooringService service) {
        this.view = view;
        this.service = service;
    }

    public void run() {
        boolean keepRunning = true;

        while (keepRunning) {

            int menuSelection = view.displayMenuAndGetSelection();

            // a file problem (FlooringPersistenceException) shows a message and returns to the menu
            try {
                switch (menuSelection) {
                    case 1:
                        displayOrders();
                        break;
                    case 2:
                        addOrder();
                        break;
                    case 3:
                        editOrder();
                        break;
                    case 4:
                        removeOrder();
                        break;
                    case 5:
                        exportAllData();
                        break;
                    case 6:
                        keepRunning = false;
                        break;
                    default:
                        view.displayUnknownCommand();
                }
            } catch (FlooringPersistenceException e) {
                view.displayErrorMessage(e.getMessage());
            }

        }
        view.displayGoodbye();
    }

     private void displayOrders() throws FlooringPersistenceException {
        view.displayBanner("Display Orders");
        LocalDate date = view.getOrderDate();
        try {
            List<Order> orders = service.getOrdersForDate(date);
            view.displayOrderList(date, orders);
        } catch (NoSuchOrderException e) {
            view.displayErrorMessage(e.getMessage());
        }
    }

    // asks for every field (repeating until the service accepts it), shows a summary
    // and saves the order only if the user confirms
    private void addOrder() throws FlooringPersistenceException {
        view.displayBanner("Add an Order");

        // load the states and products once, so the view can show them
        List<Tax> taxes = service.getAllTaxes();
        List<Product> products = service.getAllProducts();

        Order order = new Order();
        order.setOrderDate(askUntilValid(() -> service.validateOrderDate(view.getOrderDate())));
        order.setCustomerName(askUntilValid(() -> service.validateCustomerName(view.getCustomerName())));
        Tax tax = askUntilValid(() -> service.validateState(view.getState(taxes)));
        Product product = askUntilValid(() -> service.validateProductType(view.getProductType(products)));
        order.setArea(askUntilValid(() -> service.validateArea(view.getArea())));

        // fills in the prices and costs (the area must already be set)
        service.calculateOrder(order, tax, product);
        view.displayOrderSummary(order);

        if (view.confirm("Place order?")) {
            // the order number is given here, so a cancelled order does not use up a number
            Order savedOrder = service.addOrder(order);
            view.displaySuccess("Order #" + savedOrder.getOrderNumber() + " was placed.");
        } else {
            view.displaySuccess("The order was not placed.");
        }
    }

    // asks for a date and an order number and returns that order.
    // If it does not exist, shows the error message and returns null (the caller goes back to the menu).
    private Order findOrder() throws FlooringPersistenceException {
        LocalDate date = view.getOrderDate();
        int orderNumber = view.getOrderNumber();
        try {
            return service.getOrder(date, orderNumber);
        } catch (NoSuchOrderException e) {
            view.displayErrorMessage(e.getMessage());
            return null;
        }
    }

    // finds the order, lets the user change name / state / product / area (Enter keeps the current value)
    // and saves the changes only if the user confirms. The date can not be changed.
    private void editOrder() throws FlooringPersistenceException {
        view.displayBanner("Edit an Order");

        Order existing = findOrder();
        if (existing == null) {
            return;
        }

        // load the states and products once, so the view can show them
        List<Tax> taxes = service.getAllTaxes();
        List<Product> products = service.getAllProducts();

        // we change a COPY, so the saved order stays untouched unless the user confirms
        Order edited = new Order(existing);

        view.displayEditInstructions();
        edited.setCustomerName(askUntilValid(() ->
                service.validateCustomerName(view.getEditCustomerName(existing.getCustomerName()))));
        Tax tax = askUntilValid(() ->
                service.validateState(view.getEditState(existing.getState(), taxes)));
        Product product = askUntilValid(() ->
                service.validateProductType(view.getEditProductType(existing.getProductType(), products)));
        BigDecimal area = askUntilValid(() ->
                service.validateArea(view.getEditArea(existing.getArea())));

        // the prices only need to be recalculated if state, product or area changed
        // (compareTo, not equals: 249 and 249.00 are the same area)
        boolean priceChanged = !tax.getStateAbbreviation().equalsIgnoreCase(existing.getState())
                || !product.getProductType().equalsIgnoreCase(existing.getProductType())
                || area.compareTo(existing.getArea()) != 0;

        if (priceChanged) {
            edited.setArea(area);
            service.calculateOrder(edited, tax, product);
        }

        view.displayOrderSummary(edited);
        if (view.confirm("Save these changes?")) {
            try {
                service.editOrder(edited);
                view.displaySuccess("Order #" + edited.getOrderNumber() + " was updated.");
            } catch (NoSuchOrderException e) {
                // it existed a moment ago, so this should not happen; still, never crash
                view.displayErrorMessage(e.getMessage());
            }
        } else {
            view.displaySuccess("The changes were not saved.");
        }
    }

    // finds the order, shows it and removes it only if the user confirms
    private void removeOrder() throws FlooringPersistenceException {
        view.displayBanner("Remove an Order");

        Order order = findOrder();
        if (order == null) {
            return;
        }

        view.displayOrderSummary(order);
        if (view.confirm("Are you sure you want to remove this order?")) {
            try {
                service.removeOrder(order.getOrderDate(), order.getOrderNumber());
                view.displaySuccess("Order #" + order.getOrderNumber() + " was removed.");
            } catch (NoSuchOrderException e) {
                // it existed a moment ago, so this should not happen; still, never crash
                view.displayErrorMessage(e.getMessage());
            }
        } else {
            view.displaySuccess("The order was not removed.");
        }
    }

    // exports all orders of all days; a file problem goes up to run() (message, back to the menu)
    private void exportAllData() throws FlooringPersistenceException {
        view.displayBanner("Export All Data");
        service.exportAllData();
        view.displaySuccess("All orders were exported to Backup/DataExport.txt.");
    }

    // One piece of input that the service may reject. "throws" lets a lambda pass on both exceptions.
    @FunctionalInterface
    private interface ValidatedInput<T> {
        T get() throws OrderValidationException, FlooringPersistenceException;
    }

    // Runs the input again and again until the service accepts it, and returns the accepted value.
    // A broken business rule (OrderValidationException) shows its message and asks again.
    private <T> T askUntilValid(ValidatedInput<T> input) throws FlooringPersistenceException {
        while (true) {
            try {
                return input.get();
            } catch (OrderValidationException e) {
                view.displayErrorMessage(e.getMessage());
            }
        }
    }

}
