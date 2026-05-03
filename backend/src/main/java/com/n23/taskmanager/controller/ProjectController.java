package com.n23.taskmanager.controller;

import com.n23.taskmanager.dto.AddMemberRequest;
import com.n23.taskmanager.dto.ProjectRequest;
import com.n23.taskmanager.dto.ProjectResponse;
import com.n23.taskmanager.entity.User;
import com.n23.taskmanager.service.AuthService;
import com.n23.taskmanager.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final ProjectService projectService;
    private final AuthService authService;

    @GetMapping
    public ResponseEntity<List<ProjectResponse>> getAllProjects(Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(projectService.getAllProjects(currentUser));
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectResponse> getProject(@PathVariable Long id, Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(projectService.getProjectById(id, currentUser));
    }

    @PostMapping
    public ResponseEntity<ProjectResponse> createProject(@Valid @RequestBody ProjectRequest request,
                                                          Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(projectService.createProject(request, currentUser));
    }

    @PutMapping("/{id}")
    public ResponseEntity<ProjectResponse> updateProject(@PathVariable Long id,
                                                          @Valid @RequestBody ProjectRequest request,
                                                          Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(projectService.updateProject(id, request, currentUser));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Map<String, String>> deleteProject(@PathVariable Long id,
                                                              Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        projectService.deleteProject(id, currentUser);
        return ResponseEntity.ok(Map.of("message", "Project deleted successfully"));
    }

    @PostMapping("/{id}/members")
    public ResponseEntity<ProjectResponse> addMember(@PathVariable Long id,
                                                      @Valid @RequestBody AddMemberRequest request,
                                                      Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        return ResponseEntity.ok(projectService.addMember(id, request, currentUser));
    }

    @DeleteMapping("/{id}/members/{userId}")
    public ResponseEntity<Map<String, String>> removeMember(@PathVariable Long id,
                                                             @PathVariable Long userId,
                                                             Authentication authentication) {
        User currentUser = authService.getCurrentUser(authentication.getName());
        projectService.removeMember(id, userId, currentUser);
        return ResponseEntity.ok(Map.of("message", "Member removed successfully"));
    }

    @GetMapping("/users")
    public ResponseEntity<List<Map<String, Object>>> getAllUsers() {
        return ResponseEntity.ok(
                projectService.getAllUsers().stream()
                        .map(u -> Map.<String, Object>of(
                                "id", u.getId(),
                                "name", u.getName(),
                                "email", u.getEmail(),
                                "role", u.getRole().name()
                        ))
                        .collect(Collectors.toList())
        );
    }
}
