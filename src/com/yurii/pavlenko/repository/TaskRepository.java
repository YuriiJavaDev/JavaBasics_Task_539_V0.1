package com.yurii.pavlenko.repository;

import com.yurii.pavlenko.model.Task;
import org.springframework.stereotype.Repository;
import java.util.ArrayList;
import java.util.List;

/**
 * Repository to store tasks.
 */
@Repository
public class TaskRepository {
    private final List<Task> tasks = new ArrayList<>();

    public void addTask(Task task) {
        tasks.add(task);
    }

    public List<Task> getAllTasks() {
        return tasks;
    }
}