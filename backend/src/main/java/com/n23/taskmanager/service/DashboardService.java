package com.n23.taskmanager.service;

import com.n23.taskmanager.dto.DashboardResponse;
import com.n23.taskmanager.dto.TaskResponse;
import com.n23.taskmanager.entity.Task;
import com.n23.taskmanager.entity.User;
import com.n23.taskmanager.repository.ProjectRepository;
import com.n23.taskmanager.repository.TaskRepository;
import com.n23.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DashboardService {

    private final ProjectRepository projectRepository;
    private final TaskRepository taskRepository;
    private final UserRepository userRepository;

    public DashboardResponse getDashboard(User currentUser) {
        List<Task> allTasks;
        int totalProjects;

        if (currentUser.getRole() == User.Role.ADMIN) {
            allTasks = taskRepository.findAll();
            totalProjects = (int) projectRepository.count();
        } else {
            allTasks = taskRepository.findTasksByUser(currentUser.getId());
            totalProjects = projectRepository.findAllAccessibleProjects(currentUser.getId()).size();
        }

        long todoCount = allTasks.stream().filter(t -> t.getStatus() == Task.Status.TODO).count();
        long inProgressCount = allTasks.stream().filter(t -> t.getStatus() == Task.Status.IN_PROGRESS).count();
        long doneCount = allTasks.stream().filter(t -> t.getStatus() == Task.Status.DONE).count();

        LocalDate today = LocalDate.now();
        List<Task> overdueTasks = allTasks.stream()
                .filter(t -> t.getDueDate() != null
                        && t.getDueDate().isBefore(today)
                        && t.getStatus() != Task.Status.DONE)
                .collect(Collectors.toList());

        // Recent 5 tasks
        List<TaskResponse> recentTasks = allTasks.stream()
                .sorted((a, b) -> {
                    if (a.getCreatedAt() == null && b.getCreatedAt() == null) return 0;
                    if (a.getCreatedAt() == null) return 1;
                    if (b.getCreatedAt() == null) return -1;
                    return b.getCreatedAt().compareTo(a.getCreatedAt());
                })
                .limit(5)
                .map(this::toTaskResponse)
                .collect(Collectors.toList());

        List<TaskResponse> overdueTaskResponses = overdueTasks.stream()
                .map(this::toTaskResponse)
                .collect(Collectors.toList());

        return DashboardResponse.builder()
                .totalProjects(totalProjects)
                .totalTasks(allTasks.size())
                .todoTasks((int) todoCount)
                .inProgressTasks((int) inProgressCount)
                .doneTasks((int) doneCount)
                .overdueTasks(overdueTasks.size())
                .totalMembers((int) userRepository.count())
                .recentTasks(recentTasks)
                .overdueTaksList(overdueTaskResponses)
                .build();
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
