package com.n23.taskmanager.repository;

import com.n23.taskmanager.entity.Task;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.util.List;

@Repository
public interface TaskRepository extends JpaRepository<Task, Long> {

    List<Task> findByProjectId(Long projectId);

    List<Task> findByAssignedToId(Long userId);

    @Query("SELECT t FROM Task t WHERE t.assignedTo.id = :userId OR t.createdBy.id = :userId")
    List<Task> findTasksByUser(@Param("userId") Long userId);

    @Query("SELECT t FROM Task t WHERE t.project.id IN :projectIds")
    List<Task> findByProjectIdIn(@Param("projectIds") List<Long> projectIds);

    @Query("SELECT t FROM Task t WHERE t.dueDate < :today AND t.status != 'DONE' AND (t.assignedTo.id = :userId OR t.createdBy.id = :userId)")
    List<Task> findOverdueTasks(@Param("today") LocalDate today, @Param("userId") Long userId);

    @Query("SELECT t FROM Task t WHERE (t.assignedTo.id = :userId OR t.createdBy.id = :userId) ORDER BY t.createdAt DESC")
    List<Task> findRecentTasks(@Param("userId") Long userId);

    long countByStatus(Task.Status status);

    @Query("SELECT COUNT(t) FROM Task t WHERE t.dueDate < :today AND t.status != 'DONE'")
    long countOverdueTasks(@Param("today") LocalDate today);
}
