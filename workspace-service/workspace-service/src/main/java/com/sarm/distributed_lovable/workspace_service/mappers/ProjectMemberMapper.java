package com.sarm.distributed_lovable.workspace_service.mappers;


import com.sarm.distributed_lovable.workspace_service.dto.member.MemberResponse;
import com.sarm.distributed_lovable.workspace_service.entity.ProjectMember;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring")
public interface ProjectMemberMapper {

     @Mapping(target = "userId",source = "id.userId")
    MemberResponse toProjectMemberResponseFromMember(ProjectMember projectMember);
}
