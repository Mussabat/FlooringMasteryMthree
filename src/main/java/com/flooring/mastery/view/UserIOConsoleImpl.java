package com.flooring.mastery.view;

import java.util.Scanner;

import org.springframework.stereotype.Component;

@Component
public class UserIOConsoleImpl implements UserIO {

    private final Scanner scanner = new Scanner(System.in);

    @Override
    public void print(String message) {
        System.out.println(message);
    }

    @Override
    public String readString(String prompt) {
        print(prompt);
        return scanner.nextLine();
    }

    // keep asking until user enters a valid integer

    @Override
    public int readInt(String prompt) {
        while (true) {
            String input = readString(prompt);
            try {
                return Integer.parseInt(input.trim());
            } catch (NumberFormatException e) {
                print("Please enter a whole number, for example 3.");
            }
        }

    }

    // keep asking until user enters a valid integer between min and max

    @Override
    public int readInt(String prompt, int min, int max) {
        while (true) {
            int input = readInt(prompt);
            if (input >= min && input <= max) {
                return input;
            } else {
                print("Please enter a whole number between " + min + " and " + max + ".");
            }
        }
    }

}
