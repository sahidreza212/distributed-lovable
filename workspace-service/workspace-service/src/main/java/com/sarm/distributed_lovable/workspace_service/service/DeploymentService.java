package com.sarm.distributed_lovable.workspace_service.service;


import com.sarm.distributed_lovable.workspace_service.dto.deploy.DeployResponse;
import jakarta.annotation.Nullable;

public interface DeploymentService {

  @Nullable
  DeployResponse deploy(Long projectId);
}
