package com.flooring.mastery.view;

    public interface UserIO {
        // shows a message on screen
        void print(String message);

        // shows the prompt and returns whatever text the user types
        String readString(String prompt);

        // shows the prompt and returns the whole number

        int readInt(String prompt);

        // shows the prompt and returns the whole number between min and max
        int readInt(String prompt, int min, int max);


    }

