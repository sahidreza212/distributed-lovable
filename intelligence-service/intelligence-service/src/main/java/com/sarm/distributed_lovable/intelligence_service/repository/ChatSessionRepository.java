package com.sarm.distributed_lovable.intelligence_service.repository;


import com.sarm.distributed_lovable.intelligence_service.entity.ChatSection;
import com.sarm.distributed_lovable.intelligence_service.entity.ChatSessionId;
import org.springframework.data.jpa.repository.JpaRepository;


public interface ChatSessionRepository extends JpaRepository<ChatSection, ChatSessionId> {


}
