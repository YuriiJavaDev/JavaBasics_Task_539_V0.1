# Spring Component Scanning Architecture: Task Management System (JavaBasics_Task_539_V0.1)
## 📖 Description
This project focuses on transitioning from manual object instantiation to an automated dependency injection pattern using the Spring Framework. It implements a decoupled modular architecture for a task management system, leveraging Spring Component Scanning to discover and register application beans. The system comprises a model layer, a repository for data persistence, a business service layer for logic handling, and a dedicated UI component for data presentation. By removing hard-coded dependencies and shifting configuration to a pure @Configuration approach, the project ensures high maintainability and adherence to modern inversion of control principles.

## 📋 Requirements Compliance
Stereotype Specialization: Implemented standard Spring stereotypes (@Repository, @Service, @Component) to designate specific bean roles.

Component Scanning Infrastructure: Configured context discovery via @ComponentScan to automatically detect and register all application components.

Constructor-Based Injection: Enforced strict dependency coupling through constructor-based injection, eliminating the need for @Autowired field injection.

Pure Configuration Approach: Designed a standalone AppConfig class that uses @Configuration without any manual @Bean factory methods.

## 🚀 Architectural Stack
Java 23 (Spring Framework 6.x, Spring Context, Core Inversion of Control Containers)

## 🏗️ Implementation Details
TaskRepository: Encapsulates data storage logic within the @Repository layer, acting as a managed persistence provider.

TaskService: Provides a centralized business logic layer, injecting the repository via constructor to manage task lifecycle operations.

TaskPrinter: A standalone @Component dedicated to rendering data, demonstrating clean separation between service logic and output concerns.

AppConfig: Serves as the backbone of the application, configuring the component scanning scope to bridge all modules seamlessly.

## 📋 Expected result
Execution of the main() method triggers the initialization of the AnnotationConfigApplicationContext.

The application successfully constructs the bean hierarchy, processes input tasks, and outputs the task list to the console prefixed with "- " identifiers.

Verification of correct dependency injection and bean lifecycle management within the Spring container environment.

### Project Structure:

    JavaBasics_Task_538/
    ├─ src/main/java/com/yurii/pavlenko/
    │                          ├── model/
    │                          │   └── Task.java
    │                          ├── repository/
    │                          │   └── TaskRepository.java
    │                          ├── service/
    │                          │   └── TaskService.java
    │                          ├── component/
    │                          │   └── TaskPrinter.java
    │                          ├── config/
    │                          │   └── AppConfig.java
    │                          └── Main.java
    ├── pom.xml
    ├── LICENSE
    ├── TASK.md
    ├── THEORY.md
    └── README.md

## 💻 Code Example

```java
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

        AnnotationConfigApplicationContext context = new AnnotationConfigApplicationContext(AppConfig.class);

        TaskService taskService = context.getBean(TaskService.class);
        TaskPrinter taskPrinter = context.getBean(TaskPrinter.class);

        taskService.saveTask(new Task("Learn Spring"));
        taskService.saveTask(new Task("Master Dependency Injection"));
        taskService.saveTask(new Task("Finish the project"));

        taskPrinter.printAll();

        context.close();
    }
}
```

## ⚖️ License
This project is licensed under the **MIT License**.

Copyright (c) 2026 Yurii Pavlenko

Permission is hereby granted, free of charge, to any person obtaining a copy of this software and associated documentation files...

License: [MIT](LICENSE)
