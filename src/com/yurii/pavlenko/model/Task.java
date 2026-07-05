package com.yurii.pavlenko.model;

/**
 * Model class for Task.
 */
public class Task {
    private String description;

    public Task(String description) {
        this.description = description;
    }

    public String getDescription() {
        return description;
    }
}