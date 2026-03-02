package com.gustavo.taskmanager.service;

import java.util.List;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import com.gustavo.taskmanager.dto.CreateTaskRequest;
import com.gustavo.taskmanager.dto.TaskPatchDTO;
import com.gustavo.taskmanager.dto.TaskResponse;
import com.gustavo.taskmanager.dto.UpdateTaskRequest;
import com.gustavo.taskmanager.entity.Task;
import com.gustavo.taskmanager.entity.TaskPriority;
import com.gustavo.taskmanager.entity.TaskStatus;
import com.gustavo.taskmanager.exception.TaskNotFoundException;
import com.gustavo.taskmanager.repository.TaskRepository;

@Service
public class TaskService {

    private final TaskRepository taskRepository;

    public TaskService(TaskRepository taskRepository) {
        this.taskRepository = taskRepository;
    }

    public Task create(CreateTaskRequest dto) {
        Task task = new Task();
        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());

        if (dto.getStatus() != null) {
            task.setStatus(dto.getStatus());
        }

        if (dto.getPriority() != null) {
            task.setPriority(dto.getPriority());
        } else {
            task.setPriority(TaskPriority.MEDIUM);
        }

        task.setDueDate(dto.getDueDate());

        return taskRepository.save(task);
    }

    public List<Task> findAll() {
        return taskRepository.findAll();
    }

    public Page<TaskResponse> findAll(Pageable pageable) {
        return taskRepository.findAll(pageable).map(this::toResponseDTO);
    }

    public Page<TaskResponse> search(String q, TaskStatus status, TaskPriority priority, Pageable pageable) {
        String query = q == null ? null : q.trim();
        boolean hasQ = query != null && !query.isBlank();

        if (!hasQ && status == null && priority == null) {
            return findAll(pageable);
        }

        if (hasQ) {
            String like = "%" + query.toLowerCase() + "%";
            return taskRepository.search(like, status, priority, pageable).map(this::toResponseDTO);
        }

        return taskRepository.filterOnly(status, priority, pageable).map(this::toResponseDTO);
    }

    public Task findById(Long id) {
        return taskRepository.findById(id)
                .orElseThrow(() -> new TaskNotFoundException(id));
    }

    public TaskResponse toResponseDTO(Task task) {
        TaskResponse dto = new TaskResponse();
        dto.setId(task.getId());
        dto.setTitle(task.getTitle());
        dto.setDescription(task.getDescription());
        dto.setStatus(task.getStatus());
        dto.setPriority(task.getPriority());
        dto.setDueDate(task.getDueDate());
        dto.setCreatedAt(task.getCreatedAt());
        dto.setUpdatedAt(task.getUpdatedAt());
        return dto;
    }

    public TaskResponse update(Long id, UpdateTaskRequest dto) {
        Task task = findById(id);

        task.setTitle(dto.getTitle());
        task.setDescription(dto.getDescription());

        if (dto.getStatus() != null) {
            task.setStatus(dto.getStatus());
        }

        if (dto.getPriority() != null) {
            task.setPriority(dto.getPriority());
        } else {
            task.setPriority(TaskPriority.MEDIUM);
        }

        task.setDueDate(dto.getDueDate());

        Task saved = taskRepository.save(task);
        return toResponseDTO(saved);
    }

    public TaskResponse patch(Long id, TaskPatchDTO dto) {
        Task task = findById(id);

        if (dto.getTitle() != null) task.setTitle(dto.getTitle());
        if (dto.getDescription() != null) task.setDescription(dto.getDescription());
        if (dto.getStatus() != null) task.setStatus(dto.getStatus());
        if (dto.getPriority() != null) task.setPriority(dto.getPriority());
        if (dto.getDueDate() != null) task.setDueDate(dto.getDueDate());

        Task saved = taskRepository.save(task);
        return toResponseDTO(saved);
    }

    public void delete(Long id) {
        Task task = findById(id);
        taskRepository.delete(task);
    }
}




