package com.n23.taskmanager.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class TaskRequest {

    @NotBlank(message = "Task title is required")
    private String title;

    private String description;

    @NotNull(message = "Project ID is required")
    private Long projectId;

    private Long assignedTo; // user ID

    private String priority; // LOW, MEDIUM, HIGH

    private String dueDate; // yyyy-MM-dd

    private String status; // TODO, IN_PROGRESS, DONE
}
