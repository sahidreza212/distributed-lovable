package com.sarm.distributed_lovable.workspace_service.dto.Project;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;

public record ProjectRequest(
        @NotBlank(message = "Project name must not be empty")
        String name) {
}
