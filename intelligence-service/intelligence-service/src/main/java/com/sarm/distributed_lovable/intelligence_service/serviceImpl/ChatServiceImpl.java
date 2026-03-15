package com.sarm.distributed_lovable.intelligence_service.serviceImpl;


import com.sarm.distributed_lovable.common_lib.security.AuthUtil;
import com.sarm.distributed_lovable.intelligence_service.dtos.ChatResponse;
import com.sarm.distributed_lovable.intelligence_service.entity.ChatMessage;
import com.sarm.distributed_lovable.intelligence_service.entity.ChatSection;
import com.sarm.distributed_lovable.intelligence_service.entity.ChatSessionId;
import com.sarm.distributed_lovable.intelligence_service.mapper.ChatMapper;
import com.sarm.distributed_lovable.intelligence_service.repository.ChatMessageRepository;
import com.sarm.distributed_lovable.intelligence_service.repository.ChatSessionRepository;
import com.sarm.distributed_lovable.intelligence_service.service.ChatService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
@Slf4j
public class ChatServiceImpl implements ChatService {

    private  final ChatSessionRepository chatSessionRepository;
    private  final ChatMessageRepository chatMessageRepository;
    private final AuthUtil authUtil;
    private final ChatMapper chatMapper;

    @Override
    public List<ChatResponse> getProjectChatHistory(Long projectId) {

        Long userId = authUtil.getCurrentUserId();

        ChatSection chatSection = chatSessionRepository.getReferenceById(new ChatSessionId(projectId,userId));

        List<ChatMessage>chatMessageList = chatMessageRepository.findByChatSession(chatSection);
        return  chatMapper.fromListOfChatMessage(chatMessageList);
    }
}
