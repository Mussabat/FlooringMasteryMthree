package com.flooring.mastery.view;

import java.math.BigDecimal;
import java.time.LocalDate;

public interface UserIO {
    // shows a message on screen
    void print(String message);

    // shows the prompt and returns whatever text the user types
    String readString(String prompt);

    // shows the prompt and returns the whole number

    int readInt(String prompt);

    // shows the prompt and returns the whole number between min and max
    int readInt(String prompt, int min, int max);

    // shows the prompt and returns the date typed as MM/DD/YYYY
    LocalDate readDate(String prompt);

    // shows the prompt and returns the number typed (decimals allowed, for example 249.5)
    BigDecimal readBigDecimal(String prompt);

    // same as above, but pressing Enter (blank input) returns defaultValue
    BigDecimal readBigDecimal(String prompt, BigDecimal defaultValue);

    // shows the prompt and returns true for Y and false for N
    boolean readYesNo(String prompt);

}

