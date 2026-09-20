package com.flooring.mastery.view;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.time.format.ResolverStyle;
import java.util.Scanner;

import org.springframework.stereotype.Component;

@Component
public class UserIOConsoleImpl implements UserIO {

    // "uuuu" + STRICT rejects impossible dates such as 02/30/2027 (instead of moving them to a valid day)
    private static final DateTimeFormatter DATE_FORMAT =
            DateTimeFormatter.ofPattern("MM/dd/uuuu").withResolverStyle(ResolverStyle.STRICT);

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

    // keep asking until user enters a real date in the format MM/DD/YYYY
    // (this only checks the FORMAT; rules like "must be in the future" belong to the service)
    @Override
    public LocalDate readDate(String prompt) {
        while (true) {
            String input = readString(prompt);
            try {
                return LocalDate.parse(input.trim(), DATE_FORMAT);
            } catch (DateTimeParseException e) {
                print("Please enter a real date as MM/DD/YYYY, for example 06/01/2013.");
            }
        }
    }

}
