## Build a mini-app using component scanning.

### Build a small app from four classes with the correct stereotypes:

- `Task` — a regular model class (**not** a bean).
- `TaskRepository` (`@Repository`) — stores a list of tasks.
- `TaskService` (`@Service`) — business logic, depends on `TaskRepository` via the constructor.
- `TaskPrinter` (`@Component`) — depends on `TaskService`, it has a `printAll()` method,
  prints all tasks to the console with the `"-"` prefix.

`AppConfig` — only `@Configuration` + `@ComponentScan`, without a single `@Bean` method.

### In `main()`: create a context, get `TaskService`, add 3 tasks, get `TaskPrinter`, call `printAll()`.
