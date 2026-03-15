package com.sarm.distributed_lovable.intelligence_service.service;



import com.sarm.distributed_lovable.intelligence_service.dtos.ChatResponse;

import java.util.List;

public interface ChatService {

    List<ChatResponse> getProjectChatHistory(Long projectId);
}
