## Bean и Component Scanning.

### Lesson Objectives

1. Understand exactly what a **bean** is—not just an "object," but a unit managed by the container.
2. Understand how Spring **sees** an application: the `BeanDefinition` registry, bean names, types.
3. Understand the problem of manual registration via `@Bean` and move on to **automatic discovery**.
4. Master `@Component` and `@ComponentScan`.
5. Master stereotype annotations: `@Service`, `@Repository`, `@Controller`.
6. Master `@Autowired`—dependency injection without explicit `@Bean` methods.

**Relationship to the previous lesson:** Last time, we manually wrote an `@Configuration` class with `@Bean` methods for each object (`taskRepository()`, `taskService()`). Today, we'll look at what Spring actually does with these methods—and how to eliminate manual registration altogether.

---

## Part 1. What is a Bean?

### 1.1 Bean ≠ Just an Object

When you write `new TaskRepository()`, you create a regular Java object. No one but you knows about it. If you no longer need it, you simply stop referencing it.

A **Bean** is an object whose **lifecycle is managed by the container** (Spring `ApplicationContext`):
- the container decides **when** to create it;
- the container decides **which dependencies** to pass to it;
- the container stores **a reference to it** in its internal registry under a specific name;
- the container can call its initialization/destruction methods;
- the container decides **how many instances** to create (scope—singleton, prototype, etc.).

> singleton → one shared object for the entire application

> prototype → a new object for each request to the container

In other words: **bean = regular object + metadata + lifecycle control delegated to the container**.

```java
TaskRepository repo = new TaskRepository(); // regular object, no one knows about it
TaskRepository beanRepo = context.getBean(TaskRepository.class); // bean — Spring created it, stores it, and returns it
```

---

### 🧠 Analogy: furniture from a catalog

A regular object (`new TaskRepository()`) is a stool you knocked together in your garage.
It exists, but no one but you knows about it, and it's not in the shared furniture registry at home.

A bean is a piece of furniture from the IKEA catalog: it has an **article** (the bean name), it is **registered** in the system (the container registry), and if someone asks, "Give me the chair with article number X," the system knows exactly where it is and what state it's in.

---

### Level 1 — Bean or Not a Bean?

For each case, determine: is a **bean** being created, or just a regular Java object?

```java
// Case A
TaskRepository repo = new TaskRepository();

// Case B — inside a @Configuration class
@Bean
public TaskRepository taskRepository() {
    return new TaskRepository();
}

// Case C
TaskRepository repo = context.getBean(TaskRepository.class);

// Case D
public class TaskService {
    private TaskRepository repo = new TaskRepository(); // field inside the TaskService bean
}
```

