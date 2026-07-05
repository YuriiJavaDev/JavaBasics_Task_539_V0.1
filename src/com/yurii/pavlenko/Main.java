package com.yurii.pavlenko;

import com.yurii.pavlenko.config.AppConfig;
import com.yurii.pavlenko.model.Task;
import com.yurii.pavlenko.service.TaskService;
import com.yurii.pavlenko.component.TaskPrinter;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

/**
 * Main entry point.
 */
public class Main {
    public static void main(String[] args) {
        // Create context
        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        // Get beans
        TaskService taskService = context.getBean(TaskService.class);
        TaskPrinter taskPrinter = context.getBean(TaskPrinter.class);

        // Add 3 tasks
        taskService.saveTask(new Task("Learn Spring"));
        taskService.saveTask(new Task("Master Dependency Injection"));
        taskService.saveTask(new Task("Finish the project"));

        // Print tasks
        taskPrinter.printAll();

        context.close();
    }
}