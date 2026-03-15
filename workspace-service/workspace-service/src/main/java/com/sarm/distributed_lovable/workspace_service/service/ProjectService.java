package com.sarm.distributed_lovable.workspace_service.service;

import com.sarm.distributed_lovable.common_lib.enums.ProjectPermission;
import com.sarm.distributed_lovable.workspace_service.dto.Project.ProjectRequest;
import com.sarm.distributed_lovable.workspace_service.dto.Project.ProjectResponse;
import com.sarm.distributed_lovable.workspace_service.dto.Project.ProjectSummaryResponse;

import java.util.List;

public interface ProjectService {

    List<ProjectSummaryResponse> getUserProject();

     ProjectSummaryResponse getUserProjectById(Long id);

     ProjectResponse createProject(ProjectRequest request);

     ProjectResponse updateProject(Long id, ProjectRequest request);

    void softDelete(Long id);

    boolean hasPermission(Long projectId, ProjectPermission permission);
}