*(Hint for Case D: it's not where `new` is written that matters, but whether Spring manages the lifecycle of this particular object.)*

---

## Part 2. How the Container "Sees" the Application

### 2.1 BeanDefinition — a Blueprint, Not an Object

When Spring reads an @Configuration class, it doesn't immediately create objects. First, it
builds a blueprint registry, a `BeanDefinition`: for each future bean, it remembers
its type, name, dependencies, and scope. And only then, when the blueprints are ready, does it begin
**instantiating** (creating) the objects themselves.

```
AppConfig.class
        │
        ▼
Spring reads annotations → creates a BeanDefinition for "taskRepository" (type TaskRepository)
→ creates a BeanDefinition for "taskService" (type TaskService, depends on taskRepository)
        │
        ▼
Spring creates actual objects from blueprints → adds them to the bean registry
```

This explains why Spring **handles the creation order** itself: it sees from the blueprints
that `taskService` depends on `taskRepository`, and creates `taskRepository` first—
you don't need to worry about the order of calls, as you would with a manual `AppContext`.

### 2.2 Bean Name

By default, the bean name is the name of the `@Bean` method (or, as we'll see later, the lowercase class name for `@Component`):

```java
@Bean
public TaskRepository taskRepository() { ... } // bean name: "taskRepository"
```

You can get a bean by type or by name:

```java
TaskRepository repo1 = context.getBean(TaskRepository.class); // by type
TaskRepository repo2 = (TaskRepository) context.getBean("taskRepository"); // by name
```

---

### Level 2 — Exploring the Bean Registry

Take the `AppConfig` from the previous lesson (with `Logger`, `TaskRepository`, `TaskService`) and in `main()` execute:

```java
var context = new AnnotationConfigApplicationContext(AppConfig.class);
    for (String name : context.getBeanDefinitionNames()) {
        System.out.println(name);
    }
```

1. How many names were printed, and what are they?
2. Do they match the names of the `@Bean` methods?
3. Add another `@Bean` method with an explicit name: `@Bean(name = "myRepo")`. What will change in the output?

---

### - ❗ Why do you need to give a custom name to the bean `name = "..."`?

Because the method name and the bean name **don't always have to match**.

**🧩 Real-World Cases When This Is Necessary**

**1. When a Method Can't Be Named Like a Bean**

```java
@Bean(name = "primaryRepo")
public ProductRepository createRepository() {
    return new ProductRepository();
}
```

👉 The method is called `createRepository`, but the bean is called `primaryRepo`

---

**2. When there is legacy / other people's code**

Sometimes you don't want to change the method name (or it's already standardized):

```java
@Bean(name = "dbRepo")
public ProductRepository repository() {
    return new ProductRepository();
}
```

---

**3. When there are multiple beans of the same type**

```java
@Bean(name = "memoryRepo")
public ProductRepository repo1() { ... }

@Bean(name = "dbRepo")
public ProductRepository repo2() { ... }
```

👉 The name is important here for `@Qualifier`

---

**4. When the name is important for integration**

Sometimes other parts of the system expect a specific name:

```java
context.getBean("transactionManager")
```

👉 Spring does this automatically in the infrastructure

---

## Part 3. The Manual Registration Problem

In the previous lesson, we wrote a separate `@Bean` method for **each** class:

```java
@Configuration
public class AppConfig {
    @Bean
    public Logger logger() {
        return new Logger();
    }
    
    @Bean
    public TaskRepository taskRepository() {
        return new TaskRepository();
    }
    
    @Bean
    public TaskService taskService(TaskRepository repository, Logger logger) {
        return new TaskService(repository, logger);
    }
}
```

Works great for three classes. What if the application has 50 classes: repositories,
services, controllers? You'll have to write 50 @Bean methods, manually listing the parameters for each constructor. One typo in the parameters, and the application won't build.

We want Spring to automatically find the necessary classes in the project and figure out which dependencies to pass to them — without explicitly listing them in @Configuration.

---

### Level 3 — Calculate Manual Work

Given an application with the following classes: UserRepository, OrderRepository, UserService, OrderService, PaymentService, NotificationService, UserController, and OrderController (8 classes).

1. How many @Bean methods will need to be written manually?
2. `PaymentService` depends on `UserRepository`, `OrderRepository`, and `NotificationService`.
   Write a single `@Bean` method for it, as we did in Lesson 3—let this demonstrate why a manual approach becomes tedious as the dependency graph grows.
- also `Logger`, `EmailSender`, `AuditService`...

Then `PaymentService` also has `Logger`, `EmailSender`, `AuditService`...

And the method grows:

```java 
@Bean 
public PaymentService paymentService( 
    UserRepository userRepository, 
    OrderRepository orderRepository, 
    NotificationService notificationService, 
    Logger logger, 
    EmailSender emailSender, 
    AuditService auditService) { 
        
        return new PaymentService( 
        userRepository 
        orderRepository, 
        notificationService 
        logger 
        emailSender, 
        auditService); 
    } 
```

---

## Part 4. @Component and @ComponentScan

### 4.1 Marking a class as a candidate

Instead of the `@Bean` method in the configuration, we annotate the class directly:

```java
import org.springframework.stereotype.Component;

@Component
public class TaskRepository {
    private List<Task> tasks = new ArrayList<>();
    public void add(Task t) {
        tasks.add(t);
    }
    
    public List<Task> getAll() {
        return tasks;
    }
}

@Component
public class TaskService {
    private final TaskRepository repository;
    
    // Spring will automatically find the constructor and insert the required bean
    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }
}
```

`@Component` tells Spring: "This class is a bean candidate. Create it yourself if I ask you to."

### 4.2 @ComponentScan — Where to Look

`@Component` by itself doesn't run anything. The container needs to **know where to look** for these classes:

```java
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

@Configuration
@ComponentScan(basePackages = "org.example") //"Spring, scan the org.example package and all its subpackages."
    public class AppConfig {
        // @Bean methods may no longer exist at all—everything is found by scanning
    }
```

As a reminder, in our examples, the container is an object:

```java
var context = new AnnotationConfigApplicationContext(AppConfig.class);
```

This object's type is: `AnnotationConfigApplicationContext`

It implements the Spring interface: `ApplicationContext`

```
new AnnotationConfigApplicationContext(AppConfig.class)
→ scans @ComponentScan("org.example")
→ traverses all classes in the org.example package and its subpackages
→ finds classes with @Component → creates a BeanDefinition for each
→ creates objects by resolving constructor dependencies (TaskService requires TaskRepository → finds the bean by type)
→ adds it to the registry under the name "taskRepository", "taskService" (class name with lowercase letters)
```

If you have multiple packages, you can pass an array of strings:

```java
@ComponentScan(
    basePackages = {
        "org.example.service",
        "org.example.repository"
    }
)
```

The execution is exactly the same as in Lesson 3 - getBean() doesn't know whether the bean was created via the @Bean method or via scanning:

```java
var context = new AnnotationConfigApplicationContext(AppConfig.class);
TaskService service = context.getBean(TaskService.class);
```

- **Check if a bean exists**

**1. Check by type**

For example:

```java
var context = new AnnotationConfigApplicationContext(AppConfig.class);

boolean exists = context.getBeansOfType(TaskService.class).size() > 0;

System.out.println(exists);
```

If at least one bean of type `TaskService` is found, prints: `true`

---

**2. Check by name**

If we know the bean name:

```java
boolean exists = context.containsBean("taskService");

System.out.println(exists);
```

---

**3. Find all beans of a given type**

```java
var beans = context.getBeansOfType(TaskService.class);

System.out.println(beans);
```

For example:

```
{taskService=org.example.TaskService@4fca772d}
```

---

**4. Using getBean() and exception handling**

You can do this:

```java
try {
    TaskService service =
    context.getBean(TaskService.class);
    System.out.println("Found");
    }
    catch (Exception e){
        System.out.println("Not found");
    }
```

But this isn't the best way to do a regular check.

---

### Level 4 — Switch AppConfig to Scan

Take the AppConfig from Lesson 3 (with Logger, ProductRepository, ProductService, and three @Bean methods).

1. Remove all @Bean methods.
2. Add @Component to the Logger, ProductRepository, and ProductService classes.
3. Add @ComponentScan(basePackages = "...") to AppConfig (specify your actual package).
4. Run main() as before — verify that the [LOG] ... lines are printed as before.

Compare the number of lines of code in AppConfig before and after.

- **If you received an "exception"**

When performing component scanning, Spring reads .class files through the built-in ASM library to find annotations (@Component, etc.) without fully loading the class into the JVM. The version of ASM included with Spring 6.1.0 (released in late 2023) can only recognize bytecode up to a certain Java version. Java 26 is a very new release (released in 2026), and its bytecode format is newer than the ASM in Spring 6.1.0 can read. Hence:

`Unsupported class file major version 70`

(70 is the bytecode version number for Java 26).

Your pom.xml contains:
<maven.compiler.source>26</maven.compiler.source>
<maven.compiler.target>26</maven.compiler.target>
This means the Maven compiler is explicitly compiling classes for Java 26, hence the incompatibility with Spring.

**What needs to be done**

The simplest and most correct solution is to compile the project for an older (stable, LTS) version of Java, for example 21. Spring 6.1 was developed specifically for Java 21/17, so it's guaranteed to be compatible. You don't need to install a separate JDK 21 to achieve this—you can use the release flag to tell the already installed JDK 26 to "pretend" to be a compiler for an older version.

Steps:

1. Open pom.xml
2. Find the block:
   **<maven.compiler.source>26</maven.compiler.source>
   <maven.compiler.target>26</maven.compiler.target>**
3. Replace it with:
   **<maven.compiler.release>21</maven.compiler.release>**
4. (One tag instead of two—release specifies both the source and target, and ensures that APIs newer than the specified version are not used.)
5. In IntelliJ IDEA, choose Maven → Reload Project (or the Maven toolbar refresh button) to pick up the pom.xml changes.
6. Rebuild the project: Build → Rebuild Project (or mvn clean compile in the terminal inside the project folder).
7. Run Main again—the 'Unsupported class file major' version error should disappear.

If for some reason you want to stay on Java 26, then the alternative is to upgrade spring-context to a newer version (it has a newer ASM). However, there's no guarantee that any current version of Spring will support JDK 26, as it's a very recent JDK release. Therefore, for a tutorial project, it's more reliable to downgrade the target bytecode version, as described above.

--

## Part 5. Stereotype Annotations

### 5.1 Why Different Annotations if the Mechanism is the Same

`@Service`, `@Repository`, `@Controller` are **specializations of `@Component`** (technically, they are themselves marked with `@Component` — a "meta-annotation").
For the container, the detection mechanism is the same: scanning finds them just like `@Component`.

The difference lies in the **meaning for the human reader**, and in some cases, in the **additional behavior**:

| Annotation | Application Layer | Additional Behavior |
| --- | --- | --- |
| `@Component` | General-Purpose | None — base case |
| `@Service` | Business Logic | None (purely a semantic label for the code reader) |
| `@Repository` | Data Access | Spring translates database-specific exceptions into its own (`DataAccessException`) |
| `@Controller` | Web layer (HTTP handling) | Used by Spring MVC for request routing |

```java
@Repository
public class TaskRepository { ... } // data layer

@Service
public class TaskService { ... } // business logic

@Controller
public class TaskController { ... } // web layer (useful when we get to Spring MVC)
```

**Important:** If you accidentally add `@Service` to a repository, the application will still build and run (the container makes no difference). However, this confuses the reader of the code and loses the `@Repository` benefit (exception translation).

---

### Level 5 — Set the Right Stereotypes

Given a set of classes. For each, choose the appropriate annotation: `@Repository`, `@Service`, or `@Controller`.

```java
public class OrderRepository {
// works directly with the database (JDBC)
}

public class OrderService {
// calculates discounts, checks business rules, calls OrderRepository
}

public class OrderController {
// receives an HTTP request, calls OrderService, returns a response
}

public class EmailSender {
// sends emails via SMTP — not data, not HTTP, not a pure business rule
}
```

For `EmailSender`, justify your choice — why `@Component` and not one of the three specializations?

---

## Part 6. @Autowired — Dependency Injection

### 6.1 Constructor without @Autowired (if there is only one)

In the example from Part 4, we **didn't write** @Autowired on the `TaskService` constructor—and everything
worked. Starting with Spring 4.3, if a class has **one constructor**, Spring automatically
uses it for dependency injection; the annotation is not required.

In other words, if a class has **only one constructor**, Spring automatically understands:

> "Okay, there are no other options—that means we need to create the object through this one"

and **automatically calls it with the necessary dependencies**.

Simple example:

```java
@Component
public class TaskService {
    
    private final TaskRepository repo;
    
    public TaskService(TaskRepository repo) {
        this.repo = repo;
    }
}
```

👉 **@Autowired is not needed here**

Spring does it automatically:

```bash
TaskService → TaskRepository is needed
Spring looks for the TaskRepository bean
Spring calls:
new TaskService(foundRepo)
```

### 6.2 When @Autowired is required — multiple constructors

```java
@Component
public class TaskService {
    private final TaskRepository repository;
    private final Logger logger;
    
    @Autowired // is required — otherwise Spring won't know which constructor to use
    public TaskService(TaskRepository repository, Logger logger) {
        this.repository = repository;
        this.logger = logger;
    }
    
    public TaskService(TaskRepository repository) {
        this(repository, new Logger());
    }
}
```

The annotation says:

> "Spring, use this constructor for dependency injection."

### 6.3 Field injection — works, but is not recommended

```java
@Component
public class TaskService {
    @Autowired
    private TaskRepository repository; // Spring will inject the value directly into the field
    
    @Autowired
    private Logger logger;
}
```

**🧠 How it works inside Spring**

```bash
1. Spring creates an object:
new TaskService()

2. Then via reflection:
TaskService.repository = bean(TaskRepository)
```

### ❗ Features

- the object is created **without dependencies**
- dependencies are "filled in later"
- a field can be `null` in a non-Spring context

### 💥 Cons

- you can create a "broken" object:

```
new TaskService();// repository = null
```

- dependencies are hidden (not visible in the constructor)
- poorly testable
- you can't make it `final`

**Why this is worse than constructor injection** (remember Level 8 from Lesson 3 – about testability):
- fields are not `final` → the object can be created in a "broken" state without dependencies; - It's impossible to create an object manually (for example, in a test) without reflection—there's no constructor with parameters;
- Dependencies are implicit—it's not clear from the class signature what it needs.

**Rule:** Prefer **constructor injection**. Field injection—only if absolutely necessary (for example, in legacy code that you can't change).

---

## Part 7. Summary: Three Approaches to Bean Registration

| What you do | Manual `AppContext` (Task 3) | `@Bean` in `@Configuration` (Task 3) | `@Component` + `@ComponentScan` (Task 4) |
| --- | --- | --- | --- |
| Where is the bean registered? | Manually in code | Explicit method for each bean | Annotation on the class itself |
| Who finds dependencies? | You do it yourself | You do it yourself (method parameters) | Spring does it itself (scans + analyzes the constructor) |
| Adding a new class | Edit `AppContext` | Write a new `@Bean` method | Add `@Component` — that's it |
| Layer semantics are visible in the code | No | No (just a method) | Yes (`@Service`/`@Repository`/`@Controller`) |
| Suitable for | Learning / small projects | Configurations of Third-Party/External Classes | Your Own Classes in a Large Project |

**Important practical point:** In real projects, both approaches are combined:
`@ComponentScan` — for your own classes, `@Bean` methods in `@Configuration` — for classes
that you didn't write yourself (e.g., a third-party library) and can't use `@Component`.

---

## Final Exercises

---

### Level 7 — Build a Mini-App Using Component Scanning

Build a small application from four classes with the correct stereotypes:

- `Task` — a regular model class (**not** a bean).
- `TaskRepository` (`@Repository`) — stores a list of tasks.
- `TaskService` (`@Service`) — business logic, depends on `TaskRepository` via a constructor.
- `TaskPrinter` (`@Component`) — depends on `TaskService`, it has a `printAll()` method,
  printing all tasks to the console with the `"-"` prefix.

`AppConfig` — only `@Configuration` + `@ComponentScan`, without a single `@Bean` method.

In `main()`: create a context, get `TaskService`, add three tasks, get `TaskPrinter`, call `printAll()`.

---
