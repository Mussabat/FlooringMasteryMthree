package com.flooring.mastery.controller;

import java.time.LocalDate;
import java.util.List;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.flooring.mastery.dao.FlooringPersistenceException;
import com.flooring.mastery.model.Order;
import com.flooring.mastery.service.FlooringService;
import com.flooring.mastery.service.NoSuchOrderException;
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
                        // add an order
                        view.displayBanner("Add an Order");
                        view.displaySuccess("Coming soon...");
                        break;
                    case 3:
                        // edit an order
                        view.displayBanner("Edit an Order");
                        view.displaySuccess("Coming soon...");
                        break;
                    case 4:
                        // remove an order
                        view.displayBanner("Remove an Order");
                        view.displaySuccess("Coming soon...");
                        break;
                    case 5:
                        // export all  data
                        view.displayBanner("Export All Data");
                        view.displaySuccess("Coming soon...");
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

    // asks for a date and shows that day's orders; no orders -> error message, back to the menu
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

}
