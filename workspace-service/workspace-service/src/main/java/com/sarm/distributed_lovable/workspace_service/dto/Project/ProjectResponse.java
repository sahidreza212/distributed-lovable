package com.sarm.distributed_lovable.workspace_service.dto.Project;


import java.time.Instant;

public record ProjectResponse(Long id,
                              String name,
                              Instant createdAt,
                              Instant updateAt) {
}
