package com.gustavo.taskmanager.dto;

import java.time.LocalDate;

import com.gustavo.taskmanager.entity.TaskPriority;
import com.gustavo.taskmanager.entity.TaskStatus;

import jakarta.validation.constraints.FutureOrPresent;
import jakarta.validation.constraints.Size;

public class PatchTaskRequest {

    @Size(min = 3, max = 120)
    private String title;

    @Size(max = 500)
    private String description;

    private TaskStatus status;
    private TaskPriority priority;

    @FutureOrPresent
    private LocalDate dueDate;

    public String getTitle() { return title; }
    public void setTitle(String title) { this.title = title; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    public TaskStatus getStatus() { return status; }
    public void setStatus(TaskStatus status) { this.status = status; }

    public TaskPriority getPriority() { return priority; }
    public void setPriority(TaskPriority priority) { this.priority = priority; }

    public LocalDate getDueDate() { return dueDate; }
    public void setDueDate(LocalDate dueDate) { this.dueDate = dueDate; }
}

