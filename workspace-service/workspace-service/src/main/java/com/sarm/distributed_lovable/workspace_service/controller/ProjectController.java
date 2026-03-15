package com.sarm.distributed_lovable.workspace_service.controller;

import com.sarm.distributed_lovable.common_lib.security.AuthUtil;
import com.sarm.distributed_lovable.workspace_service.dto.Project.ProjectRequest;
import com.sarm.distributed_lovable.workspace_service.dto.Project.ProjectResponse;
import com.sarm.distributed_lovable.workspace_service.dto.Project.ProjectSummaryResponse;
import com.sarm.distributed_lovable.workspace_service.dto.deploy.DeployResponse;
import com.sarm.distributed_lovable.workspace_service.service.DeploymentService;
import com.sarm.distributed_lovable.workspace_service.service.ProjectService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects")

public class ProjectController {

    private  final ProjectService projectService;
    private  final AuthUtil authUtil;
    private final DeploymentService deploymentService;

    @GetMapping
    public ResponseEntity<List<ProjectSummaryResponse>>getMyProjects(){

        List<ProjectSummaryResponse>projects=projectService.getUserProject();
        return ResponseEntity.ok(projects);
    }

    @GetMapping("/{id}")
    public ResponseEntity<ProjectSummaryResponse>getProjectById(@PathVariable Long id){
        return ResponseEntity.ok(projectService.getUserProjectById(id));

    }

    @PostMapping
    public  ResponseEntity<ProjectResponse>createProject(@RequestBody @Valid ProjectRequest request){
        return ResponseEntity.status(HttpStatus.CREATED).body(projectService.createProject(request));
    }

    @PatchMapping("/{id}")
    public ResponseEntity<ProjectResponse>updateProject(
            @PathVariable Long id,
         @Valid   @RequestBody ProjectRequest request){
        return ResponseEntity.ok(projectService.updateProject( id,request));
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void>deleteProject(@PathVariable Long id){
        projectService.softDelete(id);
         return  ResponseEntity.noContent().build();
    }

    @PostMapping("/{id}/deploy")
    public ResponseEntity<DeployResponse> deployProject (@PathVariable Long id){
      return ResponseEntity.ok(deploymentService.deploy(id));
    }
}
