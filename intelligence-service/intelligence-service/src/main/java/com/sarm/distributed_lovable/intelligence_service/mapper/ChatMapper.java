package com.sarm.distributed_lovable.intelligence_service.mapper;


import com.sarm.distributed_lovable.intelligence_service.dtos.ChatResponse;
import com.sarm.distributed_lovable.intelligence_service.entity.ChatMessage;
import org.mapstruct.Mapper;

import java.util.List;

@Mapper(componentModel = "spring")
public interface ChatMapper {


    List<ChatResponse> fromListOfChatMessage(List<ChatMessage> chatMessageList);
}
