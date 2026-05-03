package com.n23.taskmanager.service;

import com.n23.taskmanager.dto.TaskRequest;
import com.n23.taskmanager.dto.TaskResponse;
import com.n23.taskmanager.entity.Project;
import com.n23.taskmanager.entity.Task;
import com.n23.taskmanager.entity.User;
import com.n23.taskmanager.exception.ResourceNotFoundException;
import com.n23.taskmanager.repository.ProjectRepository;
import com.n23.taskmanager.repository.TaskRepository;
import com.n23.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class TaskService {

    private final TaskRepository taskRepository;
    private final ProjectRepository projectRepository;
    private final UserRepository userRepository;

    public List<TaskResponse> getAllTasks(User currentUser) {
        List<Task> tasks;

        if (currentUser.getRole() == User.Role.ADMIN) {
            tasks = taskRepository.findAll();
        } else {
            tasks = taskRepository.findTasksByUser(currentUser.getId());
        }

        return tasks.stream().map(this::toTaskResponse).collect(Collectors.toList());
    }

    public List<TaskResponse> getTasksByProject(Long projectId) {
        return taskRepository.findByProjectId(projectId)
                .stream().map(this::toTaskResponse).collect(Collectors.toList());
    }

    public TaskResponse getTaskById(Long id) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        return toTaskResponse(task);
    }

    @Transactional
    public TaskResponse createTask(TaskRequest request, User currentUser) {
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Only admins can create tasks");
        }

        Project project = projectRepository.findById(request.getProjectId())
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + request.getProjectId()));

        Task task = Task.builder()
                .title(request.getTitle())
                .description(request.getDescription())
                .status(Task.Status.TODO)
                .priority(parsePriority(request.getPriority()))
                .project(project)
                .createdBy(currentUser)
                .build();

        if (request.getDueDate() != null && !request.getDueDate().isEmpty()) {
            task.setDueDate(LocalDate.parse(request.getDueDate()));
        }

        if (request.getAssignedTo() != null) {
            User assignee = userRepository.findById(request.getAssignedTo())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getAssignedTo()));
            task.setAssignedTo(assignee);
        }

        task = taskRepository.save(task);
        return toTaskResponse(task);
    }

    @Transactional
    public TaskResponse updateTask(Long id, TaskRequest request, User currentUser) {
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Only admins can update tasks");
        }

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        task.setTitle(request.getTitle());
        task.setDescription(request.getDescription());
        task.setPriority(parsePriority(request.getPriority()));

        if (request.getStatus() != null) {
            task.setStatus(Task.Status.valueOf(request.getStatus()));
        }

        if (request.getDueDate() != null && !request.getDueDate().isEmpty()) {
            task.setDueDate(LocalDate.parse(request.getDueDate()));
        }

        if (request.getAssignedTo() != null) {
            User assignee = userRepository.findById(request.getAssignedTo())
                    .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getAssignedTo()));
            task.setAssignedTo(assignee);
        }

        if (request.getProjectId() != null) {
            Project project = projectRepository.findById(request.getProjectId())
                    .orElseThrow(() -> new ResourceNotFoundException("Project not found"));
            task.setProject(project);
        }

        task = taskRepository.save(task);
        return toTaskResponse(task);
    }

    @Transactional
    public TaskResponse updateTaskStatus(Long id, String status, User currentUser) {
        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));

        // Members can update status of tasks assigned to them
        if (currentUser.getRole() != User.Role.ADMIN) {
            if (task.getAssignedTo() == null || !task.getAssignedTo().getId().equals(currentUser.getId())) {
                throw new SecurityException("You can only update status of tasks assigned to you");
            }
        }

        task.setStatus(Task.Status.valueOf(status));
        task = taskRepository.save(task);
        return toTaskResponse(task);
    }

    @Transactional
    public void deleteTask(Long id, User currentUser) {
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Only admins can delete tasks");
        }

        Task task = taskRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Task not found with id: " + id));
        taskRepository.delete(task);
    }

    private Task.Priority parsePriority(String priority) {
        if (priority == null || priority.isEmpty()) return Task.Priority.MEDIUM;
        try {
            return Task.Priority.valueOf(priority.toUpperCase());
        } catch (IllegalArgumentException e) {
            return Task.Priority.MEDIUM;
        }
    }

    private TaskResponse toTaskResponse(Task task) {
        return TaskResponse.builder()
                .id(task.getId())
                .title(task.getTitle())
                .description(task.getDescription())
                .status(task.getStatus().name())
                .priority(task.getPriority().name())
                .dueDate(task.getDueDate())
                .projectId(task.getProject().getId())
                .projectName(task.getProject().getName())
                .assignedToId(task.getAssignedTo() != null ? task.getAssignedTo().getId() : null)
                .assignedToName(task.getAssignedTo() != null ? task.getAssignedTo().getName() : null)
                .createdById(task.getCreatedBy().getId())
                .createdByName(task.getCreatedBy().getName())
                .createdAt(task.getCreatedAt())
                .overdue(task.getDueDate() != null
                        && task.getDueDate().isBefore(LocalDate.now())
                        && task.getStatus() != Task.Status.DONE)
                .build();
    }
}
