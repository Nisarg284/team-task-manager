package com.n23.taskmanager.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
public class AddMemberRequest {

    @NotNull(message = "User ID is required")
    private Long userId;

    private String role; // ADMIN or MEMBER (defaults to MEMBER)
}
