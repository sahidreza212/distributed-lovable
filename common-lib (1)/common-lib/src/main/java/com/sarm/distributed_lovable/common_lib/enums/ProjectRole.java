package com.sarm.distributed_lovable.common_lib.enums;


import lombok.Getter;

import java.util.Set;

import static com.sarm.distributed_lovable.common_lib.enums.ProjectPermission.*;


@Getter

public enum ProjectRole {
    EDITOR(VIEW,EDIT,DELETE,VIEW_MEMBER),
    VIEWER(VIEW,VIEW_MEMBER),
    OWNER(VIEW,EDIT,DELETE,VIEW_MEMBER,MANAGE_MEMBER);

    private final Set<ProjectPermission> permissions;

    ProjectRole (ProjectPermission... permissions){
       this.permissions = Set.of(permissions);
    }


}
