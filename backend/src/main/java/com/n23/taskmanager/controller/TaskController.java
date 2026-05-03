package com.n23.taskmanager.controller;

import com.n23.taskmanager.dto.StatusUpdateRequest;
import com.n23.taskmanager.dto.TaskRequest;
import com.n23.taskmanager.dto.TaskResponse;
import com.n23.taskmanager.entity.User;
import com.n23.taskmanager.service.AuthService;
import com.n23.taskmanager.service.TaskService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/tasks")
@RequiredArgsConstructor
public class TaskController {

    private final TaskService taskService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<List<TaskResponse>> getAllTasks(Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(taskService.getAllTasks(currentUser));
    }

    @GetMapping("/project/{projectId}")
    public ResponseEntity<List<TaskResponse>> getTasksByProject(@PathVariable Long projectId) {
        return ResponseEntity.ok(taskService.getTasksByProject(projectId));
    }

    @GetMapping("/{id}")
    public ResponseEntity<TaskResponse> getTask(@PathVariable Long id) {
        return ResponseEntity.ok(taskService.getTaskById(id));
    }

    @PostMapping
    public ResponseEntity<TaskResponse> createTask(@Valid @RequestBody TaskRequest request,
                                                    Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(taskService.createTask(request, currentUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<TaskResponse> updateTask(@PathVariable Long id,
                                                    @Valid @RequestBody TaskRequest request,
                                                    Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(taskService.updateTask(id, request, currentUser));
    }

    @PatchMapping("/{id}/status")
    public ResponseEntity<TaskResponse> updateTaskStatus(@PathVariable Long id,
                                                          @Valid @RequestBody StatusUpdateRequest request,
                                                          Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(taskService.updateTaskStatus(id, request.getStatus(), currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteTask(@PathVariable Long id,
                                                           Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        taskService.deleteTask(id, currentUser);
        return ResponseEntity.ok(Map.of("message", "Task deleted successfully"));
    }
}
