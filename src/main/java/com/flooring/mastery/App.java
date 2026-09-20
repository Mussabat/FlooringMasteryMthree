package com.flooring.mastery;

import org.springframework.context.annotation.AnnotationConfigApplicationContext;

import com.flooring.mastery.controller.FlooringController;

public class App {

    public static void main(String[] args) {
        AnnotationConfigApplicationContext appContext = new AnnotationConfigApplicationContext();
        appContext.scan("com.flooring.mastery");
        appContext.refresh();
        FlooringController controller = appContext.getBean("flooringController", FlooringController.class);
        controller.run();
        appContext.close();
    }
}


