package com.gustavo.taskmanager.dto;

import com.gustavo.taskmanager.entity.TaskPriority;

public class AiSuggestPriorityResponseDTO {

    private TaskPriority priority;
    private String reason;

    public AiSuggestPriorityResponseDTO() {
    }

    public AiSuggestPriorityResponseDTO(TaskPriority priority, String reason) {
        this.priority = priority;
        this.reason = reason;
    }

    public TaskPriority getPriority() {
        return priority;
    }

    public void setPriority(TaskPriority priority) {
        this.priority = priority;
    }

    public String getReason() {
        return reason;
    }

    public void setReason(String reason) {
        this.reason = reason;
    }
}
