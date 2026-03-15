package com.sarm.distributed_lovable.workspace_service.dto.member;

import com.sarm.distributed_lovable.common_lib.enums.ProjectRole;
import jakarta.validation.constraints.NotNull;

public record updateMemberRoleRequest(
      @NotNull ProjectRole role) {
}
