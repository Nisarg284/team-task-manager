package com.n23.taskmanager.repository;

import com.n23.taskmanager.entity.Project;
import com.n23.taskmanager.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ProjectRepository extends JpaRepository<Project, Long> {

    List<Project> findByCreatedBy(User user);

    @Query("SELECT DISTINCT p FROM Project p JOIN p.members m WHERE m.user.id = :userId")
    List<Project> findProjectsByMemberId(@Param("userId") Long userId);

    @Query("SELECT DISTINCT p FROM Project p LEFT JOIN p.members m WHERE p.createdBy.id = :userId OR m.user.id = :userId")
    List<Project> findAllAccessibleProjects(@Param("userId") Long userId);
}
