package com.sarm.distributed_lovable.intelligence_service.service;

import com.sarm.distributed_lovable.intelligence_service.dtos.StreamResponse;
import reactor.core.publisher.Flux;


public interface AiGenerationService {

    Flux<StreamResponse> streamResponse(String message, Long projectId);
}
