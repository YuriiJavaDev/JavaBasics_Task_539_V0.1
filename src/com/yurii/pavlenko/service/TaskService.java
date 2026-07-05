package com.yurii.pavlenko.service;

import com.yurii.pavlenko.model.Task;
import com.yurii.pavlenko.repository.TaskRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;

/**
 * Service to manage business logic.
 */
@Service
public class TaskService {
    private final TaskRepository repository;

    @Autowired
    public TaskService(TaskRepository repository) {
        this.repository = repository;
    }

    public void saveTask(Task task) {
        repository.addTask(task);
    }

    public List<Task> findAll() {
        return repository.getAllTasks();
    }
}