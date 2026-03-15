package com.sarm.distributed_lovable.workspace_service.serviceImpl;


import com.sarm.distributed_lovable.common_lib.dto.PlanDto;
import com.sarm.distributed_lovable.common_lib.enums.ProjectPermission;
import com.sarm.distributed_lovable.common_lib.enums.ProjectRole;
import com.sarm.distributed_lovable.common_lib.error.BadRequestException;
import com.sarm.distributed_lovable.common_lib.error.ResourceNotFoundException;
import com.sarm.distributed_lovable.common_lib.security.AuthUtil;
import com.sarm.distributed_lovable.workspace_service.client.AccountClient;
import com.sarm.distributed_lovable.workspace_service.dto.Project.ProjectRequest;
import com.sarm.distributed_lovable.workspace_service.dto.Project.ProjectResponse;
import com.sarm.distributed_lovable.workspace_service.dto.Project.ProjectSummaryResponse;
import com.sarm.distributed_lovable.workspace_service.entity.Project;
import com.sarm.distributed_lovable.workspace_service.entity.ProjectMember;
import com.sarm.distributed_lovable.workspace_service.entity.ProjectMemberId;
import com.sarm.distributed_lovable.workspace_service.mappers.ProjectMapper;
import com.sarm.distributed_lovable.workspace_service.repository.ProjectMemberRepository;
import com.sarm.distributed_lovable.workspace_service.repository.ProjectRepository;
import com.sarm.distributed_lovable.workspace_service.security.SecurityExpression;
import com.sarm.distributed_lovable.workspace_service.service.ProjectService;
import com.sarm.distributed_lovable.workspace_service.service.ProjectTemplateService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional
@FieldDefaults(makeFinal = true,level = AccessLevel.PRIVATE)
public class ProjectServiceImpl implements ProjectService {

    ProjectRepository projectRepository;
    ProjectMapper projectMapper;
    ProjectMemberRepository projectMemberRepository;
    AuthUtil authUtil;
    ProjectTemplateService projectTemplateService;
    AccountClient accountClient;
    SecurityExpression securityExpression;


    @Override
    public List<ProjectSummaryResponse> getUserProject() {
        Long userId = authUtil.getCurrentUserId();
        var projectWithRoles = projectRepository.findAllAccessibleByUser(userId);
        return projectWithRoles.stream()
                .map(p-> projectMapper.toProjectSummaryResponse(p.getProject(),p.getRole()))
                .toList();
    }

    @Override
    @PreAuthorize("@security.canViewProject(#projectId)")
    public ProjectSummaryResponse getUserProjectById(Long projectId) {
        Long userId = authUtil.getCurrentUserId();
       var projectWithRole = projectRepository.findAsseciableProjectByIdWithRole(userId,projectId)
               .orElseThrow(()->new BadRequestException("Project Not Found"));

       return projectMapper.toProjectSummaryResponse(projectWithRole.getProject(),projectWithRole.getRole());
    }

    @Override
    public ProjectResponse createProject(ProjectRequest request) {

        System.out.println("USER ID = " + authUtil.getCurrentUserId());

        if(!canCreateNewProject()){
         throw new BadRequestException("User cannot create a New project with current Plan, Upgrade plan now.");
        }
        Long ownerUserId = authUtil.getCurrentUserId();

        Project project = Project.builder()
                .name(request.name())
                .isPublic(false)
                .build();
        project=projectRepository.save(project);

        ProjectMemberId projectMemberId = new ProjectMemberId(project.getId(),ownerUserId);
        ProjectMember projectMember = ProjectMember.builder()
                .id(projectMemberId)
                .projectRole(ProjectRole.OWNER)
                .invitedAt(Instant.now())
                .acceptedAt(Instant.now())
                .project(project)
                .build();
        projectMemberRepository.save(projectMember);

        projectTemplateService.initializeProjectFromTemplate(project.getId());
       return projectMapper.toProjectResponse(project);
    }


    @Override
    @PreAuthorize("@security.canEditProject(#projectId)")
    public ProjectResponse updateProject(Long projectId, ProjectRequest request) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAsseciableProjectById(projectId,userId);
        project.setName(request.name());
        project=projectRepository.save(project);
        return projectMapper.toProjectResponse(project);
    }

    @Override
    @PreAuthorize("@security.canDeleteProject(#projectId)")
    public void softDelete(Long projectId) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAsseciableProjectById(projectId,userId);
        project.setDeletedAt(Instant.now());
        projectRepository.save(project);


    }

    @Override
    public boolean hasPermission(Long projectId, ProjectPermission permission) {
        return securityExpression.hasPermission(projectId,permission);
    }

    public  Project getAsseciableProjectById(Long projectId,Long userId){
        return  projectRepository.findAsseciableProjectById(projectId,userId)
                .orElseThrow(()->new ResourceNotFoundException("Project",projectId.toString()));
    }


    private boolean canCreateNewProject() {
        Long userId = authUtil.getCurrentUserId();
        if (userId == null) {
            return false;
        }
        PlanDto plan = accountClient.getCurrentSubscribedPlanByUser();

        int maxAllowed = plan.maxProjects();
        int ownedCount = projectMemberRepository.countProjectOwnedByUser(userId);

        return ownedCount < maxAllowed;
    }

}
