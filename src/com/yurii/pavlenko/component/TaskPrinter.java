package com.yurii.pavlenko.component;

import com.yurii.pavlenko.service.TaskService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Component to print tasks to console.
 */
@Component
public class TaskPrinter {
    private final TaskService taskService;

    @Autowired
    public TaskPrinter(TaskService taskService) {
        this.taskService = taskService;
    }

    public void printAll() {
        taskService.findAll().forEach(t -> System.out.println("- " + t.getDescription()));
    }
}