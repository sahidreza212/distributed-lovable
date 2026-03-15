package com.sarm.distributed_lovable.workspace_service.service;

import com.sarm.distributed_lovable.workspace_service.dto.member.InviteMemberRequest;
import com.sarm.distributed_lovable.workspace_service.dto.member.MemberResponse;
import com.sarm.distributed_lovable.workspace_service.dto.member.updateMemberRoleRequest;

import java.util.List;

public interface ProjectMemberService {


     List<MemberResponse> getProjectMembers(Long projectId);

     MemberResponse inviteMember(Long projectId, InviteMemberRequest request);


     void removeProjectMember(Long projectId, Long memberId);

      MemberResponse updateMemberRole(Long projectId, Long memberId, updateMemberRoleRequest request);
}
