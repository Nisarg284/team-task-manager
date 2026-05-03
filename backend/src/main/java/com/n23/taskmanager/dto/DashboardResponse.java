package com.n23.taskmanager.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class DashboardResponse {
    private int totalProjects;
    private int totalTasks;
    private int todoTasks;
    private int inProgressTasks;
    private int doneTasks;
    private int overdueTasks;
    private int totalMembers;
    private List<TaskResponse> recentTasks;
    private List<TaskResponse> overdueTaksList;
}
