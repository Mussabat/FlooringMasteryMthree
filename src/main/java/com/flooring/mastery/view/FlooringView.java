package com.flooring.mastery.view;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

// Everything the user sees on screen goes through this class.
// It never prints by itself. It always asks UserIO to do the raw input/output.
@Component
public class FlooringView {

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
}
