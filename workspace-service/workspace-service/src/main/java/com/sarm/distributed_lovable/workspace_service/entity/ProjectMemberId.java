package com.sarm.distributed_lovable.workspace_service.entity;


import jakarta.persistence.Embeddable;
import lombok.*;
import lombok.experimental.FieldDefaults;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
@AllArgsConstructor
@NoArgsConstructor
@Embeddable
public class ProjectMemberId {

    Long projectId;
    Long userId;

}
