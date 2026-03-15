package com.sarm.distributed_lovable.workspace_service.serviceImpl;


import com.sarm.distributed_lovable.common_lib.dto.UserDto;
import com.sarm.distributed_lovable.common_lib.enums.ProjectRole;
import com.sarm.distributed_lovable.common_lib.error.BadRequestException;
import com.sarm.distributed_lovable.common_lib.error.ResourceNotFoundException;
import com.sarm.distributed_lovable.common_lib.security.AuthUtil;
import com.sarm.distributed_lovable.workspace_service.client.AccountClient;
import com.sarm.distributed_lovable.workspace_service.dto.member.InviteMemberRequest;
import com.sarm.distributed_lovable.workspace_service.dto.member.MemberResponse;
import com.sarm.distributed_lovable.workspace_service.dto.member.updateMemberRoleRequest;
import com.sarm.distributed_lovable.workspace_service.entity.Project;
import com.sarm.distributed_lovable.workspace_service.entity.ProjectMember;
import com.sarm.distributed_lovable.workspace_service.entity.ProjectMemberId;
import com.sarm.distributed_lovable.workspace_service.mappers.ProjectMemberMapper;
import com.sarm.distributed_lovable.workspace_service.repository.ProjectMemberRepository;
import com.sarm.distributed_lovable.workspace_service.repository.ProjectRepository;
import com.sarm.distributed_lovable.workspace_service.service.ProjectMemberService;
import jakarta.transaction.Transactional;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.experimental.FieldDefaults;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.stereotype.Service;

import java.time.Instant;
import java.util.List;

@Service
@Transactional
@AllArgsConstructor
@FieldDefaults(makeFinal = true, level = AccessLevel.PRIVATE)
public class ProjectMemberServiceImpl implements ProjectMemberService {

    ProjectMemberRepository projectMemberRepository;
    ProjectRepository projectRepository;
    ProjectMemberMapper projectMemberMapper;
    AuthUtil authUtil;
    AccountClient accountClient;

    @Override
    @PreAuthorize("@security.canViewMember(#projectId)")
    public List<MemberResponse> getProjectMembers(Long projectId) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessiableProjectById(projectId,userId);

       return projectMemberRepository.findByIdProjectId(projectId)
               .stream()
               .map(projectMemberMapper::toProjectMemberResponseFromMember)
               .toList();
    }

    @Override
    @PreAuthorize("@security.canManageMember(#projectId)")
    public MemberResponse inviteMember(Long projectId, InviteMemberRequest request) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessiableProjectById(projectId,userId);


        UserDto invitee = accountClient.findByUsername(request.username()).orElseThrow();
        if(invitee.id().equals(userId)){
           throw new BadRequestException("You cannot invite yourself");
        }
        ProjectMemberId  projectMemberId = new ProjectMemberId(projectId,invitee.id());
        if(projectMemberRepository.existsById(projectMemberId)){
            throw new BadRequestException("User is already a project member");
        }

        ProjectMember projectMember = ProjectMember.builder()
                .id(projectMemberId)
                .project(project)
                .invitedAt(Instant.now())
                .projectRole(request.role())
                .build();
        projectMemberRepository.save(projectMember);
        return  projectMemberMapper.toProjectMemberResponseFromMember(projectMember);
    }

    @Override
    @PreAuthorize("@security.canManageMember(#projectId)")
    public void removeProjectMember(Long projectId, Long memberId) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessiableProjectById(projectId,userId);

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId,memberId);
        if(! projectMemberRepository.existsById(projectMemberId)){
           throw  new ResourceNotFoundException("ProjectMember ",memberId.toString());
        }
        projectMemberRepository.deleteById(projectMemberId);


    }

    @Override
    @PreAuthorize("@security.canManageMember(#projectId)")
    public MemberResponse updateMemberRole(Long projectId, Long memberId, updateMemberRoleRequest request) {
        Long userId = authUtil.getCurrentUserId();
        Project project = getAccessiableProjectById(projectId, userId);

        ProjectMemberId projectMemberId = new ProjectMemberId(projectId, memberId);
        ProjectMember projectMember = projectMemberRepository.findById(projectMemberId).orElseThrow();
        projectMember.setProjectRole(request.role());
        projectMemberRepository.save(projectMember);
        return projectMemberMapper.toProjectMemberResponseFromMember(projectMember);

        // Internal Function
    }

      public  Project getAccessiableProjectById(Long projectId,Long userId){
        return  projectRepository.findAsseciableProjectById(projectId,userId)
                .orElseThrow(()->new ResourceNotFoundException("project",projectId.toString()));
      }

    }
