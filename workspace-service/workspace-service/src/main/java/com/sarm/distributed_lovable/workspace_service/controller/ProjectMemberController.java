package com.sarm.distributed_lovable.workspace_service.controller;

import com.sarm.distributed_lovable.workspace_service.dto.member.InviteMemberRequest;
import com.sarm.distributed_lovable.workspace_service.dto.member.MemberResponse;
import com.sarm.distributed_lovable.workspace_service.dto.member.updateMemberRoleRequest;
import com.sarm.distributed_lovable.workspace_service.service.ProjectMemberService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequiredArgsConstructor
@RequestMapping("/projects/{projectId}/members")

public class ProjectMemberController {

     private  final ProjectMemberService projectMemberService;

     @GetMapping
    public ResponseEntity<List<MemberResponse>>getProjectMembers(@PathVariable Long projectId) {
         return ResponseEntity.ok(projectMemberService.getProjectMembers(projectId));
     }

     @PostMapping
    public  ResponseEntity<MemberResponse>inviteMember(@PathVariable Long projectId,
                                                     @Valid  @RequestBody InviteMemberRequest request){
         return  ResponseEntity.status(HttpStatus.CREATED).body(projectMemberService.inviteMember(projectId,request));
     }

     @PatchMapping("/{memberId}")
    public ResponseEntity<MemberResponse>updateMemberRole(
            @PathVariable Long projectId,
            @Valid @RequestBody updateMemberRoleRequest request,
            @PathVariable Long memberId){
         return ResponseEntity.ok(projectMemberService.updateMemberRole(projectId,memberId,request));
     }

     @DeleteMapping("/{memberId}")
    public ResponseEntity<Void>removeMember(
            @PathVariable Long projectId,
            @PathVariable Long memberId){
         projectMemberService.removeProjectMember(projectId,memberId);
         return ResponseEntity.noContent().build();
     }
}
