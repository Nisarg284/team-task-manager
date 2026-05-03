package com.n23.taskmanager.service;

import com.n23.taskmanager.dto.AddMemberRequest;
import com.n23.taskmanager.dto.ProjectRequest;
import com.n23.taskmanager.dto.ProjectResponse;
import com.n23.taskmanager.entity.Project;
import com.n23.taskmanager.entity.ProjectMember;
import com.n23.taskmanager.entity.User;
import com.n23.taskmanager.exception.ResourceNotFoundException;
import com.n23.taskmanager.repository.ProjectMemberRepository;
import com.n23.taskmanager.repository.ProjectRepository;
import com.n23.taskmanager.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ProjectService {

    private final ProjectRepository projectRepository;
    private final ProjectMemberRepository projectMemberRepository;
    private final UserRepository userRepository;

    public List<ProjectResponse> getAllProjects(User currentUser) {
        List<Project> projects;

        if (currentUser.getRole() == User.Role.ADMIN) {
            projects = projectRepository.findAll();
        } else {
            projects = projectRepository.findAllAccessibleProjects(currentUser.getId());
        }

        return projects.stream().map(this::toProjectResponse).collect(Collectors.toList());
    }

    public ProjectResponse getProjectById(Long id, User currentUser) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        // Check access: admin can access all, members only their projects
        if (currentUser.getRole() != User.Role.ADMIN) {
            boolean isMember = projectMemberRepository.existsByProjectIdAndUserId(id, currentUser.getId());
            boolean isCreator = project.getCreatedBy().getId().equals(currentUser.getId());
            if (!isMember && !isCreator) {
                throw new SecurityException("You don't have access to this project");
            }
        }

        return toProjectResponse(project);
    }

    @Transactional
    public ProjectResponse createProject(ProjectRequest request, User currentUser) {
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Only admins can create projects");
        }

        Project project = Project.builder()
                .name(request.getName())
                .description(request.getDescription())
                .createdBy(currentUser)
                .build();

        project = projectRepository.save(project);

        // Auto-add creator as ADMIN member
        ProjectMember creatorMember = ProjectMember.builder()
                .project(project)
                .user(currentUser)
                .role(ProjectMember.MemberRole.ADMIN)
                .build();
        projectMemberRepository.save(creatorMember);

        return toProjectResponse(project);
    }

    @Transactional
    public ProjectResponse updateProject(Long id, ProjectRequest request, User currentUser) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        checkAdminAccess(currentUser);

        project.setName(request.getName());
        project.setDescription(request.getDescription());
        project = projectRepository.save(project);

        return toProjectResponse(project);
    }

    @Transactional
    public void deleteProject(Long id, User currentUser) {
        Project project = projectRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + id));

        checkAdminAccess(currentUser);

        projectRepository.delete(project);
    }

    @Transactional
    public ProjectResponse addMember(Long projectId, AddMemberRequest request, User currentUser) {
        checkAdminAccess(currentUser);

        Project project = projectRepository.findById(projectId)
                .orElseThrow(() -> new ResourceNotFoundException("Project not found with id: " + projectId));

        User user = userRepository.findById(request.getUserId())
                .orElseThrow(() -> new ResourceNotFoundException("User not found with id: " + request.getUserId()));

        if (projectMemberRepository.existsByProjectIdAndUserId(projectId, user.getId())) {
            throw new IllegalArgumentException("User is already a member of this project");
        }

        ProjectMember.MemberRole memberRole = ProjectMember.MemberRole.MEMBER;
        if (request.getRole() != null && request.getRole().equalsIgnoreCase("ADMIN")) {
            memberRole = ProjectMember.MemberRole.ADMIN;
        }

        ProjectMember member = ProjectMember.builder()
                .project(project)
                .user(user)
                .role(memberRole)
                .build();
        projectMemberRepository.save(member);

        return toProjectResponse(project);
    }

    @Transactional
    public void removeMember(Long projectId, Long userId, User currentUser) {
        checkAdminAccess(currentUser);

        if (!projectMemberRepository.existsByProjectIdAndUserId(projectId, userId)) {
            throw new ResourceNotFoundException("Member not found in this project");
        }

        projectMemberRepository.deleteByProjectIdAndUserId(projectId, userId);
    }

    public List<User> getAllUsers() {
        return userRepository.findAll();
    }

    private void checkAdminAccess(User currentUser) {
        if (currentUser.getRole() != User.Role.ADMIN) {
            throw new SecurityException("Only admins can perform this action");
        }
    }

    private ProjectResponse toProjectResponse(Project project) {
        List<ProjectMember> members = projectMemberRepository.findByProjectId(project.getId());

        List<ProjectResponse.MemberInfo> memberInfos = members.stream()
                .map(m -> ProjectResponse.MemberInfo.builder()
                        .userId(m.getUser().getId())
                        .name(m.getUser().getName())
                        .email(m.getUser().getEmail())
                        .role(m.getRole().name())
                        .build())
                .collect(Collectors.toList());

        return ProjectResponse.builder()
                .id(project.getId())
                .name(project.getName())
                .description(project.getDescription())
                .createdByName(project.getCreatedBy().getName())
                .createdById(project.getCreatedBy().getId())
                .createdAt(project.getCreatedAt())
                .taskCount(project.getTasks() != null ? project.getTasks().size() : 0)
                .memberCount(members.size())
                .members(memberInfos)
                .build();
    }
}
