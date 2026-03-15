package com.sarm.distributed_lovable.workspace_service.entity;

import com.sarm.distributed_lovable.common_lib.enums.PreviewStatus;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.FieldDefaults;

import java.time.Instant;

@Setter
@Getter
@FieldDefaults(level = AccessLevel.PRIVATE)
public class Preview {

    Long id;

    Project project;

    String namespace;
    String podName;

    PreviewStatus previewStatus;

    Instant startedAt;
    Instant terminatedAt;
    Instant createdAt;

}
