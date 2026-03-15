package com.sarm.distributed_lovable.workspace_service.mappers;


import com.sarm.distributed_lovable.common_lib.enums.ProjectRole;
import com.sarm.distributed_lovable.workspace_service.dto.Project.ProjectResponse;
import com.sarm.distributed_lovable.workspace_service.dto.Project.ProjectSummaryResponse;
import com.sarm.distributed_lovable.workspace_service.entity.Project;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")

public interface ProjectMapper {

    ProjectResponse toProjectResponse(Project project);

    ProjectSummaryResponse toProjectSummaryResponse(Project project, ProjectRole role);

    List<ProjectSummaryResponse> toListOfProjectSummaryResponse(List<Project> projects);

}
