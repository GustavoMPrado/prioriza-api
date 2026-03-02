package com.gustavo.taskmanager.controller;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import com.gustavo.taskmanager.dto.CreateTaskRequest;
import com.gustavo.taskmanager.dto.PatchTaskRequest;
import com.gustavo.taskmanager.dto.TaskResponse;
import com.gustavo.taskmanager.dto.UpdateTaskRequest;
import com.gustavo.taskmanager.entity.Task;
import com.gustavo.taskmanager.entity.TaskPriority;
import com.gustavo.taskmanager.entity.TaskStatus;
import com.gustavo.taskmanager.service.TaskService;

import jakarta.validation.Valid;

@RestController
@RequestMapping("/tasks")
public class TaskRestController {

    private static final int DEFAULT_PAGE = 0;
    private static final int DEFAULT_SIZE = 10;
    private static final int MAX_SIZE = 50;

    private final TaskService taskApplicationService;

    public TaskRestController(TaskService taskService) {
        this.taskApplicationService = taskService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    public TaskResponse createTask(@Valid @RequestBody CreateTaskRequest dto) {
        Task created = taskApplicationService.create(dto);
        return taskApplicationService.toResponseDTO(created);
    }

    @GetMapping
    public Page<TaskResponse> searchTasks(
            @RequestParam(required = false) String q,
            @RequestParam(required = false) TaskStatus status,
            @RequestParam(required = false) TaskPriority priority,
            @RequestParam(defaultValue = "" + DEFAULT_PAGE) int page,
            @RequestParam(defaultValue = "" + DEFAULT_SIZE) int size
    ) {
        int safePage = Math.max(page, 0);
        int safeSize = Math.min(Math.max(size, 1), MAX_SIZE);
        Pageable pageable = PageRequest.of(safePage, safeSize);
        return taskApplicationService.search(q, status, priority, pageable);
    }

    @GetMapping("/{id}")
    public TaskResponse getTaskById(@PathVariable Long id) {
        Task task = taskApplicationService.findById(id);
        return taskApplicationService.toResponseDTO(task);
    }

    @PutMapping("/{id}")
    public TaskResponse update(@PathVariable Long id, @Valid @RequestBody UpdateTaskRequest dto) {
        return taskApplicationService.update(id, dto);
    }

    @PatchMapping("/{id}")
    public TaskResponse patch(@PathVariable Long id, @Valid @RequestBody PatchTaskRequest dto) {
        return taskApplicationService.patch(id, dto);
    }

    @DeleteMapping("/{id}")
    @ResponseStatus(HttpStatus.NO_CONTENT)
    public void deleteTask(@PathVariable Long id) {
        taskApplicationService.delete(id);
    }
}






