package com.flooring.mastery.controller;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.flooring.mastery.view.FlooringView;

@Component
public class FlooringController {
    private final FlooringView view;


    // Constructor injection
    @Autowired
    public FlooringController(FlooringView view) {
        this.view = view;
    }

    public void run() {
        boolean keepRunning = true;

        while (keepRunning) {

            int menuSelection = view.displayMenuAndGetSelection();

            switch (menuSelection) {
                case 1:
                    // display orders
                    view.displayBanner("Display Orders");
                    view.displaySuccess("Coming soon...");
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

        }
        view.displayGoodbye();
    }

}
