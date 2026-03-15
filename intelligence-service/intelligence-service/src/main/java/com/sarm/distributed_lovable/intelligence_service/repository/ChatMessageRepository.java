package com.sarm.distributed_lovable.intelligence_service.repository;


import com.sarm.distributed_lovable.intelligence_service.entity.ChatMessage;
import com.sarm.distributed_lovable.intelligence_service.entity.ChatSection;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface ChatMessageRepository extends JpaRepository<ChatMessage, Long> {

    @Query("""
            SELECT DISTINCT m FROM ChatMessage m
            LEFT JOIN FETCH m.events e
            WHERE m.chatSection =:chatSection
            ORDER BY m.createdAt ASC, e.sequenceOrder ASC
            """)
    List<ChatMessage> findByChatSession(ChatSection chatSection);
}
