package com.sarm.distributed_lovable.common_lib.events;

public record FileStoreRequestEvent(
        Long projectId,
        String sagaId,
        String filePath,
        String content,
        Long userId
){
}
